package tech.blastmc.radial.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.blastmc.radial.config.screen.widget.EnumDropdownWidget;
import tech.blastmc.radial.util.ExtraHoveredIgnored;
import tech.blastmc.radial.util.HasId;

@Mixin(AbstractWidget.class)
public class ClickableWidgetMixin {

    @Shadow protected boolean isHovered;

    @Inject(method = "extractRenderState",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/AbstractWidget;extractWidgetRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
    void rm$render(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (!this.isHovered)
            return;

        this.isHovered = anyOpenDropdownWidgets();
    }

    @Unique
    public boolean anyOpenDropdownWidgets() {
        if (this instanceof ExtraHoveredIgnored)
            return true;
        for (EnumDropdownWidget<?> widget : EnumDropdownWidget.WIDGETS)
            if (widget.isExpanded())
                if (this instanceof HasId hasId)
                    return hasId.getId().equals(widget.getId());
                else
                    return false;
        return true;
    }

}
