package tech.blastmc.radial.config.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
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
        super(Text.literal("RadialMacros — Groups"));
        this.parent = parent;
        this.groups = Database.getGroupsForEdit();
        this.onEdit = i -> this.client.setScreen(new RadialGroupEditScreen(this, groups, i));
    }

    @Override
    protected void init() {
        int top = 28;

        list = addDrawableChild(new GroupList(this.client, this.width, this.height - 28 - top, top, 28));
        rebuildList();

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> {
            commit();
            close();
        }).dimensions(this.width / 2 - 100, this.height - 24, 200, 20).build());

        addDrawableChild(new TextWidget(this.width / 2 - 40, 4, 80, 20, Text.literal("Radial Groups"), textRenderer));
    }

    private void rebuildList() {
        list.clearEntries();
        for (int i = 0; i < groups.size(); i++)
            list.addEntry(new GroupEntry(i, width, groups, this::rebuildList, onEdit));
        list.addEntry(new AddEntryEntry( "Add New Group", () -> onEdit.accept(-1)));
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public void resize(MinecraftClient client, int w, int h) {
        super.resize(client, w, h);
        rebuildList();
    }

    private void commit() {
        Database.commit(groups);
    }

}
