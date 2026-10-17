package com.witherplus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.witherplus.WitherPlus;
import com.witherplus.WpConfig;
import net.minecraft.client.model.monster.wither.WitherBossModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public final class WitherGlowLayer extends RenderLayer<WitherRenderState, WitherBossModel> {
    private static final Identifier GLOW = WitherPlus.id("textures/entity/wither_glow.png");
    private static final Identifier RAGE = WitherPlus.id("textures/entity/wither_glow_rage.png");
    public WitherGlowLayer(RenderLayerParent<WitherRenderState, WitherBossModel> parent) { super(parent); }
    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, WitherRenderState state, float yRot, float xRot) {
        var visual = state.getRenderDataOrDefault(WitherVisualState.KEY, WitherVisualState.NONE);
        if (!WpConfig.ENABLE_GLOW_LAYER.get() || visual.invisible() || visual.stage() <= 0) return;
        int rgb = WitherStageClient.color(visual.stage());
        float r = ((rgb >> 16) & 255) / 255.0F;
        float g = ((rgb >> 8) & 255) / 255.0F;
        float b = (rgb & 255) / 255.0F;
        float pulse = 0.75F + 0.25F * Mth.sin(state.ageInTicks * 0.12F);
        collector.submitModel(getParentModel(), state, pose, RenderTypes.eyes(GLOW), 15728880, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(1.0F, r * pulse, g * pulse, b * pulse), null, state.outlineColor, null);
        if (visual.rage() > 0.01F) {
            float k = visual.rage() * (0.55F + 0.45F * Mth.sin(state.ageInTicks * 0.3F));
            collector.submitModel(getParentModel(), state, pose, RenderTypes.eyes(RAGE), 15728880, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(1.0F, r * k, g * k, b * k), null, state.outlineColor, null);
        }
    }
}
