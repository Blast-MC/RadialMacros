package tech.blastmc.radial.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import tech.blastmc.radial.RadialMacros;

import java.util.HashMap;
import java.util.Map;

public final class RingAssets {
    private RingAssets() {}

    public static final class Ring {
        public final Identifier ringId;
        public final Identifier wedgeId;
        public final int texSize;

        Ring(Identifier ringId, Identifier wedgeId, int texSize) {
            this.ringId = ringId; this.wedgeId = wedgeId; this.texSize = texSize;
        }
    }

    private static final Map<String, Ring> CACHE = new HashMap<>();

    public static Ring getOrBake(
            int slices, float innerRatio,
            int baseRGB, float baseAlpha,
            int outlineRGB, float outlinePx, float gapDeg,
            int hiRGB, float hiAlpha,
            int texSize
    ) {
        slices = Math.max(1, slices);
        innerRatio = Math.max(0.05f, Math.min(0.95f, innerRatio));
        outlinePx = Math.max(1f, outlinePx);
        gapDeg = Math.max(0f, gapDeg);

        String key = slices + "|" + Math.round(innerRatio * 1000f) + "|" + baseRGB + "|" +
                Math.round(baseAlpha * 1000f) + "|" + outlineRGB + "|" + Math.round(outlinePx) + "|" +
                Math.round(gapDeg * 10f) + "|" + hiRGB + "|" + Math.round(hiAlpha * 1000f) + "|" + texSize;

        Ring cached = CACHE.get(key);
        if (cached != null)
            return cached;

        float cx = texSize * 0.5f, cy = texSize * 0.5f;
        float outerR = texSize * 0.5f - outlinePx;
        float innerR = outerR * innerRatio;
        float aa = 1.5f;
        float halfTexelBias = 0.5f;
        float sectorRad = (float) (Math.TAU / slices);
        float halfGap = (float) Math.toRadians(gapDeg) * 0.5f;

        float innerEdge = innerR - halfTexelBias;
        float outerEdge = outerR + halfTexelBias;

        NativeImage ring = new NativeImage(NativeImage.Format.RGBA, texSize, texSize, false);
        clearTransparent(ring);

        for (int y = 0; y < texSize; y++) {
            for (int x = 0; x < texSize; x++) {
                float dx = (x + 0.5f) - cx;
                float dy = (y + 0.5f) - cy;
                float r  = (float) Math.hypot(dx, dy);
                if (r < innerEdge - aa || r > outerEdge + aa)
                    continue;

                float ang = wrapPi((float) Math.atan2(dy, dx));

                float dInner = Math.abs(r - innerEdge);
                float dOuter = Math.abs(outerEdge - r);

                float mod = mod2pi(ang);
                float rel = mod % sectorRad;
                float toStart = rel;
                float toEnd   = sectorRad - rel;
                float minEdgeAngle = Math.min(toStart, toEnd);
                boolean inGap = (minEdgeAngle < halfGap);
                float dRadialPx = r * (minEdgeAngle);

                float aOutline = 0f;
                aOutline = Math.max(aOutline, smoothstep(outlinePx + aa, outlinePx - aa, dInner));
                aOutline = Math.max(aOutline, smoothstep(outlinePx + aa, outlinePx - aa, dOuter));
                aOutline = Math.max(aOutline, smoothstep(outlinePx + aa, outlinePx - aa, dRadialPx));

                float aFill = baseAlpha;
                aFill *= smoothstep(0f, aa, r - (innerEdge - aa))
                        *  smoothstep(0f, aa, (outerEdge + aa) - r);

                if (inGap)
                    continue;

                float aOut = aOutline + aFill * (1f - aOutline);

                int rgb = (aOutline > 0f) ? outlineRGB : baseRGB;
                int A   = Math.min(255, Math.max(0, Math.round(aOut * 255f)));
                ring.setColor(x, y, (A << 24) | (rgb & 0xFFFFFF));
            }
        }

        NativeImage wedge = new NativeImage(NativeImage.Format.RGBA, texSize, texSize, false);
        clearTransparent(wedge);

        float halfSlice = sectorRad * 0.5f - halfGap;

        for (int y = 0; y < texSize; y++) {
            for (int x = 0; x < texSize; x++) {
                float dx = (x + 0.5f) - cx;
                float dy = (y + 0.5f) - cy;
                float r  = (float) Math.hypot(dx, dy);
                if (r < innerEdge - aa || r > outerEdge + aa)
                    continue;

                float ang = wrapPi((float) Math.atan2(dy, dx));

                boolean insideWedge = (Math.abs(ang) <= halfSlice);
                if (!insideWedge)
                    continue;

                float dInner = Math.abs(r - innerEdge);
                float dOuter = Math.abs(outerEdge - r);
                float dLeftA  = Math.abs(ang + halfSlice) * r;
                float dRightA = Math.abs(ang - halfSlice) * r;
                float dRadialPx = Math.min(dLeftA, dRightA);

                float aOutline = 0f;
                aOutline = Math.max(aOutline, smoothstep(outlinePx + aa, outlinePx - aa, dInner));
                aOutline = Math.max(aOutline, smoothstep(outlinePx + aa, outlinePx - aa, dOuter));
                aOutline = Math.max(aOutline, smoothstep(outlinePx + aa, outlinePx - aa, dRadialPx));

                float aFill = hiAlpha;
                aFill *= smoothstep(0f, aa, r - (innerEdge - aa))
                        *  smoothstep(0f, aa, (outerEdge + aa) - r);

                float aOut = aOutline + aFill * (1f - aOutline);
                int rgb = (aOutline > 0f) ? outlineRGB : hiRGB;
                int A   = Math.min(255, Math.max(0, Math.round(aOut * 255f)));
                wedge.setColor(x, y, (A << 24) | (rgb & 0xFFFFFF));
            }
        }

        var ringTex  = new NativeImageBackedTexture(() -> "ui/ring" + key.hashCode(), ring);
        ringTex.setFilter(true, false);
        ringTex.setClamp(true);
        ringTex.upload();
        var wedgeTex = new NativeImageBackedTexture(() -> "ui/wedge" + key.hashCode(), wedge);
        wedgeTex.setFilter(true, false);
        wedgeTex.setClamp(true);
        wedgeTex.upload();

        var tm = MinecraftClient.getInstance().getTextureManager();
        Identifier ringId  = RadialMacros.id("ui/ring_"  + key.hashCode());
        Identifier wedgeId = RadialMacros.id("ui/wedge_" + key.hashCode());
        tm.registerTexture(ringId,  ringTex);
        tm.registerTexture(wedgeId, wedgeTex);

        Ring baked = new Ring(ringId, wedgeId, texSize);
        CACHE.put(key, baked);
        return baked;
    }

