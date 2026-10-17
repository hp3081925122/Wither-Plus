package com.witherplus.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.witherplus.client.WitherVisualState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnergySwirlLayer.class)
public abstract class EnergySwirlLayerMixin {
    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/EntityRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    private void wp$hideWhenInvisible(PoseStack pose, SubmitNodeCollector collector, int light, EntityRenderState state, float yRot, float xRot, CallbackInfo ci) {
        if (state instanceof WitherRenderState && state.getRenderDataOrDefault(WitherVisualState.KEY, WitherVisualState.NONE).invisible()) ci.cancel();
    }
}
