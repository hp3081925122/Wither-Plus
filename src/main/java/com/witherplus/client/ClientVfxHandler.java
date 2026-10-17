package com.witherplus.client;

import com.witherplus.WitherPlus;
import com.witherplus.WpConfig;
import com.witherplus.client.vfx.BlackSunVfx;
import com.witherplus.client.vfx.BoltVfx;
import com.witherplus.client.vfx.BurstVfx;
import com.witherplus.client.vfx.ChainVfx;
import com.witherplus.client.vfx.CrownVfx;
import com.witherplus.client.vfx.DashVfx;
import com.witherplus.client.vfx.MaelstromVfx;
import com.witherplus.client.vfx.MarkVfx;
import com.witherplus.client.vfx.MeteorVfx;
import com.witherplus.client.vfx.PillarVfx;
import com.witherplus.client.vfx.PyreVfx;
import com.witherplus.client.vfx.RequiemVfx;
import com.witherplus.client.vfx.ShadeVfx;
import com.witherplus.client.vfx.SpiresVfx;
import com.witherplus.client.vfx.TideVfx;
import com.witherplus.network.VfxPayload;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public final class ClientVfxHandler {
    private ClientVfxHandler() {
    }

    public static void handle(VfxPayload payload) {
        VfxManager.checkLevel();
        CompoundTag d = payload.data();
        int id = payload.entityId();
        if (WpConfig.DEBUG_LOGGING.get()) {
            WitherPlus.LOGGER.info("Received wither vfx packet type {} for entity {}", payload.kind(), id);
        }
        int color = WitherStageClient.colorOf(id);
        switch (payload.kind()) {
            case VfxPayload.STAGE -> {
                int previous = WitherStageClient.stage(id);
                int stage = d.getIntOr("stage", 0);
                WitherPlus.LOGGER.info("Wither {} stage synced on client: {} -> {}", id, previous, stage);
                WitherStageClient.set(id, stage);
                if (previous > 0 && stage > previous) {
                    ScreenFx.flash(0.5F, WitherStageClient.color(stage));
                    CameraShake.add(1.2F, 30);
                }
            }
            case VfxPayload.PYRE -> VfxManager.add(new PyreVfx(id, d.getFloatOr("center", 0.0F), d.getFloatOr("half", 0.0F), d.getIntOr("charge", 0), d.getIntOr("sweep", 0), color));
            case VfxPayload.METEOR -> VfxManager.add(new MeteorVfx(VfxPayload.getPoints(d, "impact"), VfxPayload.getPoints(d, "sky"), d.getIntArray("times").orElseGet(() -> new int[0]), d.getIntOr("fall", 0), d.getFloatOr("radius", 0.0F), color));
            case VfxPayload.CHAIN -> VfxManager.add(new ChainVfx(id, d.getIntArray("targets").orElseGet(() -> new int[0]), d.getIntOr("extend", 0), d.getIntOr("hold", 0), color));
            case VfxPayload.CHAIN_END -> {
                ChainVfx.release(id, d.getIntOr("target", 0));
                Vec3 pos = VfxPayload.getVec(d, "pos");
                float radius = 1.5F;
                if (!d.getBooleanOr("snapped", false)) {
                    radius = 2.5F;
                }
                VfxManager.add(new BurstVfx(pos, radius, color, false));
            }
            case VfxPayload.SHADE -> VfxManager.add(new ShadeVfx(VfxPayload.getVec(d, "pos"), d.getBooleanOr("on", false), color));
            case VfxPayload.DASH -> VfxManager.add(new DashVfx(VfxPayload.getVec(d, "from"), VfxPayload.getVec(d, "to"), d.getIntOr("telegraph", 0), d.getIntOr("duration", 0), color));
            case VfxPayload.SPIRES -> VfxManager.add(new SpiresVfx(VfxPayload.getPoints(d, "points"), d.getIntArray("delays").orElseGet(() -> new int[0]), VfxPayload.getVec(d, "origin"), color));
            case VfxPayload.BLACK_SUN -> VfxManager.add(new BlackSunVfx(VfxPayload.getVec(d, "pos"), d.getIntOr("form", 0), d.getIntOr("active", 0), d.getIntOr("collapse", 0), color));
            case VfxPayload.BOLTS -> {
                List<Vec3> starts = VfxPayload.getPoints(d, "start");
                List<Vec3> controls = VfxPayload.getPoints(d, "control");
                List<Vec3> ends = VfxPayload.getPoints(d, "end");
                int[] travels = d.getIntArray("travel").orElseGet(() -> new int[0]);
                int count = Math.min(Math.min(starts.size(), controls.size()), Math.min(ends.size(), travels.length));
                for (int i = 0; i < count; i++) {
                    VfxManager.add(new BoltVfx(starts.get(i), controls.get(i), ends.get(i), travels[i], d.getIntOr("style", 0), color));
                }
            }
            case VfxPayload.MAELSTROM -> VfxManager.add(new MaelstromVfx(id, VfxPayload.getVec(d, "center"), d.getFloatOr("base", 0.0F), d.getIntOr("duration", 0), d.getFloatOr("radius", 0.0F), d.getIntOr("arms", 0), color));
            case VfxPayload.MARK -> VfxManager.add(new MarkVfx(d.getIntArray("targets").orElseGet(() -> new int[0]), d.getIntOr("lock", 0), color));
            case VfxPayload.PILLAR -> {
                for (Vec3 point : VfxPayload.getPoints(d, "points")) {
                    VfxManager.add(new PillarVfx(point, d.getIntOr("delay", 0), d.getIntOr("style", 0), d.getFloatOr("radius", 0.0F), color));
                }
            }
            case VfxPayload.TIDE -> VfxManager.add(new TideVfx(VfxPayload.getVec(d, "center"), d.getFloatOr("speed", 0.0F), d.getFloatOr("radius", 0.0F), d.getFloatOr("gap", 0.0F), d.getFloatOr("gapHalf", 0.0F), d.getFloatOr("height", 0.0F), d.getBooleanOr("jumpable", false), color));
            case VfxPayload.CROWN -> VfxManager.add(new CrownVfx(id, VfxPayload.getVec(d, "center"), d.getIntOr("duration", 0), d.getFloatOr("radius", 0.0F), color));
            case VfxPayload.REQUIEM -> VfxManager.add(new RequiemVfx(id, d, color));
            case VfxPayload.BURST -> VfxManager.add(new BurstVfx(VfxPayload.getVec(d, "pos"), d.getFloatOr("radius", 0.0F), color, true));
            default -> {
            }
        }
    }
}
