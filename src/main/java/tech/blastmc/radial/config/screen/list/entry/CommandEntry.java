package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import tech.blastmc.radial.config.screen.OptionEditScreen;
import tech.blastmc.radial.util.ScreenUtils;

import java.awt.*;
import java.util.List;

public class CommandEntry extends ListEntry {

    OptionEditScreen screen;
    int index;
    List<String> commands;

    public TextFieldWidget textField;
    private final ButtonWidget deleteBtn;

    public CommandEntry(OptionEditScreen screen, int index, List<String> commands) {
        super(null);

        this.screen = screen;
        this.index = index;
        this.commands = commands;

        this.textField = ScreenUtils.createTextField(screen.getTextRenderer(), 100, 20,
                this.commands.get(index), "/say hello", input -> commands.set(index, input));
        screen.addSelectableChild(textField);

        deleteBtn = ButtonWidget.builder(Text.literal("✕"), b -> {
                    commands.remove(this.index);
                    if (commands.isEmpty())
                        commands.add("");
                    MinecraftClient.getInstance().setScreen(screen);
                })
                .dimensions(0, 0, 20, 20).build();
    }

    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
        textField.setPosition(x + 4, y + 4);
        textField.setWidth(entryWidth - 4 - 4 - 24);
        textField.render(context, mouseX, mouseY, tickProgress);

        deleteBtn.setPosition(x + entryWidth - 24, y + 4);
        deleteBtn.render(context, mouseX, mouseY, tickProgress);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        textField.mouseMoved(mouseX, mouseY);
        deleteBtn.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (textField.mouseClicked(mouseX, mouseY, button)) {
            textField.setFocused(true);
            return true;
        }
        else
            textField.setFocused(false);
        if (deleteBtn.mouseClicked(mouseX, mouseY, button)) return true;
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        textField.mouseReleased(mouseX, mouseY, button);
        deleteBtn.mouseReleased(mouseX, mouseY, button);
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
