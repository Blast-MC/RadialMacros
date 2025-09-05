package tech.blastmc.radial.config.screen.list;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.ConditionalRuleEntry;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.IconMiscOptionsEntry;
import tech.blastmc.radial.config.screen.list.entry.HasTextFieldEntry;
import tech.blastmc.radial.config.screen.list.entry.ListEntry;

public abstract class HalfWidthList extends EntryListWidget<ListEntry> {

    public HalfWidthList(MinecraftClient mc, int width, int height, int top, int itemHeight) {
        super(mc, width, height, top, itemHeight, 0);
    }

    @Override
    public int getRowWidth() { return width - 24; }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) { }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ListEntry entry : children()) {
            if (entry instanceof HasTextFieldEntry hasTextFieldEntry)
                hasTextFieldEntry.textField.setFocused(false);
            if (entry instanceof IconMiscOptionsEntry iconOptionsEntry)
                iconOptionsEntry.textField2.setFocused(false);
        }
        for (ListEntry entry : children()) {
            if (entry instanceof ConditionalRuleEntry conditionalRuleEntry) {
                if (conditionalRuleEntry.widget.isExpanded())
                    return conditionalRuleEntry.mouseClicked(mouseX, mouseY, button);
            }
        }
        for (ListEntry entry : children())
            if (entry.mouseClicked(mouseX, mouseY, button)) return true;
        return false;
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (ListEntry entry : children().reversed())
            if (entry.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public static class DetailsList extends HalfWidthList {

        public DetailsList(MinecraftClient mc, int width, int height, int top, int itemHeight) {
            super(mc, width, height, top, itemHeight);
        }

        @Override
        protected int getScrollbarX() { return 0; }

    }

    public static class CommandList extends HalfWidthList {

        public CommandList(MinecraftClient mc, int width, int height, int top, int itemHeight) {
            super(mc, width, height, top, itemHeight);
        }

        @Override
        protected int getScrollbarX() { return width * 2 - 6; }

    }

}
