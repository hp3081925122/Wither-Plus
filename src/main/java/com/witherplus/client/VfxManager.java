package com.witherplus.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.witherplus.WitherPlus;
import com.witherplus.WpConfig;
import com.witherplus.client.vfx.ChainVfx;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public final class VfxManager {
    private static final int MAX_INSTANCES = 256;
    private static final List<VfxInstance> INSTANCES = new ArrayList<>();
    private static final List<VfxInstance> PENDING = new ArrayList<>();
    private static final Matrix4f WORLD_VIEW = new Matrix4f();
    private static ClientLevel lastLevel;
    private static boolean worldViewReady;
    private static boolean renderLogged;

    private VfxManager() {
    }

    public static void add(VfxInstance instance) {
        PENDING.add(instance);
    }

    public static void clear() {
        worldViewReady = false;
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

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            WORLD_VIEW.set(event.getPoseStack().last().pose());
            worldViewReady = true;
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL || INSTANCES.isEmpty() || !worldViewReady) {
            return;
        }
        float partialTick = event.getPartialTick();
        if (WpConfig.DEBUG_LOGGING.get() && !renderLogged) {
            renderLogged = true;
            WitherPlus.LOGGER.info("Wither VFX render active: instances={}, flameShader={}, groundShader={}", INSTANCES.size(), WpShaders.flame != null, WpShaders.ground != null);
        }
        Draw.setup(event.getCamera());
        PoseStack stack = RenderSystem.getModelViewStack();
        stack.pushPose();
        stack.mulPoseMatrix(WORLD_VIEW);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        for (VfxInstance instance : INSTANCES) {
            instance.render(partialTick);
        }
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        stack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
