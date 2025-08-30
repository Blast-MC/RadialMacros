package tech.blastmc.radial.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.client.gui.widget.ScrollableWidget.class)
public interface ScrollableWidgetAccessor {
    @Invoker("getScrollY") double rm$invokeGetScrollY();
    @Invoker("setScrollY") void rm$invokeSetScrollY(double y);
}
