package tech.blastmc.radial.util;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyboardUtils {

    public static InputUtil.Key toKey(int code) {
        if (code < 0)
            return InputUtil.Type.MOUSE.createFromCode(-code - 1);
        else
            return InputUtil.fromKeyCode(code, 0);
    }

    public static boolean isKeyDown(long window, int code) {
        return code < 0
                ? GLFW.glfwGetMouseButton(window, -code - 1) == GLFW.GLFW_PRESS
                : InputUtil.isKeyPressed(window, code);
    }

}
