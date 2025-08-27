package tech.blastmc.radial.config.screen.list;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import org.jetbrains.annotations.Nullable;
import tech.blastmc.radial.config.screen.list.entry.CommandEntry;
import tech.blastmc.radial.config.screen.list.entry.ListEntry;

import java.util.Optional;

public class CommandList extends EntryListWidget<ListEntry> {

    public CommandList(MinecraftClient mc, int width, int height, int top, int itemHeight) {
        super(mc, width, height, top, itemHeight, 0);
    }

    @Override
    public int getRowWidth() { return width - 24; }

    @Override
    protected int getScrollbarX() { return width * 2 - 6; }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) { }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ListEntry entry : children())
            if (entry instanceof CommandEntry cmd)
                cmd.textField.setFocused(false);
        for (ListEntry entry : children())
            if (entry.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (ListEntry entry : children())
            if (entry.mouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (ListEntry entry : children())
            entry.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (ListEntry entry : children())
            if (entry.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (ListEntry entry : children())
            if (entry.keyReleased(keyCode, scanCode, modifiers)) return true;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (ListEntry entry : children())
            if (entry.charTyped(chr, modifiers)) return true;
        return super.charTyped(chr, modifiers);
    }
}
