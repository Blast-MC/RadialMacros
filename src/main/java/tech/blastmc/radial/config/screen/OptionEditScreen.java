package tech.blastmc.radial.config.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import tech.blastmc.radial.config.screen.list.HalfWidthList;
import tech.blastmc.radial.config.screen.list.HalfWidthList.CommandList;
import tech.blastmc.radial.config.screen.list.HalfWidthList.DetailsList;
import tech.blastmc.radial.config.screen.list.entry.AddEntryEntry;
import tech.blastmc.radial.config.screen.list.entry.CommandEntry;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.ConditionalRuleEntry;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.DetailsLabelEntry;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.DetailsNameEntry;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.IconEntry;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.IconMiscOptionsEntry;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.ItemModelEntry;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.MaterialEntry;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.VisibilityLabelEntry;
import tech.blastmc.radial.config.screen.list.entry.DetailsEntries.VisibilityModeEntry;
import tech.blastmc.radial.macros.RadialGroup;
import tech.blastmc.radial.macros.RadialOption;
import tech.blastmc.radial.macros.condition.ConditionalConfig;
import tech.blastmc.radial.macros.condition.ConditionalityRule;

import java.util.ArrayList;

public class OptionEditScreen extends Screen {

    private final Screen parent;
    private final int index;
    private final RadialGroup group;
    private final RadialOption option;

    private static final int TOP_BAR_H = 28;
    private static final int BOTTOM_BAR_H = 28;

    public Button doneButton;
    public HalfWidthList commandsList;
    public HalfWidthList detailsList;

    public OptionEditScreen(Screen parent, RadialGroup group, int index) {
        super(Component.literal("RadialMacros - Option Edit"));
        this.parent = parent;
        this.index = index;
        this.group = group;

        if (this.index == -1) {
            this.option = new RadialOption(group, "Macro", new ItemStack(Items.GRASS_BLOCK), new ArrayList<>());
            this.option.setEnabled(true);
        }
        else
            this.option = group.getOptions().get(index).clone();
    }

    @Override
    protected void init() {
        doneButton = addRenderableWidget(Button.builder(Component.literal("Done"), b -> {
                    commit();
                    onClose();
                })
                .bounds(this.width / 2 - 100, this.height - 24, 200, 20).build());

        detailsList = addRenderableWidget(new DetailsList(this.minecraft, width / 2 + 1, height - TOP_BAR_H - BOTTOM_BAR_H, TOP_BAR_H, 28));
        detailsList.setPosition(0, TOP_BAR_H);
        buildDetailsList();

        commandsList = addRenderableWidget(new CommandList(this.minecraft, width / 2, height - TOP_BAR_H - BOTTOM_BAR_H, TOP_BAR_H, 28));
        commandsList.setPosition(width / 2 + 1, TOP_BAR_H);
        buildCommandList();
    }

    private void buildDetailsList() {
        detailsList.clearEntries();

        detailsList.addEntry(new DetailsLabelEntry());
        detailsList.addEntry(new DetailsNameEntry(option));
        detailsList.addEntry(new IconEntry(option));
        detailsList.addEntry(new MaterialEntry(option));
        detailsList.addEntry(new IconMiscOptionsEntry(option));
        detailsList.addEntry(new ItemModelEntry(option));
        detailsList.addEntry(new VisibilityLabelEntry());
        detailsList.addEntry(new VisibilityModeEntry(option, this::refreshDetails));


        if (option.getRules() != null) {
            for (int i = 0; i < option.getRules().size(); i++)
                detailsList.addEntry(new ConditionalRuleEntry(i, option, this::refreshDetails));

            detailsList.addEntry(new AddEntryEntry("Add Rule", () -> {
                option.getRules().add(new ConditionalConfig(ConditionalityRule.IS_MULTIPLAYER, null));
                refreshDetails();
            }));
        }
    }

    private void refreshDetails() {
        double scroll = commandsList.scrollAmount();
        Minecraft.getInstance().gui.setScreen(this);
        detailsList.setScrollAmount(detailsList.maxScrollAmount());
        commandsList.setScrollAmount(scroll);
    }

    private void refreshCommands() {
        double scroll = detailsList.scrollAmount();
        Minecraft.getInstance().gui.setScreen(this);
        commandsList.setScrollAmount(commandsList.maxScrollAmount());
        detailsList.setScrollAmount(scroll);
    }

    private void buildCommandList() {
        commandsList.clearEntries();
        for (int i = 0; i < option.getCommands().size(); i++) {
            commandsList.addEntry(new CommandEntry(i, option.getCommands(), this::refreshCommands));
        }
        commandsList.addEntry(new AddEntryEntry("Add Command", () -> {
            option.getCommands().add("");
            refreshCommands();
        }));
    }

    @Override
    public void resize(int w, int h) {
        super.resize(w, h);
        init(w, h);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        context.centeredText(font, "Edit Macro", width / 2, 28 / 2 - font.lineHeight / 2, 0xFFFFFFFF);
        super.extractRenderState(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(parent);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (commandsList.isHovered())
            return commandsList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        detailsList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (doneButton.mouseClicked(click, doubled)) return true;
        detailsList.mouseClicked(click, doubled);
        commandsList.mouseClicked(click, doubled);
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (doneButton.mouseReleased(click)) return true;
        if (detailsList.mouseReleased(click)) return true;
        if (commandsList.mouseReleased(click)) return true;
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent keyInput) {
        if (detailsList.keyPressed(keyInput)) return true;
        if (commandsList.keyPressed(keyInput)) return true;
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean keyReleased(KeyEvent keyInput) {
        if (detailsList.keyReleased(keyInput)) return true;
        if (commandsList.keyReleased(keyInput)) return true;
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        if (detailsList.charTyped(input)) return true;
        if (commandsList.charTyped(input)) return true;
        return super.charTyped(input);
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
