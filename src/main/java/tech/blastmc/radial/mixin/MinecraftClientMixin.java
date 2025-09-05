package tech.blastmc.radial.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.blastmc.radial.config.screen.widget.EnumDropdownWidget;
import tech.blastmc.radial.screen.InGameControlsEnabledScreen;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow
    public Screen currentScreen;

    @Redirect(
            method = "setScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/KeyBinding;unpressAll()V"
            )
    )
    private void skipUnpressForEnabledControls() {
        if (!(currentScreen instanceof InGameControlsEnabledScreen)) {
            KeyBinding.unpressAll();
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    void setScreen(Screen screen, CallbackInfo ci) {
        EnumDropdownWidget.WIDGETS.clear();
    }

}
