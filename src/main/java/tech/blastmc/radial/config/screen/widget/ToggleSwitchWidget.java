package tech.blastmc.radial.config.screen.widget;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import tech.blastmc.radial.RadialMacros;

import java.util.function.Consumer;

public class ToggleSwitchWidget extends AbstractWidget {

    private static final WidgetSprites TEXTURES = new WidgetSprites(
            RadialMacros.id("toggle/enabled"), RadialMacros.id("toggle/disabled"),
            RadialMacros.id("toggle/enabled_highlighted"), RadialMacros.id("toggle/disabled_highlighted")
    );

    @Getter @Setter
    boolean enabled;
    @Setter
    Consumer<Boolean> onChangeListener;

    public ToggleSwitchWidget(int x, int y, int width, int height, boolean enabled) {
        super(x, y, width, height, Component.empty());
        this.enabled = enabled;
        setTooltip(Tooltip.create(Component.nullToEmpty(enabled ? "Enabled" : "Disabled")));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURES.get(this.enabled, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));
    }

    @Override
    public void onClick(MouseButtonEvent click, boolean doubled) {
        this.enabled = !this.enabled;
        setTooltip(Tooltip.create(Component.nullToEmpty(enabled ? "Enabled" : "Disabled")));
        if (this.onChangeListener != null)
            this.onChangeListener.accept(this.enabled);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) { }
}
