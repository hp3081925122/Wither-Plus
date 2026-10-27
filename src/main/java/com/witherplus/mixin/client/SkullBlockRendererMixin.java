package com.witherplus.mixin.client;

import com.witherplus.SpecialSummonTracker;
import com.witherplus.client.SpecialSkullVisuals;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.SkullBlockRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/SkullBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/SkullBlockRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V", at = @At("TAIL"))
    private void wp$specialSkullTexture(SkullBlockEntity skull, SkullBlockRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress, CallbackInfo ci) {
        if (SpecialSummonTracker.isSpecial(skull)) state.renderType = SpecialSkullVisuals.renderType();
    }
}
