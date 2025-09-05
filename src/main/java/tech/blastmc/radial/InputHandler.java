package tech.blastmc.radial;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import tech.blastmc.radial.config.screen.RadialGroupsScreen;
import tech.blastmc.radial.macros.RadialGroup;
import tech.blastmc.radial.macros.RadialOption;
import tech.blastmc.radial.macros.db.Database;
import tech.blastmc.radial.screen.RadialMenuScreen;
import tech.blastmc.radial.util.KeyboardUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InputHandler {

    public static final int DEFAULT_KEY = GLFW.GLFW_KEY_U;
    public static KeyBinding OPEN_CONFIG;

    private static final Map<Integer, Boolean> wasDown = new HashMap<>();
    private static final Set<Integer> latched = new HashSet<>();
    private static Integer lastTriggeredCode = null;

    private static final Text ZERO_OPTIONS_MESSAGE = Text.literal("There are currently no commands in this group. " +
            "You can add some in the config screen.").formatted(Formatting.RED);

    static void init() {
        OPEN_CONFIG = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.radialmacros.openconfig",
                DEFAULT_KEY,
                "radialmacros.mod.name"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (OPEN_CONFIG.wasPressed()) {
                client.setScreen(new RadialGroupsScreen(null));
                return;
            }

            pollGroupKeys(client);
        });
    }

    public static void pollGroupKeys(MinecraftClient client) {
        long window = client.getWindow().getHandle();

        if (client.currentScreen instanceof RadialMenuScreen) {
            updateReleases(window);
            return;
        }
        if (client.currentScreen != null)
            return;

        for (RadialGroup group : Database.getGroups()) {
            int code = group.getKeyCode();
            boolean down = KeyboardUtils.isKeyDown(window, code);
            boolean previous = wasDown.getOrDefault(code, false);

            if (!down) latched.remove(code);

            wasDown.put(code, down);

            if (down && !previous && !latched.contains(code)) {
                lastTriggeredCode = code;
                List<RadialOption> options = group.getOptions().stream().filter(RadialOption::isVisible).toList();

                if (options.isEmpty())
                    client.inGameHud.getChatHud().addMessage(ZERO_OPTIONS_MESSAGE);
                else if (options.size() == 1)
                    options.getFirst().run();
                else
                    client.setScreen(new RadialMenuScreen(options, KeyboardUtils.toKey(code)));
                return;
            }
        }
    }

    private static void updateReleases(long window) {
        for (Integer code : new ArrayList<>(wasDown.keySet())) {
            boolean down = KeyboardUtils.isKeyDown(window, code);
            if (!down)
                latched.remove(code);
            wasDown.put(code, down);
        }
    }

    public static void latchUntilReleased() {
        if (lastTriggeredCode != null)
            latched.add(lastTriggeredCode);
    }

}
