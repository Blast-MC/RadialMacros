package tech.blastmc.radial;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import tech.blastmc.radial.config.Config;

public class RadialMacros implements ModInitializer {

	public static final String MOD_ID = "radialmacros";

	public static Identifier id(String name) { return Identifier.of(MOD_ID, name); }
    public static Identifier texture(String name) { return id("textures/" + name + ".png"); }

	@Override
	public void onInitialize() {
        Config.load();
        InputHandler.init();
	}

}
