package tech.blastmc.radial.mixin;

import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClickableWidget.class)
public interface ClickableWidgetAccessor {
    @Invoker("getX") int rm$invokeGetX();
    @Invoker("getY") int rm$invokeGetY();
    @Invoker("getBottom") int rm$invokeGetBottom();
    @Invoker("getWidth") int rm$invokeGetWidth();
    @Invoker("getHeight") int rm$invokeGetHeight();
}
