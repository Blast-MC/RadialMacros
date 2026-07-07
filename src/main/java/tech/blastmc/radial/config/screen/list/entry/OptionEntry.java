package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import tech.blastmc.radial.macros.RadialGroup;
import tech.blastmc.radial.macros.RadialOption;

import java.util.function.IntConsumer;

public class OptionEntry extends ListEntry {

    private final int index;
    private final RadialGroup group;
    private final IntConsumer onEdit;

    private final RadialOption option;
    private final Button upBtn;
    private final Button downBtn;
    private final Button editBtn;
    private final Button deleteBtn;

    public OptionEntry(int index, RadialGroup group, Runnable rebuildCallback, IntConsumer onEdit) {
        super(rebuildCallback);
        this.index = index;
        this.group = group;
        this.onEdit = onEdit;

        this.option = group.getOptions().get(index);

        upBtn = Button.builder(Component.literal("↑"), b -> moveUp())
                .bounds(0, 0, 20, 20).build();

        downBtn = Button.builder(Component.literal("↓"), b -> moveDown())
                .bounds(0, 0, 20, 20).build();

        editBtn = Button.builder(Component.literal("Edit"), b -> onEdit.accept(index))
                .bounds(0, 0, 54, 20).build();

        deleteBtn = Button.builder(Component.literal("Delete"), b -> {
                    group.getOptions().remove(this.index);
                    rebuildList();
                })
                .bounds(0, 0, 54, 20).build();
    }

    @Override
    public void extractContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, boolean hovered, float tickProgress) {
        int bg = hovered ? 0x33FFFFFF : 0x22000000;
        ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);

        ctx.item(option.getIcon(), getX() + 4, getY() + (getHeight() - 16) / 2);

        int textY = getY() + (getHeight() - Minecraft.getInstance().font.lineHeight) / 2 + 1;
        ctx.text(Minecraft.getInstance().font,
                Component.literal(option.getName()),
                getX() + 24, textY, 0xFFFFFFFF);

        int btnY = getY() + (getHeight() - 20) / 2;
        int right = getX() + getWidth() - 4;
        deleteBtn.setX(right - 54);
        deleteBtn.setY(btnY);
        editBtn.setX(right - 54 - 4 - 54);
        editBtn.setY(btnY);
        upBtn.setX(right - 54 - 4 - 54 - 4 - 20);
        upBtn.setY(btnY);
        downBtn.setX(right - 54 - 4 - 54 - 4 - 20 - 4 - 20);
        downBtn.setY(btnY);

        upBtn.active = index > 0;
        downBtn.active = index < group.getOptions().size() - 1;

        upBtn.extractRenderState(ctx, mouseX, mouseY, tickProgress);
        downBtn.extractRenderState(ctx, mouseX, mouseY, tickProgress);
        editBtn.extractRenderState(ctx, mouseX, mouseY, tickProgress);
        deleteBtn.extractRenderState(ctx, mouseX, mouseY, tickProgress);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (upBtn.mouseClicked(click, doubled)) return true;
        if (downBtn.mouseClicked(click, doubled)) return true;
        if (editBtn.mouseClicked(click, doubled)) return true;
        if (deleteBtn.mouseClicked(click, doubled)) return true;

        if (click.button() == 0) {
            onEdit.accept(index);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        editBtn.mouseReleased(click);
        deleteBtn.mouseReleased(click);
        return true;
    }

    public void moveUp() {
        RadialOption option = group.getOptions().remove(index);
        group.getOptions().add(index - 1, option);
        rebuildList();
    }

    public void moveDown() {
        RadialOption option = group.getOptions().remove(index);
        group.getOptions().add(index + 1, option);
        rebuildList();
    }

}
