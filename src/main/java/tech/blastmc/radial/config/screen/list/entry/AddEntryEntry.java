package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.gui.Click;
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
    public void render(DrawContext ctx, int mouseX, int mouseY, boolean hovered, float tickProgress) {
        addButton.setX(getX() + (getWidth() / 2 - 60));
        addButton.setY(getY() + (getHeight() - 20) / 2);

        addButton.render(ctx, mouseX, mouseY, tickProgress);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        return addButton.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        addButton.mouseReleased(click);
        return true;
    }
}
