package tech.blastmc.radial.util;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public final class SkinService {
    private static final SkinService INSTANCE = new SkinService();
    public static SkinService get() { return INSTANCE; }

    private SkinService() {}

    // --- HTTP + JSON ---
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final Gson GSON = new GsonBuilder().create();
    private static final String UUID_URL    = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";

    // --- Cache (never expires) ---
    // Keys are lowercase usernames
    private final Map<String, GameProfile> byName = new ConcurrentHashMap<>();
    private final Map<UUID, GameProfile>   byUuid = new ConcurrentHashMap<>();

    // Latest-wins generation per name
    private final Map<String, Long> latestGen = new ConcurrentHashMap<>();
    private final AtomicLong genCounter = new AtomicLong(0);

    /** Returns a future that completes when networking yields a real profile; also updates the cache. */
    public CompletableFuture<GameProfile> fetch(String username) {
        String key = sanitize(username);
        if (key.isEmpty()) return CompletableFuture.failedFuture(new IllegalArgumentException("blank name"));

        GameProfile cached = byName.get(key);
        if (cached != null && hasTextures(cached)) {
            return CompletableFuture.completedFuture(cached);
        }
        return fetchInternal(username);
    }

    // ------------ internal ------------

    private CompletableFuture<GameProfile> fetchInternal(String username) {
        final String key = sanitize(username);
        final long gen = genCounter.incrementAndGet();
        latestGen.put(key, gen);

        // 1) name -> uuid
        HttpRequest reqUuid = HttpRequest.newBuilder(URI.create(UUID_URL + key))
                .timeout(Duration.ofSeconds(6))
                .header("Accept", "application/json")
                .GET().build();

        System.out.println("Sending request");
        return HTTP.sendAsync(reqUuid, HttpResponse.BodyHandlers.ofString())
                .thenCompose(resp -> {
                    if (stale(key, gen)) return CompletableFuture.failedFuture(new CancellationException("stale"));
                    if (resp.statusCode() == 204 || resp.statusCode() == 404) {
                        return CompletableFuture.failedFuture(new RuntimeException("No such player: " + username));
                    }
                    if (resp.statusCode() != 200) {
                        return CompletableFuture.failedFuture(new RuntimeException("UUID HTTP " + resp.statusCode()));
                    }

                    JsonObject o = GSON.fromJson(resp.body(), JsonObject.class);
                    String undashed = o.get("id").getAsString();
                    String apiName  = o.get("name").getAsString(); // actual casing
                    UUID uuid = uuidFromUndashed(undashed);

                    // 2) uuid -> textures
                    String url = PROFILE_URL + undashed + "?unsigned=false";
                    HttpRequest reqProfile = HttpRequest.newBuilder(URI.create(url))
                            .timeout(Duration.ofSeconds(6))
                            .header("Accept", "application/json")
                            .GET().build();

                    return HTTP.sendAsync(reqProfile, HttpResponse.BodyHandlers.ofString())
                            .thenApply(resp2 -> {
                                if (stale(key, gen)) throw new CancellationException("stale");
                                if (resp2.statusCode() != 200) throw new RuntimeException("Profile HTTP " + resp2.statusCode());

                                JsonObject p = GSON.fromJson(resp2.body(), JsonObject.class);
                                JsonArray props = p.getAsJsonArray("properties");
                                String value = null, sig = null;
                                for (JsonElement e : props) {
                                    JsonObject prop = e.getAsJsonObject();
                                    if ("textures".equals(prop.get("name").getAsString())) {
                                        value = prop.get("value").getAsString();
                                        if (prop.has("signature")) sig = prop.get("signature").getAsString();
                                        break;
                                    }
                                }
                                if (value == null) throw new RuntimeException("No textures property");

                                GameProfile gp = new GameProfile(uuid, apiName);
                                if (sig != null) gp.getProperties().put("textures", new com.mojang.authlib.properties.Property("textures", value, null));
                                else gp.getProperties().put("textures", new com.mojang.authlib.properties.Property("textures", value));

                                // Update cache on MC thread for safety
                                MinecraftClient.getInstance().execute(() -> {
                                    // Guard latest-wins again before committing
                                    if (!stale(key, gen)) {
                                        byName.put(key, gp);
                                        byUuid.put(uuid, gp);
                                    }
                                });
                                return gp;
                            });
                });
    }

    private boolean hasTextures(GameProfile gp) {
        return gp.getProperties() != null && gp.getProperties().get("textures") != null && !gp.getProperties().get("textures").isEmpty();
    }

    private static String sanitize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private static UUID uuidFromUndashed(String undashed) {
        String u = undashed.toLowerCase(Locale.ROOT);
        String dashed = u.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                "$1-$2-$3-$4-$5");
        return UUID.fromString(dashed);
    }

    private boolean stale(String key, long gen) {
        Long g = latestGen.get(key);
        return g != null && g != gen;
    }
}
