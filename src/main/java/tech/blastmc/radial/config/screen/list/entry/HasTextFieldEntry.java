package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.gui.widget.TextFieldWidget;

public abstract class HasTextFieldEntry extends ListEntry {

    public TextFieldWidget textField;

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        textField.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (textField.mouseClicked(mouseX, mouseY, button)) {
            textField.setFocused(true);
            return true;
        }
        else
            textField.setFocused(false);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        textField.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (textField.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (textField.keyReleased(keyCode, scanCode, modifiers)) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (textField.charTyped(chr, modifiers)) return true;
        return super.charTyped(chr, modifiers);
    }

}
