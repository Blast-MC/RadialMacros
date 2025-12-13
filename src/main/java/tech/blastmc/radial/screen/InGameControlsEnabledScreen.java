package tech.blastmc.radial.screen;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
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
    public boolean keyPressed(KeyInput keyInput) {
        if (keyInput.key() == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    @Override public boolean keyReleased(KeyInput keyInput) { return false; }
    @Override public boolean charTyped(CharInput input) { return false; }

    @Override public boolean mouseClicked(Click click, boolean doubled) { return true; }
    @Override public boolean mouseReleased(Click click) { return true; }
    @Override public boolean mouseDragged(Click click, double offsetX, double offsetY) { return true; }

}
