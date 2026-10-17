package com.witherplus.client;

import com.witherplus.client.vfx.AuraVfx;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public final class WitherStageClient {
    public static final int COLOR_STAGE_ONE = 0x5CF0FF;
    public static final int COLOR_STAGE_TWO = 0x2F8BFF;
    public static final int COLOR_STAGE_THREE = 0xFF5A1F;
    private static final Int2IntOpenHashMap STAGES = new Int2IntOpenHashMap();
    private static final Int2ObjectOpenHashMap<AuraVfx> AURAS = new Int2ObjectOpenHashMap<>();

    private WitherStageClient() {
    }

    public static void set(int witherId, int stage) {
        STAGES.put(witherId, stage);
        AuraVfx aura = AURAS.get(witherId);
        if (aura == null || aura.finished()) {
            aura = new AuraVfx(witherId);
            AURAS.put(witherId, aura);
            VfxManager.add(aura);
        }
    }

    public static int stage(int witherId) {
        return STAGES.getOrDefault(witherId, 0);
    }

    public static int colorOf(int witherId) {
        return color(Math.max(1, stage(witherId)));
    }

    public static float rage(int witherId) {
        AuraVfx aura = AURAS.get(witherId);
        if (aura == null) {
            return 0.0F;
        }
        return aura.rage();
    }

    public static int color(int stage) {
        if (stage >= 3) {
            return COLOR_STAGE_THREE;
        }
        if (stage == 2) {
            return COLOR_STAGE_TWO;
        }
        return COLOR_STAGE_ONE;
    }

    public static void remove(int witherId) {
        STAGES.remove(witherId);
        AURAS.remove(witherId);
    }

    public static void clear() {
        STAGES.clear();
        AURAS.clear();
    }
}
