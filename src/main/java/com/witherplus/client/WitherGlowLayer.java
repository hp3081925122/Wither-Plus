package com.witherplus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.witherplus.WitherPlus;
import com.witherplus.WpConfig;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;

public final class WitherGlowLayer extends RenderLayer<WitherBoss, WitherBossModel<WitherBoss>> {
    private static final ResourceLocation GLOW = WitherPlus.id("textures/entity/wither_glow.png");
    private static final ResourceLocation RAGE = WitherPlus.id("textures/entity/wither_glow_rage.png");

    public WitherGlowLayer(RenderLayerParent<WitherBoss, WitherBossModel<WitherBoss>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, WitherBoss wither, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!WpConfig.ENABLE_GLOW_LAYER.get() || wither.isInvisible()) {
            return;
        }
        int stage = WitherStageClient.stage(wither.getId());
        if (stage <= 0) {
            return;
        }
        int rgb = WitherStageClient.color(stage);
        float r = ((rgb >> 16) & 255) / 255.0F;
        float g = ((rgb >> 8) & 255) / 255.0F;
        float b = (rgb & 255) / 255.0F;
        float pulse = 0.75F + 0.25F * Mth.sin(ageInTicks * 0.12F);
        this.getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.eyes(GLOW)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, r * pulse, g * pulse, b * pulse, 1.0F);
        float rage = WitherStageClient.rage(wither.getId());
        if (rage > 0.01F) {
            float k = rage * (0.55F + 0.45F * Mth.sin(ageInTicks * 0.3F));
            this.getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.eyes(RAGE)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, r * k, g * k, b * k, 1.0F);
        }
    }
}
