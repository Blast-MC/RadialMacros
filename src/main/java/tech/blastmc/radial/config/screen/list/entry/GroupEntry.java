package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import tech.blastmc.radial.macros.RadialGroup;

import java.util.List;
import java.util.function.Consumer;
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
        if (isMouse(code)) {
            int btn = mouseButtonFromCode(code);
            return InputUtil.Type.MOUSE.createFromCode(btn).getLocalizedText(); // e.g. "Button 4"
        } else {
            return InputUtil.fromKeyCode(code, 0).getLocalizedText();           // e.g. "R"
        }
    }

    @Override
    public void render(DrawContext ctx, int rowIndex, int y, int x, int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float tickProgress) {
        this.index = rowIndex;

        int bg = hovered ? 0x33FFFFFF : 0x22000000;
        ctx.fill(x, y, x + entryWidth, y + entryHeight, bg);

        RadialGroup g = groups.get(rowIndex);

        int textY = y + (entryHeight - MinecraftClient.getInstance().textRenderer.fontHeight) / 2;
        ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                Text.literal(g.getName()),
                x + 8, textY, 0xFFFFFFFF);

        ctx.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                Text.literal("[ ").append(keyTextFor(g.getKeyCode())).append(" ]"),
                width / 2 + 8, textY, 0xFFFFFFFF);

        int btnY = y + (entryHeight - 20) / 2;
        int right = x + entryWidth - 4;
        deleteBtn.setX(right - 54);
        deleteBtn.setY(btnY);
        editBtn.setX(right - 54 - 4 - 54);
        editBtn.setY(btnY);

        editBtn.render(ctx, mouseX, mouseY, tickProgress);
        deleteBtn.render(ctx, mouseX, mouseY, tickProgress);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
}
