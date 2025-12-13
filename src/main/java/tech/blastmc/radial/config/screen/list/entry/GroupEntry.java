package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import tech.blastmc.radial.macros.RadialGroup;
import tech.blastmc.radial.util.KeyboardUtils;

import java.util.List;
import java.util.function.IntConsumer;

public class GroupEntry extends ListEntry {

    private int index;
    private int width;
    private List<RadialGroup> groups;
    private IntConsumer onEdit;
    private final ButtonWidget editBtn;
    private final ButtonWidget deleteBtn;

    public GroupEntry(int index, int width, List<RadialGroup> groups, Runnable rebuildCallback, IntConsumer onEdit) {
        super(rebuildCallback);
        this.index = index;
        this.width = width;
        this.groups = groups;
        this.onEdit = onEdit;

        editBtn = ButtonWidget.builder(Text.literal("Edit"), b -> onEdit.accept(index))
                .dimensions(0, 0, 54, 20).build();

        deleteBtn = ButtonWidget.builder(Text.literal("Delete"), b -> {
                    if (groups.size() <= 1) return; // keep at least one
                    groups.remove(this.index);
                    rebuildList();
                })
                .dimensions(0, 0, 54, 20).build();
    }

    private static boolean isMouse(int code) { return code < 0; }
    private static int mouseButtonFromCode(int code) { return -code - 1; }

    private Text keyTextFor(int code) {
        return KeyboardUtils.toKey(code).getLocalizedText();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, boolean hovered, float tickProgress) {
        int bg = hovered ? 0x33FFFFFF : 0x22000000;
        ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);

        RadialGroup g = groups.get(index);

        int textY = getY() + (getHeight() - MinecraftClient.getInstance().textRenderer.fontHeight) / 2;
        ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                Text.literal(g.getName()),
                getX() + 8, textY, 0xFFFFFFFF);

        ctx.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                Text.literal("[ ").append(keyTextFor(g.getKeyCode())).append(" ]"),
                width / 2 + 8, textY, 0xFFFFFFFF);

        int btnY = getY() + (getHeight() - 20) / 2;
        int right = getX() + getWidth() - 4;
        deleteBtn.setX(right - 54);
        deleteBtn.setY(btnY);
        editBtn.setX(right - 54 - 4 - 54);
        editBtn.setY(btnY);

        editBtn.render(ctx, mouseX, mouseY, tickProgress);
        deleteBtn.render(ctx, mouseX, mouseY, tickProgress);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (editBtn.mouseClicked(click, doubled)) return true;
        if (deleteBtn.mouseClicked(click, doubled)) return true;

        if (click.button() == 0) {
            onEdit.accept(index);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(Click click) {
        editBtn.mouseReleased(click);
        deleteBtn.mouseReleased(click);
        return true;
    }
}
