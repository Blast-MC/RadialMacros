package tech.blastmc.radial.config.screen.list;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import tech.blastmc.radial.config.screen.list.entry.ListEntry;

public class GroupList extends AbstractSelectionList<ListEntry> {

    public GroupList(Minecraft mc, int width, int height, int top, int itemHeight) {
        super(mc, width, height, top, itemHeight);
    }

    @Override
    public int getRowWidth() { return width - 24; }

    @Override
    protected int scrollBarX() { return this.width - 6; }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) { }
}
