package com.witherplus.client;

import com.witherplus.WitherPlus;
import com.witherplus.WpConfig;
import com.witherplus.client.vfx.ChainVfx;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class VfxManager {
    private static final int MAX_INSTANCES = 256;
    private static final List<VfxInstance> INSTANCES = new ArrayList<>();
    private static final List<VfxInstance> PENDING = new ArrayList<>();
    private static ClientLevel lastLevel;
    private static boolean renderLogged;

    private VfxManager() {
    }

    public static void add(VfxInstance instance) {
        PENDING.add(instance);
    }

    public static void clear() {
        renderLogged = false;
        INSTANCES.clear();
        PENDING.clear();
        ChainVfx.clearAll();
        WitherStageClient.clear();
    }

    public static void checkLevel() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != lastLevel) {
            lastLevel = mc.level;
            WitherPlus.LOGGER.info("Client level changed, cleared wither vfx state");
            clear();
            CameraShake.clear();
            ScreenFx.clear();
        }
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        checkLevel();
        if (mc.level == null || mc.isPaused()) {
            return;
        }
        INSTANCES.addAll(PENDING);
        PENDING.clear();
        INSTANCES.removeIf(instance -> !instance.tick());
        INSTANCES.addAll(PENDING);
        PENDING.clear();
        while (INSTANCES.size() > MAX_INSTANCES) {
            int victim = -1;
            for (int i = 0; i < INSTANCES.size(); i++) {
                if (INSTANCES.get(i).disposable()) {
                    victim = i;
                    break;
                }
            }
            if (victim < 0) {
                break;
            }
            INSTANCES.remove(victim);
        }
        CameraShake.tick();
        ScreenFx.tick();
    }

    public static void render(RenderLevelStageEvent.AfterLevel event) {
        if (INSTANCES.isEmpty()) return;
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        if (WpConfig.DEBUG_LOGGING.get() && !renderLogged) {
            renderLogged = true;
            WitherPlus.LOGGER.info("Wither VFX render active: instances={}", INSTANCES.size());
        }
        Draw.setup(event.getLevelRenderState().cameraRenderState);
        WpGpu.MODEL_VIEW.set(event.getModelViewMatrix());
        for (VfxInstance instance : INSTANCES) {
            instance.render(partialTick);
        }
    }
}
