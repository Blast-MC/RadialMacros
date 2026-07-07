package tech.blastmc.radial.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;

public class KeyboardUtils {

    public static InputConstants.Key toKey(int code) {
        if (code < 0)
            return InputConstants.Type.MOUSE.getOrCreate(-code - 1);
        else
            return InputConstants.Type.KEYSYM.getOrCreate(code);
    }

    public static boolean isKeyDown(Window window, int code) {
        return code < 0
                ? GLFW.glfwGetMouseButton(window.handle(), -code - 1) == GLFW.GLFW_PRESS
                : InputConstants.isKeyDown(window, code);
    }

}