    public static void drawRing(DrawContext ctx, Ring ring, int cx, int cy, int outerRadiusPx, float rotationDeg) {
        int size = outerRadiusPx * 2;
        int x0 = -outerRadiusPx, y0 = -outerRadiusPx;

        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(cx, cy);
        m.rotate((float) Math.toRadians(rotationDeg));
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, ring.ringId, x0, y0, 0, 0,
                size, size, ring.texSize, ring.texSize, ring.texSize, ring.texSize);
        m.popMatrix();
    }

    public static void drawWedge(DrawContext ctx, Ring ring, int cx, int cy, int outerRadiusPx, int index, int slices, float startDeg) {
        int size = outerRadiusPx * 2;
        int x0 = -outerRadiusPx, y0 = -outerRadiusPx;

        float sectorDeg = 360f / Math.max(1, slices);
        float rotDeg = (index + 0.5f) * sectorDeg + startDeg;

        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(cx, cy);
        m.rotate((float)Math.toRadians(rotDeg));
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, ring.wedgeId, x0, y0, 0, 0, size, size, ring.texSize, ring.texSize, ring.texSize, ring.texSize);
        m.popMatrix();
    }

    private static void clearTransparent(NativeImage img) {
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                img.setColor(x, y, 0x00000000);
    }

    private static float smoothstep(float e0, float e1, float x) {
        float t = (x - e0) / (e1 - e0);
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }

    private static float wrapPi(float a) {
        a = (float)((a + Math.PI) % (Math.TAU));
        if (a < 0) a += (float)Math.TAU;
        return a - (float)Math.PI;
    }

    private static float mod2pi(float a) {
        a = (float)(a % Math.TAU);
        if (a < 0) a += (float)Math.TAU;
        return a;
    }

}
