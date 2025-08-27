package tech.blastmc.radial;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.blastmc.radial.config.Config;
import tech.blastmc.radial.macros.RadialOption;

import java.util.List;

public class RadialMacros implements ModInitializer {

	public static final String MOD_ID = "radialmacros";
	public static Identifier id(String name) {
		return Identifier.of(MOD_ID, name);
	}

	@Override
	public void onInitialize() {
        Config.load();
        InputHandler.init();
	}

}
