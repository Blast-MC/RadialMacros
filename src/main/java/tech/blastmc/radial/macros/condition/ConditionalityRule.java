package tech.blastmc.radial.macros.condition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import tech.blastmc.radial.macros.RadialGroup;
import tech.blastmc.radial.macros.RadialOption;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public enum ConditionalityRule {
    PLAYER_ONLINE("Player is online", ServerPlayerEntity.class) {
        @Override
        public boolean test(String value) {
            if (!isMultiplayer())
                return false;
            for (String string : value.split(","))
                if (getOnlinePlayers().contains(string.trim()))
                    return true;
            return false;
        }
    },
    PLAYER_OFFLINE("Player not online", ServerPlayerEntity.class) {
        @Override
        public boolean test(String value) {
            if (!isMultiplayer())
                return false;
            for (String string : value.split(","))
                if (getOnlinePlayers().contains(string.trim()))
                    return false;
            return true;
        }
    },
    IS_MULTIPLAYER("Is multiplayer", null) {
        @Override
        public boolean test(String value) {
            return isMultiplayer();
        }
    },
    WORLD_EQUALS("World equals", ServerWorld.class) {
        @Override
        public boolean test(String value) {
            for (String string : value.split(","))
                if (getWorldName().equalsIgnoreCase(string.trim()))
                    return true;
            return false;
        }
    },
    WORLD_NOT_EQUALS("World not equals", ServerWorld.class) {
        @Override
        public boolean test(String value) {
            for (String string : value.split(","))
                if (getWorldName().equalsIgnoreCase(string.trim()))
                    return false;
            return true;
        }
    },
    OPTION_VISIBLE("Macro visible", RadialOption.class) {
        @Override
        public boolean testWithContext(String value, Object... context) {
            if (context == null || context.length == 0)
                return false;
            if (!(context[0] instanceof RadialGroup group))
                return false;

            RadialOption option = null;
            for (RadialOption _o : group.getOptions())
                if (_o.getName().equalsIgnoreCase(value))
                    option = _o;

            if (option == null)
                return false;

            return option.isVisible();
        }

        @Override
        public boolean needsContext() {
            return true;
        }
    },
    OPTION_NOT_VISIBLE("Macro not visible", RadialOption.class) {
        @Override
        public boolean testWithContext(String value, Object... context) {
            if (context == null || context.length == 0)
                return false;
            if (!(context[0] instanceof RadialGroup group))
                return false;

            RadialOption option = null;
            for (RadialOption _o : group.getOptions())
                if (_o.getName().equalsIgnoreCase(value))
                    option = _o;

            if (option == null)
                return false;

            return !option.isVisible();
        }

        @Override
        public boolean needsContext() {
            return true;
        }
    },
    SERVER_EQUALS("Server equals", ServerWorld.class) {
        @Override
        public boolean test(String value) {
            if (!isMultiplayer())
                return false;
            for (String string : value.split(","))
                if (MC.getCurrentServerEntry().address.equalsIgnoreCase(string.trim()))
                    return true;
            return false;
        }
    },
    SERVER_NOT_EQUALS("Server not equals", ServerWorld.class) {
        @Override
        public boolean test(String value) {
            if (!isMultiplayer())
                return true;
            for (String string : value.split(","))
                if (MC.getCurrentServerEntry().address.equalsIgnoreCase(string.trim()))
                    return false;
            return true;
        }
    }
    ;

    @Getter
    final String display;
    @Getter
    final Class<?> type;

    public boolean hasValue() {
        return type != null;
    }

    public boolean needsContext() {
        return false;
    }

    public boolean test(String value) {
        return true;
    }

    public boolean testWithContext(String value, Object... context) {
        return true;
    }

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public static boolean inWorld() {
        return MC.world != null && MC.player != null;
    }

    public static String getWorldName() {
        if (!inWorld())
            return "Unknown";

        if (MC.getNetworkHandler() != null && !MC.getNetworkHandler().getBrand().equalsIgnoreCase("vanilla"))
            return MC.world.getRegistryKey().getValue().getPath();
        else if (MC.getServer() != null)
            return MC.getServer().getSaveProperties().getLevelName();
        return "Unknown";
    }

    public static boolean onRemoteMultiplayer() {
        return inWorld() && MC.getCurrentServerEntry() != null;
    }

    public static boolean hostingIntegrated() {
        return inWorld() && MC.isIntegratedServerRunning();
    }

    public static boolean lanOpen() {
        return hostingIntegrated()
                && MC.getServer() != null
                && MC.isConnectedToLocalServer();
    }

    public static boolean isMultiplayer() {
        return onRemoteMultiplayer() || lanOpen();
    }

    public static List<String> getOnlinePlayers() {
        ClientPlayNetworkHandler network = MC.getNetworkHandler();
        if (network == null || !isMultiplayer())
            return Collections.emptyList();

        return network.getPlayerList().stream()
                .map(entry -> entry.getProfile().getName())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

}
