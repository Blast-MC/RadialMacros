package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

public abstract class HasTextFieldEntry extends ListEntry {

    public TextFieldWidget textField;

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        textField.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (textField.mouseClicked(click, doubled)) {
            textField.setFocused(true);
            return true;
        }
        else
            textField.setFocused(false);
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        textField.mouseReleased(click);
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        if (textField.keyPressed(keyInput)) return true;
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean keyReleased(KeyInput keyInput) {
        if (textField.keyReleased(keyInput)) return true;
        return super.keyReleased(keyInput);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (textField.charTyped(input)) return true;
        return super.charTyped(input);
    }

}
