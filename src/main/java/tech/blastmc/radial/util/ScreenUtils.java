package tech.blastmc.radial.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScreenUtils {

    public static final List<Runnable> LAST_RENDERS = new ArrayList<>();

    public static EditBox createTextField(Font textRenderer, int width, int height, String text, String placeholder, Consumer<String> onChangeLister) {
        return ScreenUtils.createTextField(textRenderer, width, height, text, placeholder, null, onChangeLister);
    }

    public static EditBox createTextField(Font textRenderer, int width, int height, String text, String placeholder, String tooltip, Consumer<String> onChangeListener) {
        EditBox textField = new EditBox(textRenderer, width, height, Component.literal(placeholder));
        textField.setHint(Component.literal(placeholder).withStyle(ChatFormatting.GRAY));
        textField.setEditable(true);
        if (text != null && !text.isEmpty())
            textField.setValue(text);
        textField.moveCursorToStart(false);
        textField.setResponder(onChangeListener);
        if (tooltip != null) {
            textField.setTooltip(Tooltip.create(Component.literal(tooltip)));
            textField.setTooltipDelay(Duration.ofMillis(250));
        }
        return textField;
    }

    public static <T> T nextWithLoop(Class<? extends T> clazz, int ordinal) {
        T[] values = clazz.getEnumConstants();
        int next = ordinal + 1 % values.length;
        return next >= values.length ? values[0] : values[next];
    }

    public static boolean canDrawItems() {
        return BuiltInRegistries.ITEM.wrapAsHolder(Items.GRASS_BLOCK).areComponentsBound();
    }

}
