package tech.blastmc.radial.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.blastmc.radial.util.ScreenUtils;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "render", at = @At("TAIL"))
    void renderLast(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        ScreenUtils.LAST_RENDERS.forEach(Runnable::run);
        ScreenUtils.LAST_RENDERS.clear();
    }

}
