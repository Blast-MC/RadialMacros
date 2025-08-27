package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import tech.blastmc.radial.macros.RadialGroup;
import tech.blastmc.radial.macros.RadialOption;

import java.util.function.IntConsumer;

public class OptionEntry extends ListEntry {

    private final int index;
    private final RadialGroup group;
    private final IntConsumer onEdit;

    private final RadialOption option;
    private final ButtonWidget upBtn;
    private final ButtonWidget downBtn;
    private final ButtonWidget editBtn;
    private final ButtonWidget deleteBtn;

    public OptionEntry(int index, RadialGroup group, Runnable rebuildCallback, IntConsumer onEdit) {
        super(rebuildCallback);
        this.index = index;
        this.group = group;
        this.onEdit = onEdit;

        this.option = group.getOptions().get(index);

        upBtn = ButtonWidget.builder(Text.literal("↑"), b -> moveUp())
                .dimensions(0, 0, 20, 20).build();

        downBtn = ButtonWidget.builder(Text.literal("↓"), b -> moveDown())
                .dimensions(0, 0, 20, 20).build();

        editBtn = ButtonWidget.builder(Text.literal("Edit"), b -> onEdit.accept(index))
                .dimensions(0, 0, 54, 20).build();

        deleteBtn = ButtonWidget.builder(Text.literal("Delete"), b -> {
                    group.getOptions().remove(this.index);
                    rebuildList();
                })
                .dimensions(0, 0, 54, 20).build();
    }

    @Override
    public void render(DrawContext ctx, int rowIndex, int y, int x, int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float tickProgress) {
        int bg = hovered ? 0x33FFFFFF : 0x22000000;
        ctx.fill(x, y, x + entryWidth, y + entryHeight, bg);

        ctx.drawItem(option.getIcon(), x + 4, y + (entryHeight - 16) / 2);

        int textY = y + (entryHeight - MinecraftClient.getInstance().textRenderer.fontHeight) / 2 + 1;
        ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                Text.literal(option.getName()),
                x + 24, textY, 0xFFFFFFFF);

        int btnY = y + (entryHeight - 20) / 2;
        int right = x + entryWidth - 4;
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

        upBtn.render(ctx, mouseX, mouseY, tickProgress);
        downBtn.render(ctx, mouseX, mouseY, tickProgress);
        editBtn.render(ctx, mouseX, mouseY, tickProgress);
        deleteBtn.render(ctx, mouseX, mouseY, tickProgress);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (upBtn.mouseClicked(mouseX, mouseY, button)) return true;
        if (downBtn.mouseClicked(mouseX, mouseY, button)) return true;
        if (editBtn.mouseClicked(mouseX, mouseY, button)) return true;
        if (deleteBtn.mouseClicked(mouseX, mouseY, button)) return true;

        if (button == 0) {
            onEdit.accept(index);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        editBtn.mouseReleased(mx, my, btn);
        deleteBtn.mouseReleased(mx, my, btn);
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
