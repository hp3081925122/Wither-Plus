package com.witherplus.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.witherplus.client.SpecialSkullVisuals;
import com.witherplus.item.WpItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin {
    @ModifyVariable(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("STORE"))
    private RenderType wp$specialSkullTexture(RenderType renderType, PoseStack pose, MultiBufferSource buffers, int light, LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(WpItems.SPECIAL_WITHER_SKELETON_SKULL.get()) ? SpecialSkullVisuals.renderType() : renderType;
    }
}
