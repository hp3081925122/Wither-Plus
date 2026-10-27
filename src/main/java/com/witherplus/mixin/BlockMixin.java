package com.witherplus.mixin;

import com.witherplus.SpecialSummonTracker;
import com.witherplus.WitherPlus;
import com.witherplus.WpConfig;
import com.witherplus.item.WpItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "getCloneItemStack(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void wp$pickSpecialSkull(LevelReader level, BlockPos pos, BlockState state, CallbackInfoReturnable<ItemStack> cir) {
        if (!state.is(Blocks.WITHER_SKELETON_SKULL) && !state.is(Blocks.WITHER_SKELETON_WALL_SKULL)) {
            return;
        }
        boolean special = level.getBlockEntity(pos) instanceof SkullBlockEntity skull && SpecialSummonTracker.isSpecial(skull);
        if (WpConfig.DEBUG_LOGGING.get()) {
            WitherPlus.LOGGER.info("Pick wither skull at {}, special={}", pos, special);
        }
        if (special) {
            cir.setReturnValue(new ItemStack(WpItems.SPECIAL_WITHER_SKELETON_SKULL.get()));
        }
    }
}
