package tech.blastmc.radial.macros;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import tech.blastmc.radial.macros.condition.ConditionalConfig;
import tech.blastmc.radial.macros.condition.ConditionalityRule;
import tech.blastmc.radial.util.SkinService;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RadialOption {

    public static final List<String> DYEABLE = List.of("minecraft:leather_helmet", "minecraft:leather_chestplate",
            "minecraft:leather_leggings", "minecraft:leather_boots", "minecraft:leather_horse_armor", "minecraft:wolf_armor");

    private String name;
    private boolean enabled;
    private String material;
    private String rgb;
    private String itemModel;
    private boolean enchanted;
    private String skullOwner;
    private List<String> commands;
    private boolean conditional;
    private List<ConditionalConfig> rules;

    private transient boolean skullOwnerProcessed;
    private transient long skullOwnerLastUpdate;
    private transient RadialGroup group;

    public RadialOption(RadialGroup group, String name, ItemStack icon, List<String> commands) {
        this.group = group;
        this.name = name;
        this.material = icon.getItem().toString();
        this.commands = new ArrayList<>(commands);
    }

    public RadialOption() {
        this.enabled = true;
    }

    public void clearCachedIcon() {
        this.cached = null;
    }

    public void run() {
        if (commands == null || commands.isEmpty())
            return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null)
            return;
        var network = mc.getNetworkHandler();
        if (network == null)
            return;

        for (String command : commands) {
            String raw = command.startsWith("/") ? command.substring(1) : command;
            network.sendChatCommand(raw);
        }
    }

    public void setSkullOwner(String skullOwner) {
        this.skullOwner = skullOwner;
        this.skullOwnerProcessed = false;
        this.skullOwnerLastUpdate = Util.getMeasuringTimeMs();
    }

    public boolean isDyeable() {
        if (this.material == null || this.material.isEmpty())
            return false;
        if (DYEABLE.contains(this.material))
            return true;
        if (this.material.split(":").length == 1)
            return DYEABLE.contains("minecraft:" + this.material);
        return false;
    }

    public RadialOption clone() {
        return new RadialOption(this.name, this.enabled, this.material, this.rgb, this.itemModel, this.enchanted,
                this.skullOwner, new ArrayList<>(this.commands), conditional, this.rules == null ? null : new ArrayList<>(this.rules),
                true, 0, this.group, null);
    }

    transient ItemStack cached;

    public ItemStack getIcon() {
        if (cached != null)
            return cached;
        try {
            String material = getMaterial();
            if (material.split(":").length == 1)
                material = "minecraft:" + material;

            Item item = Registries.ITEM.get(Identifier.of(getMaterial()));
            if (item == Items.AIR)
                throw new IllegalArgumentException();
            ItemStack stack = new ItemStack(item);

            if (isEnchanted())
                stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

            if (material.endsWith("player_head") && skullOwner != null && !skullOwner.isEmpty()) {
                SkinService.get().fetch(skullOwner).thenAccept(gp -> {
                    if (gp == null)
                        return;
                    if (skullOwner.equals(gp.getName()))
                        stack.set(DataComponentTypes.PROFILE, new ProfileComponent(gp));
                });
            }

            if (DYEABLE.contains(material) && rgb != null && !rgb.isEmpty()) {
                String[] p = rgb.trim().split(",");
                int r = Integer.parseInt(p[0]), g = Integer.parseInt(p[1]), b = Integer.parseInt(p[2]);
                int color = (r << 16) | (g << 8) | b;
                stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color));
            }

            if (itemModel != null && !itemModel.isEmpty()) {
                String finalModel = itemModel;
                if (itemModel.split(":").length == 1)
                    finalModel = "minecraft:" + finalModel;
                stack.set(DataComponentTypes.ITEM_MODEL, Identifier.of(finalModel));
            }

            cached = stack;
            return stack;
        } catch (Exception e) {
            return new ItemStack(Items.BARRIER);
        }
    }

    public boolean isVisible() {
        if (!enabled)
            return false;

        if (!conditional)
            return true;

        for (ConditionalConfig rule : rules)
            if (rule.getType().needsContext())
                return rule.getType().testWithContext(rule.getValue(), getContextFor(rule.getType()));
            else if (!rule.getType().test(rule.getValue()))
                return false;
        return true;
    }

    public Object[] getContextFor(ConditionalityRule type) {
        return switch (type) {
            case OPTION_VISIBLE, OPTION_NOT_VISIBLE -> List.of(this.group).toArray(new Object[0]);
            default -> null;
        };
    }

}
