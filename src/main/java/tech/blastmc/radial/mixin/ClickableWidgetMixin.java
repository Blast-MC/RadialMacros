package tech.blastmc.radial.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.blastmc.radial.config.screen.widget.EnumDropdownWidget;
import tech.blastmc.radial.util.ExtraHoveredIgnored;
import tech.blastmc.radial.util.HasId;

@Mixin(ClickableWidget.class)
public class ClickableWidgetMixin {

    @Shadow protected boolean hovered;

    @Inject(method = "render",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/ClickableWidget;renderWidget(Lnet/minecraft/client/gui/DrawContext;IIF)V"))
    void rm$render(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (!this.hovered)
            return;

        this.hovered = anyOpenDropdownWidgets();
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
