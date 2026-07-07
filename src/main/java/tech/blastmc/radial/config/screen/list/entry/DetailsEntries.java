package tech.blastmc.radial.config.screen.list.entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
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
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            context.centeredText(Minecraft.getInstance().font, "Display Settings",
                    getX() + getWidth() / 2, 2 + (getY() + getItemHeight() / 2) - Minecraft.getInstance().font.lineHeight / 2, 0xFFFFFFFF);
        }

        @Override
        public int getItemHeight() {
            return  Minecraft.getInstance().font.lineHeight + 4;
        }
    }

    public static class DetailsNameEntry extends HasTextFieldEntry implements CustomHeightEntry {

        ToggleSwitchWidget toggleButton;

        public DetailsNameEntry(RadialOption option) {
            this.textField = ScreenUtils.createTextField(Minecraft.getInstance().font, 100, 20, option.getName(), "Name", option::setName);
            this.textField.setMaxLength(32);

            this.toggleButton = new ToggleSwitchWidget(0, 0, 40, 20, option.isEnabled());
            this.toggleButton.setTooltipDelay(Duration.ofMillis(250));
            this.toggleButton.setOnChangeListener(option::setEnabled);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            textField.setWidth(getWidth() - 42);
            textField.setPosition(getX(), getY() + 2);
            textField.extractRenderState(context, mouseX, mouseY, tickProgress);

            toggleButton.setPosition(getX() + getWidth() - 40, getY() + 2);
            toggleButton.extractRenderState(context, mouseX, mouseY, tickProgress);
        }

        @Override
        public int getItemHeight() {
            return 24;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            if (toggleButton.mouseClicked(click, doubled)) return true;
            return super.mouseClicked(click, doubled);
        }
    }

    public static class IconEntry extends ListEntry implements CustomHeightEntry {

        private final RadialOption option;

        public IconEntry(RadialOption option) {
            this.option = option;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            context.text(Minecraft.getInstance().font, "Icon",
                    getX(), getY() + getItemHeight() - Minecraft.getInstance().font.lineHeight / 2,  0xFFFFFFFF);

            context.item(option.getIcon(), getX() + getWidth() - 16, getY() + getItemHeight() - 10);
        }

        @Override
        public int getItemHeight() {
            return 22;
        }
    }

    public static class MaterialEntry extends HasTextFieldEntry implements CustomHeightEntry {

        public MaterialEntry(RadialOption option) {
            textField = ScreenUtils.createTextField(Minecraft.getInstance().font, 100, 20, option.getMaterial(), "Material", input -> {
                if (input.equals(option.getMaterial()))
                    return;
                option.setMaterial(input);
                option.clearCachedIcon();
            });
            textField.setMaxLength(255);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            textField.setWidth(getWidth());
            textField.setPosition(getX(), getY() + 4);
            textField.extractRenderState(context, mouseX, mouseY, tickProgress);
        }

        @Override
        public int getItemHeight() {
            return 24;
        }
    }

    public static class ItemModelEntry extends HasTextFieldEntry implements CustomHeightEntry {

        public ItemModelEntry(RadialOption option) {
            textField = ScreenUtils.createTextField(Minecraft.getInstance().font, 100, 20,
                    option.getItemModel(), "Item Model", "This is the 'item_model' component, which allows for custom items (optional)", input -> {
                        if (input.equals(option.getItemModel()))
                            return;
                        option.setItemModel(input);
                        option.clearCachedIcon();
                    });
            textField.setMaxLength(255);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            textField.setWidth(getWidth());
            textField.setPosition(getX(), getY() + 4);
            textField.extractRenderState(context, mouseX, mouseY, tickProgress);
        }

        @Override
        public int getItemHeight() {
            return 22;
        }
    }

    public static class IconMiscOptionsEntry extends HasTextFieldEntry implements CustomHeightEntry {

        private final RadialOption option;
        public EditBox textField2;
        private Button enchantedButton;

        public IconMiscOptionsEntry(RadialOption option) {
            this.option = option;

            enchantedButton = Button.builder(Component.literal("Enchanted: Off"),
                            b -> {
                                option.setEnchanted(!option.isEnchanted());
                                option.clearCachedIcon();
                            })
                    .bounds(0, 0, 100, 20).build();

            textField = ScreenUtils.createTextField(Minecraft.getInstance().font, 100, 20,
                    option.getSkullOwner(), "Skull Owner", input -> {
                        if (input.equals(option.getSkullOwner()))
                            return;
                        option.setSkullOwner(input);
                    });
            textField.setMaxLength(16);

            textField2 = ScreenUtils.createTextField(Minecraft.getInstance().font, 100, 20,
                    option.getRgb(), "Dye Color", "The RGB value of the color (255,255,255)",input -> {
                        if (input.equals(option.getRgb()))
                            return;
                        option.setRgb(input);
                        option.clearCachedIcon();
                    });
            textField2.setMaxLength(11);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            enchantedButton.setWidth(getWidth() / 2 - 1);
            textField.setWidth(getWidth() / 2 - 1);
            textField2.setWidth(getWidth() / 2 - 1);

            textField2.setPosition(getX()  + (getWidth() / 2) + 1, getY() + 2);

            if (option.getMaterial().endsWith("player_head")) {
                textField.visible = true;
                textField.setCanLoseFocus(true);
                textField.setPosition(getX(), getY() + 2);
                enchantedButton.setPosition(0, 0);
                if (!option.isSkullOwnerProcessed() && Util.getMillis() - option.getSkullOwnerLastUpdate() > 200) {
                    option.setSkullOwnerProcessed(true);
                    option.clearCachedIcon();
                }
                textField.extractRenderState(context, mouseX, mouseY, tickProgress);
            }
            else {
                textField.visible = false;
                textField.setFocused(false);
                textField.setCanLoseFocus(false);
                textField.setPosition(0, 0);
                enchantedButton.setPosition(getX(), getY() + 2);
                enchantedButton.setMessage(option.isEnchanted() ? Component.literal("Enchanted: On") : Component.literal("Enchanted: Off"));
                enchantedButton.extractRenderState(context, mouseX, mouseY, tickProgress);
            }

            if (option.isDyeable())
                textField2.extractRenderState(context, mouseX, mouseY, tickProgress);
        }

        @Override
        public int getItemHeight() {
            return 22;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            if (enchantedButton.mouseClicked(click, doubled))
                return true;
            if (textField.mouseClicked(click, doubled)) {
                textField.setFocused(true);
                textField2.setFocused(false);
                return true;
            }
            else
                textField.setFocused(false);
            if (textField2.mouseClicked(click, doubled)) {
                textField2.setFocused(true);
                textField.setFocused(false);
                return true;
            }
            else
                textField2.setFocused(false);
            return false;
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent click) {
            enchantedButton.mouseReleased(click);
            textField.mouseReleased(click);
            textField2.mouseReleased(click);
            return super.mouseReleased(click);
        }

        @Override
        public boolean keyPressed(KeyEvent keyInput) {
            if (textField.keyPressed(keyInput)) return true;
            if (textField2.keyPressed(keyInput)) return true;
            return super.keyPressed(keyInput);
        }

        @Override
        public boolean keyReleased(KeyEvent keyInput) {
            if (textField.keyReleased(keyInput)) return true;
            if (textField2.keyReleased(keyInput)) return true;
            return super.keyReleased(keyInput);
        }

        @Override
        public boolean charTyped(CharacterEvent input) {
            if (textField.charTyped(input)) return true;
            if (textField2.charTyped(input)) return true;
            return super.charTyped(input);
        }

    }

    public static class VisibilityLabelEntry extends ListEntry implements CustomHeightEntry {

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            context.text(Minecraft.getInstance().font, "Visibility",
                    getX(), getY() + getItemHeight() - Minecraft.getInstance().font.lineHeight / 2,  0xFFFFFFFF);
        }

        @Override
        public int getItemHeight() {
            return 22;
        }
    }

    public static class VisibilityModeEntry extends ListEntry implements CustomHeightEntry {

        private Button button;

        public VisibilityModeEntry(RadialOption option, Runnable rebuildCallback) {
            this.button = Button.builder(
                    Component.literal(option.isConditional() ? "Mode: Conditional" : "Mode: Always"), button -> {
                        option.setConditional(!option.isConditional());
                        button.setMessage(Component.literal(option.isConditional() ? "Mode: Conditional" : "Mode: Always"));

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
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            button.setWidth(getWidth());
            button.setPosition(getX(), getY() + 4);
            button.extractRenderState(context, mouseX, mouseY, tickProgress);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            return this.button.mouseClicked(click, doubled);
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
        private final Button deleteButton;

        public ConditionalRuleEntry(int id, RadialOption option, Runnable rebuildCallback) {
            this.option = option;
            this.id = id;

            ConditionalConfig config = option.getRules().get(id);

            this.textField = ScreenUtils.createTextField(Minecraft.getInstance().font, 100, 20,
                    config.getValue() == null ? "" : config.getValue(), "Value", config::setValue);
            this.textField.setTooltip(Tooltip.create(Component.literal("Use a comma (,) to separate multiple values")));
            this.textField.setTooltipDelay(Duration.ofMillis(250));

            this.widget = new EnumDropdownWidget<>("conditional-rule-" + id,0, 0, 100, 20, config.getType(), ConditionalityRule::getDisplay);
            this.widget.setOnChangeListener(rule -> {
                ConditionalityRule oldRule = config.getType();
                if (oldRule.getType() == null || !oldRule.getType().equals(rule.getType())) {
                    System.out.println("Clearing value");
                    config.setValue(null);
                    this.textField.setValue("");
                }
                config.setType(rule);
            });
            this.deleteButton = Button.builder(Component.literal("✕"), button -> {
                option.getRules().remove(id);
                if (option.getRules().isEmpty())
                    option.getRules().add(new ConditionalConfig(ConditionalityRule.IS_MULTIPLAYER, null));

                rebuildCallback.run();
            }).bounds(0, 0, 20, 20).build();
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            this.textField.setWidth(getWidth() / 2 - 2 - 20);
            this.textField.setPosition(getX()  + (getWidth() / 2) + 1, getY() + 4);
            if (option.getRules().get(id).getType().hasValue())
                this.textField.extractRenderState(context, mouseX, mouseY, tickProgress);

            this.deleteButton.setPosition(getX() + getWidth() - 20, getY() + 4);
            this.deleteButton.extractRenderState(context, mouseX, mouseY, tickProgress);

            this.widget.setPosition(getX(), getY() + 4);
            this.widget.setSize(getWidth() / 2 - 1, 20);
            this.widget.extractRenderState(context, mouseX, mouseY, tickProgress);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            if (this.widget.mouseClicked(click, doubled)) return true;
            if (this.deleteButton.mouseClicked(click, doubled)) return true;
            return super.mouseClicked(click, doubled);
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
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickProgress) {}

        @Override
        public int getItemHeight() {
            return height;
        }
    }

}
