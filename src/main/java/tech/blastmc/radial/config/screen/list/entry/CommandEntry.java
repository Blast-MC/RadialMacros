package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import tech.blastmc.radial.util.ScreenUtils;

import java.util.List;

public class CommandEntry extends HasTextFieldEntry {

    int index;
    List<String> commands;

    private final Button deleteBtn;

    public CommandEntry(int index, List<String> commands, Runnable rebuildCallback) {
        this.index = index;
        this.commands = commands;

        textField = ScreenUtils.createTextField(Minecraft.getInstance().font, 100, 20,
                this.commands.get(index), "/say hello", input -> commands.set(index, input));
        textField.setMaxLength(1000000);

        deleteBtn = Button.builder(Component.literal("✕"), b -> {
                    commands.remove(this.index);
                    if (commands.isEmpty())
                        commands.add("");
                    rebuildCallback.run();
                })
                .bounds(0, 0, 20, 20).build();
    }

    @Override
    public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
        textField.setPosition(getX() + 4, getY() + 4);
        textField.setWidth(getWidth() - 4 - 4 - 24);
        textField.extractRenderState(context, mouseX, mouseY, tickProgress);

        deleteBtn.setPosition(getX() + getWidth() - 24, getY() + 4);
        deleteBtn.extractRenderState(context, mouseX, mouseY, tickProgress);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        if (deleteBtn.mouseClicked(click, doubled)) return true;
        return false;
    }

}
