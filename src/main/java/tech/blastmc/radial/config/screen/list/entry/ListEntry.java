package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.gui.components.AbstractSelectionList;

public abstract class ListEntry extends AbstractSelectionList.Entry<ListEntry> {

    private final Runnable rebuildCallback;

    public ListEntry(Runnable rebuildCallback) {
        this.rebuildCallback = rebuildCallback;
    }

    public ListEntry() {
        this.rebuildCallback = null;
    }

    public void rebuildList() {
        if (this.rebuildCallback != null)
            this.rebuildCallback.run();
    }

}
