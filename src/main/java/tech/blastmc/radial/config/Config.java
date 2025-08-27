package tech.blastmc.radial.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import tech.blastmc.radial.macros.Database;
import tech.blastmc.radial.macros.RadialGroup;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;

import static tech.blastmc.radial.RadialMacros.MOD_ID;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Config {

	public final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public final static Path GAME_CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
	public final static File CONFIG_FILE = new File(configDir(), MOD_ID + ".json");

	public static File configDir() {
		File mapConfigDir = GAME_CONFIG_DIR.toFile();
		if (!mapConfigDir.exists()) {
			mapConfigDir.mkdirs();
		}
		return mapConfigDir;
	}

	public static JsonObject getJsonObject(File jsonFile) {
		if (jsonFile.exists()) {
			JsonObject jsonObject = loadJson(jsonFile).getAsJsonObject();
			if (jsonObject == null) {
				return new JsonObject();
			}
			return jsonObject;
		}

		save();

		return getJsonObject(CONFIG_FILE);
	}

	public static JsonObject loadJson(File jsonFile) {
		if (jsonFile.exists()) {
			try (Reader reader = new FileReader(jsonFile)) {
				JsonObject object = loadJson(reader);
                if (object != null)
                    return object;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return new JsonObject();
	}

	public static JsonObject loadJson(Reader reader) {
		return GSON.fromJson(reader, JsonObject.class);
	}

	public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            String json = GSON.toJson(Database.get(), Database.class);
            writer.write(json);
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}

	public static void load() {
		JsonObject json = getJsonObject(CONFIG_FILE);
		Database.setGroups(GSON.fromJson(json, Database.class).groups);
	}

}
