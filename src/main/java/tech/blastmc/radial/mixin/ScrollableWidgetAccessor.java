package tech.blastmc.radial.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.client.gui.components.AbstractScrollArea.class)
public interface ScrollableWidgetAccessor {
    @Invoker("scrollAmount") double rm$invokeGetScrollY();
    @Invoker("setScrollAmount") void rm$invokeSetScrollY(double y);
}
