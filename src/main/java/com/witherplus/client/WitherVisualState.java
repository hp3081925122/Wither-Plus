package com.witherplus.client;

import com.witherplus.WitherPlus;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.boss.wither.WitherBoss;

public record WitherVisualState(int stage, float rage, boolean invisible) {
    public static final ContextKey<WitherVisualState> KEY = new ContextKey<>(WitherPlus.id("wither_visuals"));
    public static final WitherVisualState NONE = new WitherVisualState(0, 0, false);
    public static WitherVisualState of(WitherBoss wither) {
        return new WitherVisualState(WitherStageClient.stage(wither.getId()), WitherStageClient.rage(wither.getId()), wither.isInvisible());
    }
}
