package tech.blastmc.radial.screen;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class InGameControlsEnabledScreen extends Screen {

    protected InGameControlsEnabledScreen(Component title) {
        super(title);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        KeyMapping.setAll();
    }

    @Override
    public boolean keyPressed(KeyEvent keyInput) {
        if (keyInput.key() == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return false;
    }

    @Override public boolean keyReleased(KeyEvent keyInput) { return false; }
    @Override public boolean charTyped(CharacterEvent input) { return false; }

    @Override public boolean mouseClicked(MouseButtonEvent click, boolean doubled) { return true; }
    @Override public boolean mouseReleased(MouseButtonEvent click) { return true; }
    @Override public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) { return true; }

}
