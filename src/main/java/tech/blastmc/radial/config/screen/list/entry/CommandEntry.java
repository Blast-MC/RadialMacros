package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import tech.blastmc.radial.util.ScreenUtils;

import java.util.List;

public class CommandEntry extends HasTextFieldEntry {

    int index;
    List<String> commands;

    private final ButtonWidget deleteBtn;

    public CommandEntry(int index, List<String> commands, Runnable rebuildCallback) {
        this.index = index;
        this.commands = commands;

        textField = ScreenUtils.createTextField(MinecraftClient.getInstance().textRenderer, 100, 20,
                this.commands.get(index), "/say hello", input -> commands.set(index, input));
        textField.setMaxLength(1000000);

        deleteBtn = ButtonWidget.builder(Text.literal("✕"), b -> {
                    commands.remove(this.index);
                    if (commands.isEmpty())
                        commands.add("");
                    rebuildCallback.run();
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (deleteBtn.mouseClicked(mouseX, mouseY, button)) return true;
        return false;
    }

}
