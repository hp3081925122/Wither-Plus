package com.witherplus.mixin;

import com.witherplus.SpecialSummonTracker;
import com.witherplus.item.WpItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.WitherSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherSkullBlock.class)
public abstract class WitherSkullBlockMixin {
    @Inject(method = "setPlacedBy", at = @At("HEAD"))
    private void wp$markPlaced(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci) {
        if (stack.is(WpItems.SPECIAL_WITHER_SKELETON_SKULL.get()) && level.getBlockEntity(pos) instanceof SkullBlockEntity skull) {
            skull.getPersistentData().putBoolean(SpecialSummonTracker.SKULL_TAG, true);
            skull.setChanged();
            if (!level.isClientSide) {
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            }
        }
    }

    @Inject(
            method = "checkSpawn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/SkullBlockEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/CarvedPumpkinBlock;clearPatternBlocks(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/pattern/BlockPattern$BlockPatternMatch;)V")
    )
    private static void wp$markPattern(Level level, BlockPos pos, SkullBlockEntity skull, CallbackInfo ci) {
        SpecialSummonTracker.markFromPattern(level, pos);
    }
}
