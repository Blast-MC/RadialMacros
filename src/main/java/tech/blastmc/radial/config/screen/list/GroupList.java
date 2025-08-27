package tech.blastmc.radial.config.screen.list;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import tech.blastmc.radial.config.screen.list.entry.ListEntry;

public class GroupList extends EntryListWidget<ListEntry> {

    public GroupList(MinecraftClient mc, int width, int height, int top, int itemHeight) {
        super(mc, width, height, top, itemHeight, 0);
    }

    @Override
    public int getRowWidth() { return width - 24; }

    @Override
    protected int getScrollbarX() { return this.width - 6; }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) { }
}
