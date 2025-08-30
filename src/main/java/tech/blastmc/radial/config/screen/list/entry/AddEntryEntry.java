package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AddEntryEntry extends ListEntry {

    private final ButtonWidget addButton;

    public AddEntryEntry(String text, Runnable onClick) {
        addButton = ButtonWidget.builder(Text.literal(text), b -> onClick.run())
                .dimensions(0, 0, 120, 20)
                .build();
    }

    @Override
    public void render(DrawContext ctx, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
        addButton.setX(x + (entryWidth / 2 - 60));
        addButton.setY(y + (entryHeight - 20) / 2);

        addButton.render(ctx, mouseX, mouseY, tickProgress);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return addButton.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        addButton.mouseReleased(mouseX, mouseY, button);
        return true;
    }
}
