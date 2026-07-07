package tech.blastmc.radial.config.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import tech.blastmc.radial.config.screen.list.GroupList;
import tech.blastmc.radial.config.screen.list.entry.AddEntryEntry;
import tech.blastmc.radial.config.screen.list.entry.GroupEntry;
import tech.blastmc.radial.macros.RadialGroup;
import tech.blastmc.radial.macros.db.Database;

import java.util.List;
import java.util.function.IntConsumer;

public class RadialGroupsScreen extends Screen {
    private final Screen parent;
    private final List<RadialGroup> groups;
    private final IntConsumer onEdit;

    private GroupList list;

    public RadialGroupsScreen(Screen parent) {
        super(Component.literal("RadialMacros — Groups"));
        this.parent = parent;
        this.groups = Database.getGroupsForEdit();
        this.onEdit = i -> this.minecraft.gui.setScreen(new RadialGroupEditScreen(this, groups, i));
    }

    @Override
    protected void init() {
        int top = 28;

        list = addRenderableWidget(new GroupList(this.minecraft, this.width, this.height - 28 - top, top, 28));
        rebuildList();

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            commit();
            onClose();
        }).bounds(this.width / 2 - 100, this.height - 24, 200, 20).build());

        addRenderableWidget(new StringWidget(this.width / 2 - 40, 4, 80, 20, Component.literal("Radial Groups"), font));
    }

    private void rebuildList() {
        list.clearEntries();
        for (int i = 0; i < groups.size(); i++)
            list.addEntry(new GroupEntry(i, width, groups, this::rebuildList, onEdit));
        list.addEntry(new AddEntryEntry( "Add New Group", () -> onEdit.accept(-1)));
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }

    @Override
    public void resize(int w, int h) {
        super.resize(w, h);
        rebuildList();
    }

    private void commit() {
        Database.commit(groups);
    }

}
