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
                int stage = d.getInt("stage");
                WitherPlus.LOGGER.info("Wither {} stage synced on client: {} -> {}", id, previous, stage);
                WitherStageClient.set(id, stage);
                if (previous > 0 && stage > previous) {
                    ScreenFx.flash(0.5F, WitherStageClient.color(stage));
                    CameraShake.add(1.2F, 30);
                }
            }
            case VfxPayload.PYRE -> VfxManager.add(new PyreVfx(id, d.getFloat("center"), d.getFloat("half"), d.getInt("charge"), d.getInt("sweep"), color));
            case VfxPayload.METEOR -> VfxManager.add(new MeteorVfx(VfxPayload.getPoints(d, "impact"), VfxPayload.getPoints(d, "sky"), d.getIntArray("times"), d.getInt("fall"), d.getFloat("radius"), color));
            case VfxPayload.CHAIN -> VfxManager.add(new ChainVfx(id, d.getIntArray("targets"), d.getInt("extend"), d.getInt("hold"), color));
            case VfxPayload.CHAIN_END -> {
                ChainVfx.release(id, d.getInt("target"));
                Vec3 pos = VfxPayload.getVec(d, "pos");
                float radius = 1.5F;
                if (!d.getBoolean("snapped")) {
                    radius = 2.5F;
                }
                VfxManager.add(new BurstVfx(pos, radius, color, false));
            }
            case VfxPayload.SHADE -> VfxManager.add(new ShadeVfx(VfxPayload.getVec(d, "pos"), d.getBoolean("on"), color));
            case VfxPayload.DASH -> VfxManager.add(new DashVfx(VfxPayload.getVec(d, "from"), VfxPayload.getVec(d, "to"), d.getInt("telegraph"), d.getInt("duration"), color));
            case VfxPayload.SPIRES -> VfxManager.add(new SpiresVfx(VfxPayload.getPoints(d, "points"), d.getIntArray("delays"), VfxPayload.getVec(d, "origin"), color));
            case VfxPayload.BLACK_SUN -> VfxManager.add(new BlackSunVfx(VfxPayload.getVec(d, "pos"), d.getInt("form"), d.getInt("active"), d.getInt("collapse"), color));
            case VfxPayload.BOLTS -> {
                List<Vec3> starts = VfxPayload.getPoints(d, "start");
                List<Vec3> controls = VfxPayload.getPoints(d, "control");
                List<Vec3> ends = VfxPayload.getPoints(d, "end");
                int[] travels = d.getIntArray("travel");
                int count = Math.min(Math.min(starts.size(), controls.size()), Math.min(ends.size(), travels.length));
                for (int i = 0; i < count; i++) {
                    VfxManager.add(new BoltVfx(starts.get(i), controls.get(i), ends.get(i), travels[i], d.getInt("style"), color));
                }
            }
            case VfxPayload.MAELSTROM -> VfxManager.add(new MaelstromVfx(id, VfxPayload.getVec(d, "center"), d.getFloat("base"), d.getInt("duration"), d.getFloat("radius"), d.getInt("arms"), color));
            case VfxPayload.MARK -> VfxManager.add(new MarkVfx(d.getIntArray("targets"), d.getInt("lock"), color));
            case VfxPayload.PILLAR -> {
                for (Vec3 point : VfxPayload.getPoints(d, "points")) {
                    VfxManager.add(new PillarVfx(point, d.getInt("delay"), d.getInt("style"), d.getFloat("radius"), color));
                }
            }
            case VfxPayload.TIDE -> VfxManager.add(new TideVfx(VfxPayload.getVec(d, "center"), d.getFloat("speed"), d.getFloat("radius"), d.getFloat("gap"), d.getFloat("gapHalf"), d.getFloat("height"), d.getBoolean("jumpable"), color));
            case VfxPayload.CROWN -> VfxManager.add(new CrownVfx(id, VfxPayload.getVec(d, "center"), d.getInt("duration"), d.getFloat("radius"), color));
            case VfxPayload.REQUIEM -> VfxManager.add(new RequiemVfx(id, d, color));
            case VfxPayload.BURST -> VfxManager.add(new BurstVfx(VfxPayload.getVec(d, "pos"), d.getFloat("radius"), color, true));
            default -> {
            }
        }
    }
}
