package com.witherplus.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.witherplus.WitherPlus;
import com.witherplus.WpConfig;

public final class ScreenFx {
    private static float desaturate;
    private static float flash;
    private static int flashColor = 0xFFFFFF;
    private static float vignette;
    private static int vignetteColor = 0x000000;
    private static float holdDesaturate;
    private static float holdVignette;
    private static boolean diagnosticsLogged;

    private ScreenFx() {
    }

    public static void flash(float amount, int color) {
        if (amount > flash) {
            flash = amount;
            flashColor = color;
        }
    }

    public static void desaturate(float amount) {
        desaturate = Math.max(desaturate, amount);
    }

    public static void sustain(float desaturateAmount, float vignetteAmount, int color) {
        holdDesaturate = Math.max(holdDesaturate, desaturateAmount);
        if (vignetteAmount > holdVignette) {
            holdVignette = vignetteAmount;
            vignetteColor = color;
        }
    }

    public static void clear() {
        diagnosticsLogged = false;
        desaturate = 0.0F;
        flash = 0.0F;
        vignette = 0.0F;
        holdDesaturate = 0.0F;
        holdVignette = 0.0F;
    }

    public static void tick() {
        flash = Math.max(0.0F, flash * 0.8F - 0.01F);
        desaturate = Math.max(holdDesaturate, desaturate * 0.9F);
        vignette = Math.max(holdVignette, vignette * 0.9F);
        holdDesaturate = 0.0F;
        holdVignette = 0.0F;
    }

    public static void render() {
        if (!WpConfig.ENABLE_SCREEN_EFFECTS.get() || (flash < 0.005F && desaturate < 0.005F && vignette < 0.005F)) return;
        SceneCopy.capture();
        if (WpConfig.DEBUG_LOGGING.get() && !diagnosticsLogged) {
            diagnosticsLogged = true;
            WitherPlus.LOGGER.info("Wither screen FX active: desaturate={}, flash={}, vignette={}", desaturate, flash, vignette);
        }
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(-1, 1, 0).setUv(0, 1);
        buffer.addVertex(-1, -1, 0).setUv(0, 0);
        buffer.addVertex(1, -1, 0).setUv(1, 0);
        buffer.addVertex(1, 1, 0).setUv(1, 1);
        var tint = new org.joml.Vector4f(((vignetteColor >> 16) & 255) / 255.0F, ((vignetteColor >> 8) & 255) / 255.0F, (vignetteColor & 255) / 255.0F, 0.9F);
        var flashTint = new org.joml.Vector4f(((flashColor >> 16) & 255) / 255.0F, ((flashColor >> 8) & 255) / 255.0F, (flashColor & 255) / 255.0F, 1.0F);
        try {
            WpGpu.draw(buffer.buildOrThrow(), WpShaders.SCREEN, new org.joml.Vector4f(desaturate, flash, vignette, 0), tint, flashTint, WitherPlus.id("noise"));
        } finally {
            SceneCopy.invalidate();
        }
    }
}
