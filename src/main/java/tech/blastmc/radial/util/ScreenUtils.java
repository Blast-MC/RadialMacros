package tech.blastmc.radial.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.Duration;
import java.util.function.Consumer;

public class ScreenUtils {

    public static TextFieldWidget createTextField(TextRenderer textRenderer, int width, int height, String text, String placeholder, Consumer<String> onChangeLister) {
        return ScreenUtils.createTextField(textRenderer, width, height, text, placeholder, null, onChangeLister);
    }

    public static TextFieldWidget createTextField(TextRenderer textRenderer, int width, int height, String text, String placeholder, String tooltip, Consumer<String> onChangeListener) {
        TextFieldWidget textField = new TextFieldWidget(textRenderer, width, height, Text.literal(placeholder));
        textField.setPlaceholder(Text.literal(placeholder).formatted(Formatting.GRAY));
        textField.setEditable(true);
        textField.setText(text);
        textField.setCursorToStart(false);
        textField.setChangedListener(onChangeListener);
        if (tooltip != null) {
            textField.setTooltip(Tooltip.of(Text.literal(tooltip)));
            textField.setTooltipDelay(Duration.ofMillis(250));
        }
        return textField;
    }

}
