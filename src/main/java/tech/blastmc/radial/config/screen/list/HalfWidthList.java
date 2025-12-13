package tech.blastmc.radial.config.screen.list;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.ConditionalRuleEntry;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.IconMiscOptionsEntry;
import tech.blastmc.radial.config.screen.list.entry.HasTextFieldEntry;
import tech.blastmc.radial.config.screen.list.entry.ListEntry;

public abstract class HalfWidthList extends EntryListWidget<ListEntry> {

    public HalfWidthList(MinecraftClient mc, int width, int height, int top, int itemHeight) {
        super(mc, width, height, top, itemHeight);
    }

    @Override
    public int getRowWidth() { return width - 24; }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) { }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        for (ListEntry entry : children()) {
            if (entry instanceof HasTextFieldEntry hasTextFieldEntry)
                hasTextFieldEntry.textField.setFocused(false);
            if (entry instanceof IconMiscOptionsEntry iconOptionsEntry)
                iconOptionsEntry.textField2.setFocused(false);
        }
        for (ListEntry entry : children()) {
            if (entry instanceof ConditionalRuleEntry conditionalRuleEntry) {
                if (conditionalRuleEntry.widget.isExpanded())
                    return conditionalRuleEntry.mouseClicked(click, doubled);
            }
        }
        for (ListEntry entry : children())
            if (entry.mouseClicked(click, doubled)) return true;
        return false;
    }

    @Override
    public boolean mouseReleased(Click click) {
        for (ListEntry entry : children())
            if (entry.mouseReleased(click)) return true;
        return super.mouseReleased(click);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (ListEntry entry : children())
            entry.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        for (ListEntry entry : children())
            if (entry.keyPressed(keyInput)) return true;
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean keyReleased(KeyInput keyInput) {
        for (ListEntry entry : children())
            if (entry.keyReleased(keyInput)) return true;
        return super.keyReleased(keyInput);
    }

    @Override
    public boolean charTyped(CharInput input) {
        for (ListEntry entry : children())
            if (entry.charTyped(input)) return true;
        return super.charTyped(input);
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
