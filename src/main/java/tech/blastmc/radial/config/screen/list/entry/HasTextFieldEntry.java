package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public abstract class HasTextFieldEntry extends ListEntry {

    public EditBox textField;

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        textField.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (textField.mouseClicked(click, doubled)) {
            textField.setFocused(true);
            return true;
        }
        else
            textField.setFocused(false);
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        textField.mouseReleased(click);
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyEvent keyInput) {
        if (textField.keyPressed(keyInput)) return true;
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean keyReleased(KeyEvent keyInput) {
        if (textField.keyReleased(keyInput)) return true;
        return super.keyReleased(keyInput);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (textField.charTyped(input)) return true;
        return super.charTyped(input);
    }

}
