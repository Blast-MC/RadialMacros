package tech.blastmc.radial.config.screen.list;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.ConditionalRuleEntry;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.IconMiscOptionsEntry;
import tech.blastmc.radial.config.screen.list.entry.HasTextFieldEntry;
import tech.blastmc.radial.config.screen.list.entry.ListEntry;

public abstract class HalfWidthList extends AbstractSelectionList<ListEntry> {

    public HalfWidthList(Minecraft mc, int width, int height, int top, int itemHeight) {
        super(mc, width, height, top, itemHeight);
    }

    @Override
    public int getRowWidth() { return width - 24; }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) { }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
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
    public boolean mouseReleased(MouseButtonEvent click) {
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
    public boolean keyPressed(KeyEvent keyInput) {
        for (ListEntry entry : children())
            if (entry.keyPressed(keyInput)) return true;
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean keyReleased(KeyEvent keyInput) {
        for (ListEntry entry : children())
            if (entry.keyReleased(keyInput)) return true;
        return super.keyReleased(keyInput);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
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

        public DetailsList(Minecraft mc, int width, int height, int top, int itemHeight) {
            super(mc, width, height, top, itemHeight);
        }

        @Override
        protected int scrollBarX() { return 0; }

    }

    public static class CommandList extends HalfWidthList {

        public CommandList(Minecraft mc, int width, int height, int top, int itemHeight) {
            super(mc, width, height, top, itemHeight);
        }

        @Override
        protected int scrollBarX() { return width * 2 - 6; }

    }

}
