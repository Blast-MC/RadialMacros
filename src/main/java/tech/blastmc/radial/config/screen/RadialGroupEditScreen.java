package tech.blastmc.radial.config.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import tech.blastmc.radial.config.screen.list.OptionList;
import tech.blastmc.radial.config.screen.list.entry.AddEntryEntry;
import tech.blastmc.radial.config.screen.list.entry.OptionEntry;
import tech.blastmc.radial.macros.RadialGroup;
import tech.blastmc.radial.util.KeyboardUtils;
import tech.blastmc.radial.util.ScreenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class RadialGroupEditScreen extends Screen {

    private final int index;
    private final Screen parent;
    private final List<RadialGroup> groups;
    private RadialGroup group = null;

    private EditBox groupName;
    private Button hotkeyBtn;
    private boolean capturingKey = false;

    private OptionList list;
    private final IntConsumer onEdit;

    public RadialGroupEditScreen(Screen parent, List<RadialGroup> groups, int index) {
        super(Component.literal("RadialMacros — Group Edit"));
        this.index = index;
        this.parent = parent;
        this.groups = groups;

        if (index >= 0)
            this.group = groups.get(index).clone();

        if (this.group == null)
            this.group = new RadialGroup("", GLFW.GLFW_KEY_Y, new ArrayList<>());

        this.onEdit = i -> this.minecraft.gui.setScreen(new OptionEditScreen(this, this.group, i));
    }

    private void rebuildList() {
        list.clearEntries();
        for (int i = 0; i < group.getOptions().size(); i++)
            list.addEntry(new OptionEntry(i, group, this::rebuildList, onEdit));
        list.addEntry(new AddEntryEntry("Add Macro", () -> onEdit.accept(-1)));
    }

    private static boolean isMouse(int code) { return code < 0; }
    private static int codeFromMouseButton(int btn) { return -(btn + 1); }
    private static int mouseButtonFromCode(int code) { return -code - 1; }

    private Component keyTextFor(int code) {
        return KeyboardUtils.toKey(code).getDisplayName();
    }

    @Override
    protected void init() {
        int top = 28;
        list = addRenderableWidget(new OptionList(this.minecraft, this.width, this.height - 28 - top, top, 28));
        rebuildList();

        groupName = ScreenUtils.createTextField(font, 160, 20, group.getName(), "Group Name", group::setName);
        groupName.setPosition(4, 4);
        groupName.setMaxLength(32);
        addWidget(groupName);

        hotkeyBtn = addRenderableWidget(Button.builder(Component.empty(), b -> {
                    capturingKey = true;
                    keyTextFor(group.getKeyCode());
                }).bounds(this.width - 80 - 4, 4, 80, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
            commit();
            onClose();
        }).bounds(this.width / 2 - 100, this.height - 24, 200, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        super.extractRenderState(context, mouseX, mouseY, deltaTicks);

        groupName.extractRenderState(context, mouseX, mouseY, deltaTicks);

        if (capturingKey)
            hotkeyBtn.setMessage(Component.literal("> ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.empty().append(keyTextFor(group.getKeyCode())).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.UNDERLINE))
                    .append(" <").withStyle(ChatFormatting.YELLOW));
        else
            hotkeyBtn.setMessage(keyTextFor(group.getKeyCode()));
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

    @Override
    public boolean keyPressed(KeyEvent keyInput) {
        if (capturingKey) {
            if (keyInput.key() == GLFW.GLFW_KEY_ESCAPE) {
                capturingKey = false;
                return true;
            }
            group.setKeyCode(keyInput.key());
            capturingKey = false;
            return true;
        }
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (capturingKey) {
            group.setKeyCode(codeFromMouseButton(click.button()));
            capturingKey = false;
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    private void commit() {
        validate();

        if (index == -1)
            groups.add(group);
        else
            groups.set(index, group);
    }

    private void validate() {
        if (group.getName() == null || group.getName().isBlank())
            group.setName(index == -1 ? "New group" : groups.get(index).getName());
    }

}
