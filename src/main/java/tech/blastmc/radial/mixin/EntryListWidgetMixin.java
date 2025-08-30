package tech.blastmc.radial.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.util.math.MathHelper;
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
@Mixin(EntryListWidget.class)
public abstract class EntryListWidgetMixin<E extends EntryListWidget.Entry<E>> {

    @Shadow @Final protected int itemHeight;
    @Shadow protected int headerHeight;

    @Shadow protected abstract int getRowLeft();
    @Shadow protected abstract int getRowWidth();
    @Shadow protected abstract E getEntry(int index);
    @Shadow protected abstract int getEntryCount();
    @Shadow protected abstract List<E> children();
    @Shadow protected abstract void renderEntry(DrawContext context, int mouseX, int mouseY, float delta,
                                                int index, int x, int y, int entryWidth, int entryHeight);

    @Unique
    private int rm$getEntryHeightForIndex(int index) {
        if (index < 0 || index >= getEntryCount()) return itemHeight;
        return rm$getEntryHeight(getEntry(index));
    }

    @Unique
    private int rm$getEntryHeight(Object entryObj) {
        if (entryObj instanceof CustomHeightEntry che) {
            int h = che.getItemHeight();
            return Math.max(4, h);
        }
        return this.itemHeight;
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
        int top = ((ClickableWidgetAccessor) this).rm$invokeGetY() + 4 - (int) ((ScrollableWidgetAccessor) this).rm$invokeGetScrollY() + this.headerHeight + rm$getCumulativeHeightUpTo(index);
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

    @Inject(method = "getContentsHeightWithPadding", at = @At("HEAD"), cancellable = true)
    private void rm$getContentsHeightWithPadding(CallbackInfoReturnable<Integer> cir) {
        int total = headerHeight + 4 + rm$getCumulativeHeightUpTo(getEntryCount());
        cir.setReturnValue(total);
    }

    @Inject(method = "getEntryAtPosition", at = @At("HEAD"), cancellable = true)
    private void rm$getEntryAtPosition(double x, double y, CallbackInfoReturnable<@Nullable E> cir) {
        int halfRowWidth = this.getRowWidth() / 2;
        int centerX = ((ClickableWidgetAccessor) this).rm$invokeGetX() + ((ClickableWidgetAccessor) this).rm$invokeGetWidth() / 2;
        int left = centerX - halfRowWidth;
        int right = centerX + halfRowWidth;

        int relY = MathHelper.floor(y - (double) ((ClickableWidgetAccessor) this).rm$invokeGetY()) - this.headerHeight + (int) ((ScrollableWidgetAccessor) this).rm$invokeGetScrollY() - 4;
        if (x < left || x > right || relY < 0) {
            cir.setReturnValue(null);
            return;
        }

        int running = 0;
        int count = this.getEntryCount();
        for (int i = 0; i < count; i++) {
            int h = rm$getEntryHeightForIndex(i);
            if (relY < running + h) {
                cir.setReturnValue(this.getEntry(i));
                return;
            }
            running += h;
        }
        cir.setReturnValue(null);
    }

    @Inject(method = "renderList", at = @At("HEAD"), cancellable = true)
    private void rm$renderList(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int rowLeft = this.getRowLeft();
        int rowWidth = this.getRowWidth();
        int count = this.getEntryCount();

        for (int i = 0; i < count; i++) {
            int yTop = rm$getRowTopReturn(i).getReturnValue();
            int fullHeight = rm$getEntryHeightForIndex(i);
            int innerHeight = Math.max(1, fullHeight - 4);
            int yBottom = yTop + fullHeight;

            if (yBottom >= ((ClickableWidgetAccessor) this).rm$invokeGetY() && yTop <= ((ClickableWidgetAccessor) this).rm$invokeGetBottom()) {
                this.renderEntry(context, mouseX, mouseY, delta, i, rowLeft, yTop, rowWidth, innerHeight);
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

    @Inject(method = "ensureVisible", at = @At("HEAD"), cancellable = true)
    private void rm$ensureVisible(E entry, CallbackInfo ci) {
        int index = this.children().indexOf(entry);
        if (index >= 0) {
            int top = rm$getRowTopReturn(index).getReturnValue();
            int h = rm$getEntryHeight(entry);

            int overTop = top - ((ClickableWidgetAccessor) this).rm$invokeGetY() - 4 - h;
            if (overTop < 0) {
                ((ScrollableWidgetAccessor) this).rm$invokeSetScrollY(((ScrollableWidgetAccessor) this).rm$invokeGetScrollY() + overTop);
            }

            int overBottom = ((ClickableWidgetAccessor) this).rm$invokeGetBottom() - top - h - h;
            if (overBottom < 0) {
                ((ScrollableWidgetAccessor) this).rm$invokeSetScrollY(((ScrollableWidgetAccessor) this).rm$invokeGetScrollY() - overBottom);
            }
        }
        ci.cancel();
    }
}

