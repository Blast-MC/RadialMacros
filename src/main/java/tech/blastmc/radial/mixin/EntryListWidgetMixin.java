package tech.blastmc.radial.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.blastmc.radial.config.screen.list.entry.CustomHeightEntry;

import java.util.List;

/**
 * Makes EntryListWidget support variable row heights via CustomHeightEntry.
 * Any entry that implements CustomHeightEntry will use its own height; others use the widget's itemHeight.
 */
@Environment(EnvType.CLIENT)
@Mixin(AbstractSelectionList.class)
public abstract class EntryListWidgetMixin<E extends AbstractSelectionList.Entry<E>> {

    @Shadow @Final protected int defaultEntryHeight;

    @Shadow
    public abstract int getRowLeft();
    @Shadow
    public abstract int getRowWidth();
    @Shadow protected abstract int getItemCount();
    @Shadow
    public abstract List<E> children();
    @Shadow protected abstract void extractItem(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, E entry);

    @Unique
    private int rm$getEntryHeightForIndex(int index) {
        if (index < 0 || index >= getItemCount()) return defaultEntryHeight;
        return rm$getEntryHeight(children().get(index));
    }

    @Unique
    private int rm$getEntryHeight(Object entryObj) {
        if (entryObj instanceof CustomHeightEntry che) {
            int h = che.getItemHeight();
            return Math.max(4, h);
        }
        return this.defaultEntryHeight;
    }

    @Unique
    private int rm$getCumulativeHeightUpTo(int exclusiveIndex) {
        int sum = 0;
        for (int i = 0; i < exclusiveIndex; i++) {
            sum += rm$getEntryHeightForIndex(i);
        }
        return sum;
    }

    @Inject(method = "getRowTop", at = @At("HEAD"), cancellable = true)
    private void rm$getRowTop(int index, CallbackInfoReturnable<Integer> cir) {
        int top = ((ClickableWidgetAccessor) this).rm$invokeGetY() + 4 - (int) ((ScrollableWidgetAccessor) this).rm$invokeGetScrollY() + rm$getCumulativeHeightUpTo(index);
        cir.setReturnValue(top);
    }

    @Inject(method = "getRowBottom", at = @At("HEAD"), cancellable = true)
    private void rm$getRowBottom(int index, CallbackInfoReturnable<Integer> cir) {
        int bottom = (Integer) ((CallbackInfoReturnable<?>) rm$getRowTopReturn(index)).getReturnValue()
                + rm$getEntryHeightForIndex(index);
        cir.setReturnValue(bottom);
    }

    @Unique
    private CallbackInfoReturnable<Integer> rm$getRowTopReturn(int index) {
        CallbackInfoReturnable<Integer> cir = new CallbackInfoReturnable<>("getRowTop", true);
        rm$getRowTop(index, cir);
        return cir;
    }

    @Inject(method = "contentHeight", at = @At("HEAD"), cancellable = true)
    private void rm$getContentsHeightWithPadding(CallbackInfoReturnable<Integer> cir) {
        int total = 4 + rm$getCumulativeHeightUpTo(getItemCount());
        cir.setReturnValue(total);
    }

    @Inject(method = "getEntryAtPosition", at = @At("HEAD"), cancellable = true)
    private void rm$getEntryAtPosition(double x, double y, CallbackInfoReturnable<@Nullable E> cir) {
        int halfRowWidth = this.getRowWidth() / 2;
        int centerX = ((ClickableWidgetAccessor) this).rm$invokeGetX() + ((ClickableWidgetAccessor) this).rm$invokeGetWidth() / 2;
        int left = centerX - halfRowWidth;
        int right = centerX + halfRowWidth;

        int relY = Mth.floor(y - (double) ((ClickableWidgetAccessor) this).rm$invokeGetY()) + (int) ((ScrollableWidgetAccessor) this).rm$invokeGetScrollY() - 4;
        if (x < left || x > right || relY < 0) {
            cir.setReturnValue(null);
            return;
        }

        int running = 0;
        int count = this.getItemCount();
        for (int i = 0; i < count; i++) {
            int h = rm$getEntryHeightForIndex(i);
            if (relY < running + h) {
                cir.setReturnValue(children().get(i));
                return;
            }
            running += h;
        }
        cir.setReturnValue(null);
    }

    @Inject(method = "extractListItems", at = @At("HEAD"), cancellable = true)
    private void rm$renderList(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int rowLeft = this.getRowLeft();
        int rowWidth = this.getRowWidth();
        int count = this.getItemCount();

        for (int i = 0; i < count; i++) {
            int yTop = rm$getRowTopReturn(i).getReturnValue();
            int fullHeight = rm$getEntryHeightForIndex(i);
            int innerHeight = Math.max(1, fullHeight - 4);
            int yBottom = yTop + fullHeight;

            if (yBottom >= ((ClickableWidgetAccessor) this).rm$invokeGetY() && yTop <= ((ClickableWidgetAccessor) this).rm$invokeGetBottom()) {
                this.extractItem(context, mouseX, mouseY, delta, children().get(i));
            }
        }

        ci.cancel();
    }

    @Inject(method = "centerScrollOn", at = @At("HEAD"), cancellable = true)
    private void rm$centerScrollOn(E entry, CallbackInfo ci) {
        int index = this.children().indexOf(entry);
        if (index >= 0) {
            int top = rm$getRowTopReturn(index).getReturnValue();
            int h = rm$getEntryHeight(entry);
            ((ScrollableWidgetAccessor) this).rm$invokeSetScrollY(top + (h / 2.0) - (((ClickableWidgetAccessor) this).rm$invokeGetHeight() / 2.0));
        }
        ci.cancel();
    }

}

