package com.witherplus.mixin.client;

import com.witherplus.client.SpecialSkullVisuals;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.SkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin {
    @Inject(method = "resolveSkullRenderType", at = @At("HEAD"), cancellable = true)
    private void wp$specialSkullTexture(LivingEntityRenderState state, SkullBlock.Type type, CallbackInfoReturnable<RenderType> cir) {
        if (state.getRenderDataOrDefault(SpecialSkullVisuals.WORN, false)) cir.setReturnValue(SpecialSkullVisuals.renderType());
    }
}
