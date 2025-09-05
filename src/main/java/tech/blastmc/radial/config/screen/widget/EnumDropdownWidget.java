package tech.blastmc.radial.config.screen.widget;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
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

public class EnumDropdownWidget<T extends Enum<T>> extends ClickableWidget implements HasId {

    public static List<EnumDropdownWidget<?>> WIDGETS = new ArrayList<>();

    @Getter
    private final String id;

    @Getter
    private T currentValue;
    @Setter
    private Consumer<T> onChangeListener;

    private final Function<T, String> displayNameFunction;

    private final TextFieldWidget textField;
    private final DropdownEntryList<T> entryListWidget;

    @Getter @Setter
    private boolean expanded = false;
    private final int itemHeight = 16;

    private static final int OUTLINE_COLOR = new Color(160, 160, 160, 255).getRGB();

    public EnumDropdownWidget(String id, int x, int y, int width, int height, @NonNull T currentValue, Function<T, String> displayNameFunction) {
        super(x, y, width, height, Text.empty());
        this.id = id;
        this.currentValue = currentValue;
        this.displayNameFunction = displayNameFunction;

        this.textField = ScreenUtils.createTextField(MinecraftClient.getInstance().textRenderer, width, height, displayNameFunction.apply(currentValue), "", null);
        this.textField.setEditable(false);
        this.textField.setMaxLength(200);

        this.entryListWidget = new DropdownEntryList<T>(MinecraftClient.getInstance(), width, height, y, itemHeight);

        this.entryListWidget.clearEntries();
        for (T type : getValues())
            this.entryListWidget.addEntry(new DropdownEntry<>(type, displayNameFunction.apply(type), this::setCurrentValue));

        WIDGETS.add(this);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.textField.setText(this.displayNameFunction.apply(currentValue));
        this.textField.setDimensions(this.width, this.height);
        this.textField.setPosition(this.getX(), this.getY());
        this.textField.render(context, mouseX, mouseY, deltaTicks);

        if (this.expanded) {
            this.entryListWidget.setWidth(this.width - 1 + 6);
            this.entryListWidget.setHeight(Math.min(100, getValues().size() * itemHeight + (getValues().size() - 1) * 2));

            if (this.getY() > MinecraftClient.getInstance().getWindow().getScaledHeight() / 2) {
                ScreenUtils.LAST_RENDERS.add(() -> context.fill(this.getX(), this.getY() - this.entryListWidget.getHeight() + 3, this.getX() + this.entryListWidget.getWidth() + 1, this.getY() + 1, OUTLINE_COLOR));
                this.entryListWidget.setPosition(this.getX(), this.getY() - this.entryListWidget.getHeight() + 2);
            }
            else {
                ScreenUtils.LAST_RENDERS.add(() -> context.fill(this.getX(), this.getY() + 18, this.getX() + this.entryListWidget.getWidth() + 1, this.getY() + 16 + this.entryListWidget.getHeight(), OUTLINE_COLOR));
                this.entryListWidget.setPosition(this.getX(), this.getY() + 17);
            }

            ScreenUtils.LAST_RENDERS.add(() -> this.entryListWidget.render(context, mouseX, mouseY, deltaTicks));
        }
    }

    private void setCurrentValue(T value) {
        this.currentValue = value;
        this.textField.setText(this.displayNameFunction.apply(value));

        if (this.onChangeListener != null)
            this.onChangeListener.accept(value);

        this.expanded = false;
    }

    private List<T> getValues() {
        return Arrays.asList(currentValue.getDeclaringClass().getEnumConstants());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.textField.mouseClicked(mouseX, mouseY, button)) {
            this.expanded = !this.expanded;
            return true;
        }
        else if (this.expanded && this.entryListWidget.mouseClicked(mouseX, mouseY, button)) return true;
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
    protected void appendClickableNarrations(NarrationMessageBuilder builder) { }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EnumDropdownWidget<?> widget)) return false;
        if (!currentValue.getClass().equals(widget.currentValue.getClass())) return false;
        return this.id.equals(widget.id);
    }

    public final class DropdownEntryList<T extends Enum<T>> extends EntryListWidget<DropdownEntry<T>> implements ExtraHoveredIgnored {

        public DropdownEntryList(MinecraftClient client, int width, int height, int y, int itemHeight) {
            super(client, width, height, y, itemHeight);
            this.headerHeight = 0;
        }

        @Override
        protected int getScrollbarX() {
            return this.getX() + this.getWidth() - 6;
        }

        @Override
        public int getRowWidth() { return width; }

        protected void enableScissor(DrawContext context) {
            context.enableScissor(this.getX(), this.getY() + 2, this.getRight(), this.getBottom() - 2);
        }

        @Override
        protected void drawScrollbar(DrawContext context) {
            enableScissor(context);
            super.drawScrollbar(context);
            context.disableScissor();
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) { }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            for (DropdownEntry<T> dropdownEntry : this.children())
                dropdownEntry.setFocused(false);
            super.renderWidget(context, mouseX, mouseY, deltaTicks);
        }

        @Override
        protected void drawHeaderAndFooterSeparators(DrawContext context) { }

        @Override
        protected void drawMenuListBackground(DrawContext context) { }
    }

    public final class DropdownEntry<T extends Enum<T>> extends EntryListWidget.Entry<DropdownEntry<T>> implements CustomHeightEntry, ExtraHoveredIgnored {

        T value;
        String display;
        Consumer<T> onChangeListener;

        private DropdownEntry(T value, String display, Consumer<T> onChangeListener) {
            this.value = value;
            this.display = display;
            this.onChangeListener = onChangeListener;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            context.fill(x - 1 , y - 2, x + entryWidth - 1, y + entryHeight + 2, hovered ? Color.DARK_GRAY.getRGB() : Color.BLACK.getRGB());
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(display),
                    x + 2, y + entryHeight / 2 - MinecraftClient.getInstance().textRenderer.fontHeight / 2, 0xFFFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.onChangeListener.accept(this.value);
            return true;
        }

        @Override
        public int getItemHeight() {
            return itemHeight;
        }
    }

}
