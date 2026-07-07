package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class AddEntryEntry extends ListEntry {

    private final Button addButton;

    public AddEntryEntry(String text, Runnable onClick) {
        addButton = Button.builder(Component.literal(text), b -> onClick.run())
                .bounds(0, 0, 120, 20)
                .build();
    }

    @Override
    public void extractContent(GuiGraphicsExtractor ctx, int mouseX, int mouseY, boolean hovered, float tickProgress) {
        addButton.setX(getX() + (getWidth() / 2 - 60));
        addButton.setY(getY() + (getHeight() - 20) / 2);

        addButton.extractRenderState(ctx, mouseX, mouseY, tickProgress);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        return addButton.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        addButton.mouseReleased(click);
        return true;
    }
}
