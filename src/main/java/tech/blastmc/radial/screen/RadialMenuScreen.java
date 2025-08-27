package tech.blastmc.radial.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;
import tech.blastmc.radial.InputHandler;
import tech.blastmc.radial.macros.RadialOption;

import java.util.List;

public class RadialMenuScreen extends InGameControlsEnabledScreen {
    private final List<RadialOption> options;

    private int cx, cy;
    private int innerR = 67;
    private int thickness = 38;
    private int iconRadius = 86;
    private RingAssets.Ring baked;
    private float lastInnerRatio = -1f;
    private int lastSlices = -1;

    private final InputUtil.Key openKey;
    private int selected = -1;

    private final long openedAt = Util.getMeasuringTimeMs();
    private float inEase;

    public RadialMenuScreen(List<RadialOption> options, InputUtil.Key key) {
        super(Text.literal("RadialMacros"));
        this.options = options;
        this.openKey = key;
    }

    @Override
    protected void init() {
        cx = this.width / 2;
        cy = this.height / 2;

        int minDim = Math.min(width, height);
        thickness = Math.max(thickness, minDim / 20);
        innerR = Math.max(innerR, minDim / 28);
        iconRadius = Math.max(iconRadius, minDim / 9);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float innerRatio = innerR / (float)(innerR + thickness);
        int slices = Math.max(1, options.size());
        if (baked == null || slices != lastSlices || Math.abs(innerRatio - lastInnerRatio) > 0.001f) {
            baked = RingAssets.getOrBake(slices, innerRatio,
                    0x2F2F2F, 0.6f,
                    0x0E0E0E, 2.5f, 0f,
                    0xFFFFFF, 0.25f,
                    512);
            lastSlices = slices;
            lastInnerRatio = innerRatio;
        }

        inEase = Math.min(1f, (Util.getMeasuringTimeMs() - openedAt) / 120f);
        float pop = easeOutBack(inEase);

        float sector = 360f / slices;
        float startDeg = -90f - 0.5f * sector;

        selected = computeSelected(mouseX, mouseY, startDeg);

        int outerR = (int) (innerR + thickness * pop);

        RingAssets.drawRing(ctx, baked, cx, cy, outerR, startDeg);
        if (selected >= 0)
            RingAssets.drawWedge(ctx, baked, cx, cy, outerR, selected, slices, startDeg);

        drawIcons(ctx, pop, startDeg);
        drawCenter(ctx, mouseX, mouseY, pop);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            boolean inCenter = dist((float) mouseX,(float) mouseY, cx, cy) < innerR * 0.85f;
            if (!inCenter && getSelectedOption() != null)
                activateAndClose();
            else
                cancelAndClose();
            return true;
        }

        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            cancelAndClose();
            return true;
        }
        return true;
    }

    private void activateAndClose() {
        RadialOption opt = getSelectedOption();
        if (opt != null) opt.run();
        InputHandler.latchUntilReleased();
        close();
    }

    private void cancelAndClose() {
        InputHandler.latchUntilReleased();
        close();
    }

    @Override
    public void tick() {
        long window = MinecraftClient.getInstance().getWindow().getHandle();
        boolean down = (openKey.type == InputUtil.Type.MOUSE)
                ? GLFW.glfwGetMouseButton(window, openKey.getCode()) == GLFW.GLFW_PRESS
                : InputUtil.isKeyPressed(window, openKey.getCode());

        if (!down)
            activateAndClose();

        super.tick();
    }

    @Override
    public void close() {
        super.close();
        baked = null;
    }

    private RadialOption getSelectedOption() {
        if (selected < 0 || selected >= options.size())
            return null;
        return options.get(selected);
    }

    private void drawIcons(DrawContext ctx, float pop, float startDeg) {
        if (options.isEmpty()) return;
        float sector = 360f / options.size();

        for (int i = 0; i < options.size(); i++) {
            float midDeg = (i + 0.5f) * sector + startDeg; // center of slice i
            double rad = Math.toRadians(midDeg);
            int ix = cx + (int) (Math.cos(rad) * iconRadius * pop);
            int iy = cy + (int) (Math.sin(rad) * iconRadius * pop);

            int size = 24;
            float scale = size / 16f;
            var m = ctx.getMatrices();
            m.pushMatrix();
            m.translate(ix, iy);
            m.scale(scale, scale);
            m.translate(-8, -8);
            var stack = options.get(i).getIcon();
            ctx.drawItem(stack, 0, 0);
            m.popMatrix();
        }
    }

    private void drawCenter(DrawContext ctx, int mouseX, int mouseY, float pop) {
        int rInner = (int) (innerR * pop);
        boolean inCenter = dist(mouseX, mouseY, cx, cy) < rInner * 0.85f;

        if (inCenter) {
            String x = "✕";
            int tw = MinecraftClient.getInstance().textRenderer.getWidth(x);
            ctx.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(x), cx - tw / 2, cy - 4, 0xFFFFFFFF, true);
        } else {
            RadialOption opt = getSelectedOption();
            if (opt != null) {
                int size = Math.max(28, (int) (thickness * 0.9f));
                float scale = size / 16f;
                Matrix3x2fStack m = ctx.getMatrices();
                m.pushMatrix();
                m.translate(cx, cy);
                m.scale(scale, scale);
                m.translate(-8, -8);
                ctx.drawItem(opt.getIcon(), 0, 0);
                m.popMatrix();

                int tw = MinecraftClient.getInstance().textRenderer.getWidth(opt.getName());
                ctx.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(opt.getName()), cx - tw / 2, cy + size / 2 + 8, 0xFFFFFFFF, true);
            }
        }
    }

    private int computeSelected(int mouseX, int mouseY, float startDeg) {
        if (options.isEmpty())
            return -1;

        double dx = mouseX - cx, dy = mouseY - cy;
        if (Math.hypot(dx, dy) < innerR * 0.85)
            return -1;

        double ang = Math.toDegrees(Math.atan2(dy, dx));
        ang = (ang % 360 + 360) % 360;

        double sector = 360.0 / options.size();
        double rel = ang - startDeg;
        rel = (rel % 360 + 360) % 360;

        int idx = (int) Math.floor(rel / sector);
        return Math.max(0, Math.min(options.size() - 1, idx));
    }

    private static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        t = Math.min(1f, Math.max(0f, t));
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }

    private static float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot(x2 - x1, y2 - y1);
    }
}
