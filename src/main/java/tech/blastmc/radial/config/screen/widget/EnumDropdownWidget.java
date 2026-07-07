package tech.blastmc.radial.config.screen.widget;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import tech.blastmc.radial.config.screen.list.entry.CustomHeightEntry;
import tech.blastmc.radial.util.ExtraHoveredIgnored;
import tech.blastmc.radial.util.HasId;
import tech.blastmc.radial.util.ScreenUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class EnumDropdownWidget<T extends Enum<T>> extends AbstractWidget implements HasId {

    public static List<EnumDropdownWidget<?>> WIDGETS = new ArrayList<>();

    @Getter
    private final String id;

    @Getter
    private T currentValue;
    @Setter
    private Consumer<T> onChangeListener;

    private final Function<T, String> displayNameFunction;

    private final EditBox textField;
    private final DropdownEntryList<T> entryListWidget;

    @Getter @Setter
    private boolean expanded = false;
    private final int itemHeight = 16;

    private static final int OUTLINE_COLOR = new Color(160, 160, 160, 255).getRGB();

    public EnumDropdownWidget(String id, int x, int y, int width, int height, @NonNull T currentValue, Function<T, String> displayNameFunction) {
        super(x, y, width, height, Component.empty());
        this.id = id;
        this.currentValue = currentValue;
        this.displayNameFunction = displayNameFunction;

        this.textField = ScreenUtils.createTextField(Minecraft.getInstance().font, width, height, displayNameFunction.apply(currentValue), "", null);
        this.textField.setEditable(false);
        this.textField.setMaxLength(200);

        this.entryListWidget = new DropdownEntryList<T>(Minecraft.getInstance(), width, height, y, itemHeight);

        this.entryListWidget.clearEntries();
        for (T type : getValues())
            this.entryListWidget.addEntry(new DropdownEntry<>(type, displayNameFunction.apply(type), this::setCurrentValue));

        WIDGETS.add(this);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        this.textField.setValue(this.displayNameFunction.apply(currentValue));
        this.textField.setSize(this.width, this.height);
        this.textField.setPosition(this.getX(), this.getY());
        this.textField.extractRenderState(context, mouseX, mouseY, deltaTicks);

        if (this.expanded) {
            this.entryListWidget.setWidth(this.width - 1 + 6);
            this.entryListWidget.setHeight(Math.min(100, getValues().size() * itemHeight + (getValues().size() - 1) * 2));

            if (this.getY() > Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2) {
                ScreenUtils.LAST_RENDERS.add(() -> context.fill(this.getX(), this.getY() - this.entryListWidget.getHeight() + 3, this.getX() + this.entryListWidget.getWidth() + 1, this.getY() + 1, OUTLINE_COLOR));
                this.entryListWidget.setPosition(this.getX(), this.getY() - this.entryListWidget.getHeight() + 2);
            }
            else {
                ScreenUtils.LAST_RENDERS.add(() -> context.fill(this.getX(), this.getY() + 18, this.getX() + this.entryListWidget.getWidth() + 1, this.getY() + 16 + this.entryListWidget.getHeight(), OUTLINE_COLOR));
                this.entryListWidget.setPosition(this.getX(), this.getY() + 17);
            }

            ScreenUtils.LAST_RENDERS.add(() -> this.entryListWidget.extractRenderState(context, mouseX, mouseY, deltaTicks));
        }
    }

    private void setCurrentValue(T value) {
        this.currentValue = value;
        this.textField.setValue(this.displayNameFunction.apply(value));

        if (this.onChangeListener != null)
            this.onChangeListener.accept(value);

        this.expanded = false;
    }

    private List<T> getValues() {
        return Arrays.asList(currentValue.getDeclaringClass().getEnumConstants());
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (this.textField.mouseClicked(click, doubled)) {
            this.expanded = !this.expanded;
            return true;
        }
        else if (this.expanded && this.entryListWidget.mouseClicked(click, doubled)) return true;
        else this.expanded = false;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.expanded && this.entryListWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
            return true;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) { }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EnumDropdownWidget<?> widget)) return false;
        if (!currentValue.getClass().equals(widget.currentValue.getClass())) return false;
        return this.id.equals(widget.id);
    }

    public final class DropdownEntryList<T extends Enum<T>> extends AbstractSelectionList<DropdownEntry<T>> implements ExtraHoveredIgnored {

        public DropdownEntryList(Minecraft client, int width, int height, int y, int itemHeight) {
            super(client, width, height, y, itemHeight);
            this.centerListVertically = false;
        }

        @Override
        protected int scrollBarX() {
            return this.getX() + this.getWidth() - 6;
        }

        @Override
        public int getRowWidth() { return width; }

        protected void enableScissor(GuiGraphicsExtractor context) {
            context.enableScissor(this.getX(), this.getY() + 2, this.getRight(), this.getBottom() - 2);
        }

        @Override
        protected void extractScrollbar(GuiGraphicsExtractor context, int mouseX, int mouseY) {
            enableScissor(context);
            super.extractScrollbar(context, mouseX, mouseY);
            context.disableScissor();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) { }

        @Override
        public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
            for (DropdownEntry<T> dropdownEntry : this.children())
                dropdownEntry.setFocused(false);
            super.extractWidgetRenderState(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor context) { }

        @Override
        protected void extractListBackground(GuiGraphicsExtractor context) { }
    }

    public final class DropdownEntry<T extends Enum<T>> extends AbstractSelectionList.Entry<DropdownEntry<T>> implements CustomHeightEntry, ExtraHoveredIgnored {

        T value;
        String display;
        Consumer<T> onChangeListener;

        private DropdownEntry(T value, String display, Consumer<T> onChangeListener) {
            this.value = value;
            this.display = display;
            this.onChangeListener = onChangeListener;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            this.onChangeListener.accept(this.value);
            return true;
        }

        @Override
        public int getItemHeight() {
            return itemHeight;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            context.fill(getX() - 1 , getY() - 2, getX() + getWidth() - 1, getY() + getHeight() + 2, hovered ? Color.DARK_GRAY.getRGB() : Color.BLACK.getRGB());
            context.text(Minecraft.getInstance().font, Component.literal(display),
                    getX() + 2, getY() + getHeight() / 2 - Minecraft.getInstance().font.lineHeight / 2, 0xFFFFFFFF);
        }
    }

}
