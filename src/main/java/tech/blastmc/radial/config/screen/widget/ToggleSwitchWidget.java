package tech.blastmc.radial.config.screen.widget;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import tech.blastmc.radial.RadialMacros;

import java.util.function.Consumer;

public class ToggleSwitchWidget extends ClickableWidget {

    private static final ButtonTextures TEXTURES = new ButtonTextures(
            RadialMacros.id("toggle/enabled"), RadialMacros.id("toggle/disabled"),
            RadialMacros.id("toggle/enabled_highlighted"), RadialMacros.id("toggle/disabled_highlighted")
    );

    @Getter @Setter
    boolean enabled;
    @Setter
    Consumer<Boolean> onChangeListener;

    public ToggleSwitchWidget(int x, int y, int width, int height, boolean enabled) {
        super(x, y, width, height, Text.empty());
        this.enabled = enabled;
        setTooltip(Tooltip.of(Text.of(enabled ? "Enabled" : "Disabled")));
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURES.get(this.enabled, this.isSelected()), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ColorHelper.getWhite(this.alpha));
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.enabled = !this.enabled;
        setTooltip(Tooltip.of(Text.of(enabled ? "Enabled" : "Disabled")));
        if (this.onChangeListener != null)
            this.onChangeListener.accept(this.enabled);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) { }
}
