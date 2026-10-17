package com.witherplus.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.witherplus.WitherPlus;
import com.witherplus.WpConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

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

    public static void render(GuiGraphics graphics, float partialTick) {
        if (!WpConfig.ENABLE_SCREEN_EFFECTS.get()) {
            return;
        }
        if (flash < 0.005F && desaturate < 0.005F && vignette < 0.005F) {
            return;
        }
        ShaderInstance shader = WpShaders.screen;
        if (shader == null) {
            return;
        }
        graphics.flush();
        ShaderInstance previousShader = RenderSystem.getShader();
        int previousTexture = RenderSystem.getShaderTexture(0);
        float[] previousColor = RenderSystem.getShaderColor().clone();
        SceneCopy.capture();
        if (WpConfig.DEBUG_LOGGING.get() && !diagnosticsLogged) {
            diagnosticsLogged = true;
            WitherPlus.LOGGER.info("Wither screen FX active: desaturate={}, flash={}, vignette={}", desaturate, flash, vignette);
        }
        shader.safeGetUniform("WpParams").set(desaturate, flash, vignette, 0.0F);
        shader.safeGetUniform("WpTint").set(((vignetteColor >> 16) & 255) / 255.0F, ((vignetteColor >> 8) & 255) / 255.0F, (vignetteColor & 255) / 255.0F, 0.9F);
        shader.safeGetUniform("WpFlash").set(((flashColor >> 16) & 255) / 255.0F, ((flashColor >> 8) & 255) / 255.0F, (flashColor & 255) / 255.0F, 1.0F);
        try {
            RenderSystem.setShader(() -> shader);
            RenderSystem.setShaderTexture(0, SceneCopy.textureId());
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            Matrix4f matrix = graphics.pose().last().pose();
            float w = graphics.guiWidth();
            float h = graphics.guiHeight();
            BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setUv(0.0F, 1.0F);
            buffer.addVertex(matrix, 0.0F, h, 0.0F).setUv(0.0F, 0.0F);
            buffer.addVertex(matrix, w, h, 0.0F).setUv(1.0F, 0.0F);
            buffer.addVertex(matrix, w, 0.0F, 0.0F).setUv(1.0F, 1.0F);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        } finally {
            RenderSystem.setShader(() -> previousShader);
            RenderSystem.setShaderTexture(0, previousTexture);
            RenderSystem.setShaderColor(previousColor[0], previousColor[1], previousColor[2], previousColor[3]);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            SceneCopy.invalidate();
        }
    }
}
