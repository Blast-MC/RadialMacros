package tech.blastmc.radial.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.blastmc.radial.config.screen.widget.EnumDropdownWidget;
import tech.blastmc.radial.screen.InGameControlsEnabledScreen;

@Mixin(Gui.class)
public class MinecraftClientMixin {

    @Shadow
    public Screen screen;

    @Redirect(
            method = "setScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;releaseAll()V"
            )
    )
    private void skipUnpressForEnabledControls() {
        if (!(screen instanceof InGameControlsEnabledScreen)) {
            KeyMapping.releaseAll();
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    void setScreen(Screen screen, CallbackInfo ci) {
        EnumDropdownWidget.WIDGETS.clear();
    }

}
