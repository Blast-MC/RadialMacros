package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import tech.blastmc.radial.util.ScreenUtils;

import java.util.List;

public class CommandEntry extends HasTextFieldEntry {

    Screen screen;
    int index;
    List<String> commands;

    private final ButtonWidget deleteBtn;

    public CommandEntry(Screen screen, int index, List<String> commands) {

        this.screen = screen;
        this.index = index;
        this.commands = commands;

        textField = ScreenUtils.createTextField(screen.getTextRenderer(), 100, 20,
                this.commands.get(index), "/say hello", input -> commands.set(index, input));
        textField.setMaxLength(1000000);

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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (deleteBtn.mouseClicked(mouseX, mouseY, button)) return true;
        return false;
    }

}
