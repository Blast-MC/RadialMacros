package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import tech.blastmc.radial.macros.RadialOption.RadialCommand;
import tech.blastmc.radial.macros.RadialOption.RadialCommand.CommandType;
import tech.blastmc.radial.util.ScreenUtils;

import java.util.List;

public class CommandEntry extends HasTextFieldEntry {

    int index;
    List<RadialCommand> commands;

    private final Button deleteBtn;
    private final Button typeBtn;

    public CommandEntry(int index, List<RadialCommand> commands, Runnable rebuildCallback) {
        this.index = index;
        this.commands = commands;

        this.typeBtn = Button.builder(Component.literal(commands.get(index).getType().getName()),b -> {
                    commands.get(index).setType(ScreenUtils.nextWithLoop(CommandType.class, commands.get(index).getType().ordinal()));
                    if (commands.get(index).getType() == CommandType.WAIT)
                        commands.get(index).setValue("");
                    rebuildCallback.run();
                })
                .tooltip(Tooltip.create(Component.literal(commands.get(index).getType().getTooltip())))
                .bounds(0, 0, 50, 20).build();

        textField = ScreenUtils.createTextField(Minecraft.getInstance().font, 100, 20,
                this.commands.get(index).getValue(), this.commands.get(index).getType().getPlaceholder(), input -> commands.get(index).setValue(input));
        textField.setMaxLength(1000000);

        deleteBtn = Button.builder(Component.literal("✕"), b -> {
                    commands.remove(this.index);
                    if (commands.isEmpty())
                        commands.add(new RadialCommand(CommandType.COMMAND, ""));
                    rebuildCallback.run();
                })
                .bounds(0, 0, 20, 20).build();
    }

    @Override
    public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
        typeBtn.setPosition(getX() + 4, getY() + 4);
        typeBtn.extractRenderState(context, mouseX, mouseY, tickProgress);

        textField.setPosition(getX() + 4 + 50 + 4, getY() + 4);
        textField.setWidth(getWidth() - 4 - 4 - 24 - 54);
        textField.extractRenderState(context, mouseX, mouseY, tickProgress);

        deleteBtn.setPosition(getX() + getWidth() - 24, getY() + 4);
        deleteBtn.extractRenderState(context, mouseX, mouseY, tickProgress);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        if (typeBtn.mouseClicked(click, doubled)) return true;
        if (deleteBtn.mouseClicked(click, doubled)) return true;
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (commands.get(index).getType() == CommandType.WAIT) {
            try {
                Integer.parseInt(input.codepointAsString());
                if (textField.charTyped(input)) return true;
            } catch (NumberFormatException ignore) {
                return false;
            }
        }

        if (textField.charTyped(input)) return true;
        return super.charTyped(input);
    }

}
