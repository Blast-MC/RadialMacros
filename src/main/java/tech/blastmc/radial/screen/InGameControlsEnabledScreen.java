package tech.blastmc.radial.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class InGameControlsEnabledScreen extends Screen {

    protected InGameControlsEnabledScreen(Text title) {
        super(title);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void tick() {
        KeyBinding.updatePressedStates();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    @Override public boolean keyReleased(int keyCode, int scanCode, int modifiers) { return false; }
    @Override public boolean charTyped(char chr, int modifiers) { return false; }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) { return true; }
    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) { return true; }
    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) { return true; }

}
