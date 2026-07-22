package tech.blastmc.radial;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import tech.blastmc.radial.config.Config;
import tech.blastmc.radial.util.ClientTickScheduler;

public class RadialMacros implements ModInitializer {

	public static final String MOD_ID = "radialmacros";

	public static Identifier id(String name) { return Identifier.fromNamespaceAndPath(MOD_ID, name); }
    public static Identifier texture(String name) { return id("textures/" + name + ".png"); }

	@Override
	public void onInitialize() {
        Config.load();
        InputHandler.init();
		ClientTickScheduler.init();
	}

	public static void log(String message) {
		LogManager.getLogger().log(Level.INFO, "[RadialMacros] " + message);
	}

}
