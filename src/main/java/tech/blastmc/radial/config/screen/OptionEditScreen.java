package tech.blastmc.radial.config.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import tech.blastmc.radial.config.screen.list.CommandList;
import tech.blastmc.radial.config.screen.list.entry.AddEntryEntry;
import tech.blastmc.radial.config.screen.list.entry.CommandEntry;
import tech.blastmc.radial.macros.RadialGroup;
import tech.blastmc.radial.macros.RadialOption;
import tech.blastmc.radial.util.ScreenUtils;

import java.util.ArrayList;

public class OptionEditScreen extends Screen {

    private static final Identifier MENU_LIST_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/inworld_menu_list_background.png");

    private final Screen parent;
    private final int index;
    private final RadialGroup group;
    private final RadialOption option;

    private static final int TOP_BAR_H = 28;
    private static final int BOTTOM_BAR_H = 28;

    private TextFieldWidget nameField;
    private TextFieldWidget materialField;
    private TextFieldWidget rgbField;
    private TextFieldWidget skullOwnerField;
    private TextFieldWidget modelField;
    private ButtonWidget enchantedButton;

    public CommandList commandsList;

    public OptionEditScreen(Screen parent, RadialGroup group, int index) {
        super(Text.literal("RadialMacros - Option Edit"));
        this.parent = parent;
        this.index = index;
        this.group = group;

        if (this.index == -1)
            this.option = new RadialOption("Macro", new ItemStack(Items.GRASS_BLOCK), new ArrayList<>());
        else
            this.option = group.getOptions().get(index).clone();
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> {
                    commit();
                    close();
                })
                .dimensions(this.width / 2 - 100, this.height - 24, 200, 20).build());

        nameField = ScreenUtils.createTextField(textRenderer, width / 2 - 16, 20, option.getName(), "Name", option::setName);
        nameField.setMaxLength(32);
        addSelectableChild(nameField);

        materialField = ScreenUtils.createTextField(textRenderer, width / 2 - 16, 20, option.getMaterial(), "Material", input -> {
            if (input.equals(option.getMaterial()))
                return;
            option.setMaterial(input);
            option.clearCachedIcon();
        });
        materialField.setMaxLength(255);
        addSelectableChild(materialField);

        rgbField = ScreenUtils.createTextField(textRenderer, (width / 2 - 16) / 2 - 2, 20,
                option.getRgb(), "Dye Color", "The RGB value of the color (255,255,255)",input -> {
                    if (input.equals(option.getRgb()))
                        return;
                    option.setRgb(input);
                    option.clearCachedIcon();
                });
        rgbField.setMaxLength(11);
        addSelectableChild(rgbField);

        skullOwnerField = ScreenUtils.createTextField(textRenderer, (width / 2 - 16) / 2 - 2, 20,
                option.getSkullOwner(), "Skull Owner", input -> {
                    if (input.equals(option.getSkullOwner()))
                        return;
                    option.setSkullOwner(input);
                });
        skullOwnerField.setMaxLength(16);
        addSelectableChild(skullOwnerField);

        modelField = ScreenUtils.createTextField(textRenderer, width / 2 - 16, 20,
                option.getItemModel(), "Item Model", input -> {
                    if (input.equals(option.getItemModel()))
                        return;
                    option.setItemModel(input);
                    option.clearCachedIcon();
                });
        modelField.setMaxLength(255);
        addSelectableChild(modelField);

        enchantedButton = ButtonWidget.builder(Text.literal("Enchanted: Off"),
                        b -> {
                            option.setEnchanted(!option.isEnchanted());
                            option.clearCachedIcon();
                        })
                .dimensions(0, 0, (width / 2 - 16) / 2 - 2, 20).build();
        addSelectableChild(enchantedButton);

        commandsList = addDrawableChild(new CommandList(this.client, width / 2, height - TOP_BAR_H - BOTTOM_BAR_H, TOP_BAR_H, 28));
        commandsList.setPosition(width / 2 + 1, TOP_BAR_H);
        rebuildList();
    }

    private void rebuildList() {
        commandsList.clearEntries();
        for (int i = 0; i < option.getCommands().size(); i++) {
            commandsList.addEntry(new CommandEntry(this, i, option.getCommands()));
        }
        commandsList.addEntry(new AddEntryEntry("Add Command", () -> {
            option.getCommands().add("");
            this.client.setScreen(this);
            commandsList.setScrollY(commandsList.getMaxScrollY());
        }));
    }

    @Override
    public void resize(MinecraftClient client, int w, int h) {
        super.resize(client, w, h);
        init(client, w, h);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        drawSeperators(context, 0, TOP_BAR_H, height - BOTTOM_BAR_H, width / 2 + 1);
        drawBackground(context, 0, TOP_BAR_H, width / 2 + 1, height - BOTTOM_BAR_H - TOP_BAR_H);

        context.drawCenteredTextWithShadow(textRenderer, "Edit Macro", width / 2, 28 / 2 - textRenderer.fontHeight / 2, 0xFFFFFFFF);
        renderDisplaySettings(context, mouseX, mouseY, deltaTicks);

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    public void renderDisplaySettings(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawCenteredTextWithShadow(textRenderer, "Display Settings", width / 2 / 2, TOP_BAR_H + 10 - textRenderer.fontHeight / 2, 0xFFFFFFFF);

        nameField.setPosition(8, TOP_BAR_H + 20);
        nameField.render(context, mouseX, mouseY, deltaTicks);

        context.drawTextWithShadow(textRenderer, "Icon", 8, TOP_BAR_H + 52, 0xFFFFFFFF);
        context.drawItem(option.getIcon(), width / 2 - 24, TOP_BAR_H + 48);

        materialField.setPosition(8, TOP_BAR_H + 66);
        materialField.render(context, mouseX, mouseY, deltaTicks);

        if (option.getMaterial().endsWith("player_head")) {
            skullOwnerField.visible = true;
            skullOwnerField.setPosition(8, TOP_BAR_H + 90);
            if (!option.isSkullOwnerProcessed() && Util.getMeasuringTimeMs() - option.getSkullOwnerLastUpdate() > 200) {
                option.setSkullOwnerProcessed(true);
                option.clearCachedIcon();
            }
            skullOwnerField.render(context, mouseX, mouseY, deltaTicks);
        }
        else {
            skullOwnerField.visible = false;
            enchantedButton.setMessage(option.isEnchanted() ? Text.literal("Enchanted: On") : Text.literal("Enchanted: Off"));
            enchantedButton.setPosition(8, TOP_BAR_H + 90);
            enchantedButton.render(context, mouseX, mouseY, deltaTicks);
        }

        if (option.isDyeable()) {
            rgbField.setPosition(width / 2 / 2 + 2, TOP_BAR_H + 90);
            rgbField.render(context, mouseX, mouseY, deltaTicks);
        }

        modelField.setPosition(8, TOP_BAR_H + 114);
        modelField.render(context, mouseX, mouseY, deltaTicks);
    }

    public void drawSeperators(DrawContext context, int x, int top, int bottom, int width) {
        Identifier identifier = this.client.world == null ? Screen.HEADER_SEPARATOR_TEXTURE : Screen.INWORLD_HEADER_SEPARATOR_TEXTURE;
        Identifier identifier2 = this.client.world == null ? Screen.FOOTER_SEPARATOR_TEXTURE : Screen.INWORLD_FOOTER_SEPARATOR_TEXTURE;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, identifier, x, top - 2, 0.0F, 0.0F, width, 2, 32, 2);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, identifier2, x, bottom, 0.0F, 0.0F, width, 2, 32, 2);
    }

    public void drawBackground(DrawContext context, int x, int y, int w, int h) {
        Identifier identifier = this.client.world == null ? MENU_LIST_BACKGROUND_TEXTURE : INWORLD_MENU_LIST_BACKGROUND_TEXTURE;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, identifier, x, y, 0, 0, w, h, 32, 32);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return commandsList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        commandsList.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (nameField.mouseReleased(mouseX, mouseY, button)) return true;
        if (materialField.mouseReleased(mouseX, mouseY, button)) return true;
        if (skullOwnerField.mouseReleased(mouseX, mouseY, button)) return true;
        if (enchantedButton.mouseReleased(mouseX, mouseY, button)) return true;
        if (rgbField.mouseReleased(mouseX, mouseY, button)) return true;
        if (modelField.mouseReleased(mouseX, mouseY, button)) return true;
        if (commandsList.mouseReleased(mouseX, mouseY, button)) return true;
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (materialField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (skullOwnerField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (enchantedButton.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (rgbField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (modelField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (commandsList.keyPressed(keyCode, scanCode, modifiers)) return true;
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (nameField.keyReleased(keyCode, scanCode, modifiers)) return true;
        if (materialField.keyReleased(keyCode, scanCode, modifiers)) return true;
        if (skullOwnerField.keyReleased(keyCode, scanCode, modifiers)) return true;
        if (enchantedButton.keyReleased(keyCode, scanCode, modifiers)) return true;
        if (rgbField.keyReleased(keyCode, scanCode, modifiers)) return true;
        if (modelField.keyReleased(keyCode, scanCode, modifiers)) return true;
        if (commandsList.keyReleased(keyCode, scanCode, modifiers)) return true;
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (nameField.charTyped(chr, modifiers)) return true;
        if (materialField.charTyped(chr, modifiers)) return true;
        if (skullOwnerField.charTyped(chr, modifiers)) return true;
        if (enchantedButton.charTyped(chr, modifiers)) return true;
        if (rgbField.charTyped(chr, modifiers)) return true;
        if (modelField.charTyped(chr, modifiers)) return true;
        if (commandsList.charTyped(chr, modifiers)) return true;
        return super.charTyped(chr, modifiers);
    }

    public void commit() {
        validate();

        if (index == -1)
            group.getOptions().add(option);
        else
            group.getOptions().set(index, option);
    }

    public void validate() {
        if (option.getName() == null || option.getName().isEmpty())
            if (index >= 0)
                option.setName(group.getOptions().get(index).getName());
            else
                option.setName("New Macro");

        if (option.getMaterial() == null || option.getMaterial().isEmpty())
            option.setMaterial("minecraft:grass_block");

        if (option.getMaterial().split(":").length == 1)
            option.setMaterial("minecraft:" + option.getMaterial());

        if (option.getItemModel() != null && !option.getItemModel().isEmpty())
            if (option.getItemModel().split(":").length == 1)
                option.setItemModel("minecraft:" + option.getItemModel());

        if (!option.getMaterial().equals("minecraft:player_head"))
            option.setSkullOwner(null);

        if (!option.isDyeable())
            option.setRgb(null);

        option.getCommands().removeIf(String::isEmpty);
    }

}
