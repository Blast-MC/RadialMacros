package tech.blastmc.radial.config.screen;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

    private TextFieldWidget groupName;
    private ButtonWidget hotkeyBtn;
    private boolean capturingKey = false;

    private OptionList list;
    private final IntConsumer onEdit;

    public RadialGroupEditScreen(Screen parent, List<RadialGroup> groups, int index) {
        super(Text.literal("RadialMacros — Group Edit"));
        this.index = index;
        this.parent = parent;
        this.groups = groups;

        if (index >= 0)
            this.group = groups.get(index).clone();

        if (this.group == null)
            this.group = new RadialGroup("", GLFW.GLFW_KEY_Y, new ArrayList<>());

        this.onEdit = i -> this.client.setScreen(new OptionEditScreen(this, this.group, i));
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

    private Text keyTextFor(int code) {
        return KeyboardUtils.toKey(code).getLocalizedText();
    }

    @Override
    protected void init() {
        int top = 28;
        list = addDrawableChild(new OptionList(this.client, this.width, this.height - 28 - top, top, 28));
        rebuildList();

        groupName = ScreenUtils.createTextField(textRenderer, 160, 20, group.getName(), "Group Name", group::setName);
        groupName.setPosition(4, 4);
        groupName.setMaxLength(32);
        addSelectableChild(groupName);

        hotkeyBtn = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
                    capturingKey = true;
                    keyTextFor(group.getKeyCode());
                }).dimensions(this.width - 80 - 4, 4, 80, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> {
            commit();
            close();
        }).dimensions(this.width / 2 - 100, this.height - 24, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        groupName.render(context, mouseX, mouseY, deltaTicks);

        if (capturingKey)
            hotkeyBtn.setMessage(Text.literal("> ").formatted(Formatting.YELLOW)
                    .append(Text.empty().append(keyTextFor(group.getKeyCode())).formatted(Formatting.WHITE).formatted(Formatting.UNDERLINE))
                    .append(" <").formatted(Formatting.YELLOW));
        else
            hotkeyBtn.setMessage(keyTextFor(group.getKeyCode()));
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public void resize(int w, int h) {
        super.resize(w, h);
        rebuildList();
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
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
    public boolean mouseClicked(Click click, boolean doubled) {
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
