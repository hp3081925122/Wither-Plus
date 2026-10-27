package com.witherplus;

import com.witherplus.wither.WitherBrainHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import java.util.HashMap;
import java.util.Map;

public final class SpecialSummonTracker {
    public static final String SKULL_TAG = "WitherPlusSpecial";
    private static final Map<Level, BlockPos> PENDING = new HashMap<>();
    private SpecialSummonTracker() {
    }

    public static boolean isSpecial(SkullBlockEntity skull) {
        return skull.getPersistentData().getBoolean(SKULL_TAG);
    }

    public static void markSkull(Level level, BlockPos pos) {
        PENDING.put(level, pos.immutable());
    }

    public static void markFromPattern(Level level, BlockPos center) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -2, -2), center.offset(2, 2, 2))) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SkullBlockEntity skull && isSpecial(skull)) {
                markSkull(level, pos);
                return;
            }
        }
    }

    public static void applyIfPending(Level level, WitherBoss wither) {
        BlockPos pos = PENDING.get(level);
        if (pos != null && pos.distSqr(wither.blockPosition()) <= 36.0) {
            PENDING.remove(level);
            ((WitherBrainHolder) wither).wp$getBrain().setSpecialSummon(true);
        }
    }
}

