package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import tech.blastmc.radial.config.screen.widget.EnumDropdownWidget;
import tech.blastmc.radial.config.screen.widget.ToggleSwitchWidget;
import tech.blastmc.radial.macros.RadialOption;
import tech.blastmc.radial.macros.condition.ConditionalConfig;
import tech.blastmc.radial.macros.condition.ConditionalityRule;
import tech.blastmc.radial.util.ScreenUtils;

import java.time.Duration;
import java.util.ArrayList;

public class DetailsEntries {

    public static class DetailsLabelEntry extends ListEntry implements CustomHeightEntry {

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, "Display Settings",
                    x + entryWidth / 2, 2 + (y + entryHeight / 2) - MinecraftClient.getInstance().textRenderer.fontHeight / 2, 0xFFFFFFFF);
        }

        @Override
        public int getItemHeight() {
            return  MinecraftClient.getInstance().textRenderer.fontHeight + 4;
        }
    }

    public static class DetailsNameEntry extends HasTextFieldEntry implements CustomHeightEntry {

        ToggleSwitchWidget toggleButton;

        public DetailsNameEntry(RadialOption option) {
            this.textField = ScreenUtils.createTextField(MinecraftClient.getInstance().textRenderer, 100, 20, option.getName(), "Name", option::setName);
            this.textField.setMaxLength(32);

            this.toggleButton = new ToggleSwitchWidget(0, 0, 40, 20, option.isEnabled());
            this.toggleButton.setTooltipDelay(Duration.ofMillis(250));
            this.toggleButton.setOnChangeListener(option::setEnabled);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            textField.setWidth(entryWidth - 42);
            textField.setPosition(x, y + 2);
            textField.render(context, mouseX, mouseY, tickProgress);

            toggleButton.setPosition(x + entryWidth - 40, y + 2);
            toggleButton.render(context, mouseX, mouseY, tickProgress);
        }

        @Override
        public int getItemHeight() {
            return 24;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (toggleButton.mouseClicked(mouseX, mouseY, button)) return true;
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }

    public static class IconEntry extends ListEntry implements CustomHeightEntry {

        private final RadialOption option;

        public IconEntry(RadialOption option) {
            this.option = option;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "Icon",
                    x, y + entryHeight - MinecraftClient.getInstance().textRenderer.fontHeight / 2,  0xFFFFFFFF);

            context.drawItem(option.getIcon(), x + entryWidth - 16, y + entryHeight - 10);
        }

        @Override
        public int getItemHeight() {
            return 22;
        }
    }

    public static class MaterialEntry extends HasTextFieldEntry implements CustomHeightEntry {

        public MaterialEntry(RadialOption option) {
            textField = ScreenUtils.createTextField(MinecraftClient.getInstance().textRenderer, 100, 20, option.getMaterial(), "Material", input -> {
                if (input.equals(option.getMaterial()))
                    return;
                option.setMaterial(input);
                option.clearCachedIcon();
            });
            textField.setMaxLength(255);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            textField.setWidth(entryWidth);
            textField.setPosition(x, y + 4);
            textField.render(context, mouseX, mouseY, tickProgress);
        }

        @Override
        public int getItemHeight() {
            return 24;
        }
    }

    public static class ItemModelEntry extends HasTextFieldEntry implements CustomHeightEntry {

        public ItemModelEntry(RadialOption option) {
            textField = ScreenUtils.createTextField(MinecraftClient.getInstance().textRenderer, 100, 20,
                    option.getItemModel(), "Item Model", "This is the 'item_model' component, which allows for custom items (optional)", input -> {
                        if (input.equals(option.getItemModel()))
                            return;
                        option.setItemModel(input);
                        option.clearCachedIcon();
                    });
            textField.setMaxLength(255);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            textField.setWidth(entryWidth);
            textField.setPosition(x, y + 4);
            textField.render(context, mouseX, mouseY, tickProgress);
        }

        @Override
        public int getItemHeight() {
            return 22;
        }
    }

    public static class IconMiscOptionsEntry extends HasTextFieldEntry implements CustomHeightEntry {

        private final RadialOption option;
        public TextFieldWidget textField2;
        private ButtonWidget enchantedButton;

        public IconMiscOptionsEntry(RadialOption option) {
            this.option = option;

            enchantedButton = ButtonWidget.builder(Text.literal("Enchanted: Off"),
                            b -> {
                                option.setEnchanted(!option.isEnchanted());
                                option.clearCachedIcon();
                            })
                    .dimensions(0, 0, 100, 20).build();

            textField = ScreenUtils.createTextField(MinecraftClient.getInstance().textRenderer, 100, 20,
                    option.getSkullOwner(), "Skull Owner", input -> {
                        if (input.equals(option.getSkullOwner()))
                            return;
                        option.setSkullOwner(input);
                    });
            textField.setMaxLength(16);

            textField2 = ScreenUtils.createTextField(MinecraftClient.getInstance().textRenderer, 100, 20,
                    option.getRgb(), "Dye Color", "The RGB value of the color (255,255,255)",input -> {
                        if (input.equals(option.getRgb()))
                            return;
                        option.setRgb(input);
                        option.clearCachedIcon();
                    });
            textField2.setMaxLength(11);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            enchantedButton.setWidth(entryWidth / 2 - 1);
            textField.setWidth(entryWidth / 2 - 1);
            textField2.setWidth(entryWidth / 2 - 1);

            textField2.setPosition(x  + (entryWidth / 2) + 1, y + 2);

            if (option.getMaterial().endsWith("player_head")) {
                textField.visible = true;
                textField.setFocusUnlocked(true);
                textField.setPosition(x, y + 2);
                enchantedButton.setPosition(0, 0);
                if (!option.isSkullOwnerProcessed() && Util.getMeasuringTimeMs() - option.getSkullOwnerLastUpdate() > 200) {
                    option.setSkullOwnerProcessed(true);
                    option.clearCachedIcon();
                }
                textField.render(context, mouseX, mouseY, tickProgress);
            }
            else {
                textField.visible = false;
                textField.setFocused(false);
                textField.setFocusUnlocked(false);
                textField.setPosition(0, 0);
                enchantedButton.setPosition(x, y + 2);
                enchantedButton.setMessage(option.isEnchanted() ? Text.literal("Enchanted: On") : Text.literal("Enchanted: Off"));
                enchantedButton.render(context, mouseX, mouseY, tickProgress);
            }

            if (option.isDyeable())
                textField2.render(context, mouseX, mouseY, tickProgress);
        }

        @Override
        public int getItemHeight() {
            return 22;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (enchantedButton.mouseClicked(mouseX, mouseY, button))
                return true;
            if (textField.mouseClicked(mouseX, mouseY, button)) {
                textField.setFocused(true);
                textField2.setFocused(false);
                return true;
            }
            else
                textField.setFocused(false);
            if (textField2.mouseClicked(mouseX, mouseY, button)) {
                textField2.setFocused(true);
                textField.setFocused(false);
                return true;
            }
            else
                textField2.setFocused(false);
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            enchantedButton.mouseReleased(mouseX, mouseY, button);
            textField.mouseReleased(mouseX, mouseY, button);
            textField2.mouseReleased(mouseX, mouseY, button);
            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (textField.keyPressed(keyCode, scanCode, modifiers)) return true;
            if (textField2.keyPressed(keyCode, scanCode, modifiers)) return true;
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            if (textField.keyReleased(keyCode, scanCode, modifiers)) return true;
            if (textField2.keyReleased(keyCode, scanCode, modifiers)) return true;
            return super.keyReleased(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            if (textField.charTyped(chr, modifiers)) return true;
            if (textField2.charTyped(chr, modifiers)) return true;
            return super.charTyped(chr, modifiers);
        }

    }

    public static class VisibilityLabelEntry extends ListEntry implements CustomHeightEntry {

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "Visibility",
                    x, y + entryHeight - MinecraftClient.getInstance().textRenderer.fontHeight / 2,  0xFFFFFFFF);
        }

        @Override
        public int getItemHeight() {
            return 22;
        }
    }

    public static class VisibilityModeEntry extends ListEntry implements CustomHeightEntry {

        private ButtonWidget button;

        public VisibilityModeEntry(RadialOption option, Runnable rebuildCallback) {
            this.button = ButtonWidget.builder(
                    Text.literal(option.isConditional() ? "Mode: Conditional" : "Mode: Always"), button -> {
                        option.setConditional(!option.isConditional());
                        button.setMessage(Text.literal(option.isConditional() ? "Mode: Conditional" : "Mode: Always"));

                        if (option.isConditional())
                            option.setRules(new ArrayList<>() {{
                                add(new ConditionalConfig(ConditionalityRule.IS_MULTIPLAYER, null));
                            }});
                        else
                            option.setRules(null);

                        rebuildCallback.run();
                    }
            ).build();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            button.setWidth(entryWidth);
            button.setPosition(x, y + 4);
            button.render(context, mouseX, mouseY, tickProgress);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.button.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public int getItemHeight() {
            return 22;
        }
    }

    public static class ConditionalRuleEntry extends HasTextFieldEntry implements CustomHeightEntry {

        private final RadialOption option;
        private final int id;
        public final EnumDropdownWidget<ConditionalityRule> widget;
        private final ButtonWidget deleteButton;

        public ConditionalRuleEntry(int id, RadialOption option, Runnable rebuildCallback) {
            this.option = option;
            this.id = id;

            ConditionalConfig config = option.getRules().get(id);

            this.textField = ScreenUtils.createTextField(MinecraftClient.getInstance().textRenderer, 100, 20,
                    config.getValue() == null ? "" : config.getValue(), "Value", config::setValue);
            this.textField.setTooltip(Tooltip.of(Text.literal("Use a comma (,) to separate multiple values")));
            this.textField.setTooltipDelay(Duration.ofMillis(250));

            this.widget = new EnumDropdownWidget<>("conditional-rule-" + id,0, 0, 100, 20, config.getType(), ConditionalityRule::getDisplay);
            this.widget.setOnChangeListener(rule -> {
                ConditionalityRule oldRule = config.getType();
                if (oldRule.getType() == null || !oldRule.getType().equals(rule.getType())) {
                    System.out.println("Clearing value");
                    config.setValue(null);
                    this.textField.setText("");
                }
                config.setType(rule);
            });
            this.deleteButton = ButtonWidget.builder(Text.literal("✕"), button -> {
                option.getRules().remove(id);
                if (option.getRules().isEmpty())
                    option.getRules().add(new ConditionalConfig(ConditionalityRule.IS_MULTIPLAYER, null));

                rebuildCallback.run();
            }).dimensions(0, 0, 20, 20).build();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            this.textField.setWidth(entryWidth / 2 - 2 - 20);
            this.textField.setPosition(x  + (entryWidth / 2) + 1, y + 4);
            if (option.getRules().get(id).getType().hasValue())
                this.textField.render(context, mouseX, mouseY, tickProgress);

            this.deleteButton.setPosition(x + entryWidth - 20, y + 4);
            this.deleteButton.render(context, mouseX, mouseY, tickProgress);

            this.widget.setPosition(x, y + 4);
            this.widget.setDimensions(entryWidth / 2 - 1, 20);
            this.widget.render(context, mouseX, mouseY, tickProgress);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.widget.mouseClicked(mouseX, mouseY, button)) return true;
            if (this.deleteButton.mouseClicked(mouseX, mouseY, button)) return true;
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (this.widget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        @Override
        public int getItemHeight() {
            return option.getRules().size() == id + 1 ? 24 : 22;
        }
    }

    public static class SpacerEntry extends ListEntry implements CustomHeightEntry {

        private final int height;

        public SpacerEntry(int height) {
            this.height = height;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {}

        @Override
        public int getItemHeight() {
            return height;
        }
    }

}
