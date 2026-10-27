package com.witherplus.mixin.client;

import com.witherplus.SpecialSummonTracker;
import com.witherplus.client.SpecialSkullVisuals;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin {
    @ModifyVariable(method = "render(Lnet/minecraft/world/level/block/entity/SkullBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("STORE"))
    private RenderType wp$specialSkullTexture(RenderType renderType, SkullBlockEntity skull) {
        return SpecialSummonTracker.isSpecial(skull) ? SpecialSkullVisuals.renderType() : renderType;
    }
}
