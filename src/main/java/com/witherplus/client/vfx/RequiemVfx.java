package com.witherplus.client.vfx;

import com.witherplus.client.CameraShake;
import com.witherplus.client.Draw;
import com.witherplus.client.ScreenFx;
import com.witherplus.client.VfxInstance;
import com.witherplus.client.VfxManager;
import com.witherplus.network.VfxPayload;
import com.witherplus.wither.WpMath;
import com.witherplus.wither.hazard.CheckerHazard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class RequiemVfx extends VfxInstance {
    private static final int TELEGRAPH = 45;
    private static final int ERUPT = 16;
    private final Vec3 center;
    private final float radius;
    private final float cell;
    private final int waveA;
    private final int waveB;
    private final int ring;
    private final float ringSpeed;
    private final float ringRadius;
    private final int duration;
    private final int rgb;
    private final List<Vec3> evenCells = new ArrayList<>();
    private final List<Vec3> oddCells = new ArrayList<>();

    public RequiemVfx(int witherId, CompoundTag d, int rgb) {
        super(d.getInt("duration") + 10);
        this.center = VfxPayload.getVec(d, "center");
        this.radius = d.getFloat("radius");
        this.cell = Math.max(1.0F, d.getFloat("cell"));
        this.waveA = d.getInt("waveA");
        this.waveB = d.getInt("waveB");
        this.ring = d.getInt("ring");
        this.ringSpeed = d.getFloat("ringSpeed");
        this.ringRadius = d.getFloat("ringRadius");
        this.duration = d.getInt("duration");
        this.rgb = rgb;
        int count = Mth.ceil(this.radius / this.cell);
        for (int ix = -count; ix < count; ix++) {
            for (int iz = -count; iz < count; iz++) {
                double x = this.center.x + (ix + 0.5) * this.cell;
                double z = this.center.z + (iz + 0.5) * this.cell;
                double dx = x - this.center.x;
                double dz = z - this.center.z;
                if (dx * dx + dz * dz > this.radius * this.radius) {
                    continue;
                }
                double y = Mth.clamp(Draw.groundY(x, z, this.center.y), this.center.y - 3.0, this.center.y + 4.0);
                Vec3 p = new Vec3(x, y, z);
                if (CheckerHazard.parityAt(this.center, this.cell, x, z) == 0) {
                    this.evenCells.add(p);
                } else {
                    this.oddCells.add(p);
                }
            }
        }
    }

    @Override
    protected void onTick() {
        if (this.age < this.duration && distanceToPlayer(this.center) < this.radius + 30.0) {
            ScreenFx.sustain(0.75F, 0.55F, 0x05060A);
        }
        if (this.age == this.waveA || this.age == this.waveB) {
            CameraShake.addAt(this.center, 1.4F, this.radius * 2.5F, 18);
            if (distanceToPlayer(this.center) < this.radius + 6.0) {
                ScreenFx.flash(0.35F, this.rgb);
            }
        }
        if (this.age == this.ring) {
            VfxManager.add(new TideVfx(this.center, this.ringSpeed, this.ringRadius, 0.0F, 0.0F, 1.2F, true, this.rgb));
            CameraShake.addAt(this.center, 1.0F, this.ringRadius * 1.5F, 14);
        }
    }

    @Override
    public void render(float partialTick) {
        float t = this.time(partialTick);
        float alpha = this.fade(partialTick, 15.0F, 10.0F);
        if (Draw.begin(Draw.Material.GROUND)) {
            Draw.ground(this.center, this.radius + 4.0F, this.rgb, alpha);
            Draw.end(Draw.GROUND_SIGIL, Mth.clamp(t / 50.0F, 0.0F, 1.0F), 7.7F, 0.0F);
        }
        this.renderWave(t, this.waveA, 0, this.evenCells);
        this.renderWave(t, this.waveB, 1, this.oddCells);
        if (Draw.begin(Draw.Material.SMOKE)) {
            for (int i = 0; i < 40; i++) {
                float phase = (hash(i) + t * 0.012F) % 1.0F;
                float angle = hash(i + 97) * Mth.TWO_PI;
                float dist = Mth.sqrt(hash(i + 193)) * this.radius;
                Vec3 p = this.center.add(Mth.cos(angle) * dist, 18.0 - phase * 18.0, Mth.sin(angle) * dist);
                float a = alpha * Mth.sin(phase * Mth.PI) * 0.7F;
                Draw.billboard(p, 0.5F + hash(i + 7) * 0.4F, phase * 6.0F + i, Draw.ASH, a);
            }
            Draw.end(Draw.FLAME_SMOKE, 0.5F, 2.0F, 0.0F);
        }
    }

    private void renderWave(float t, int wave, int parity, List<Vec3> cells) {
        float start = wave - TELEGRAPH;
        if (t < start || t > wave + ERUPT) {
            return;
        }
        if (t < wave) {
            float prog = (t - start) / TELEGRAPH;
            if (Draw.begin(Draw.Material.GROUND)) {
                Draw.ground(this.center, this.radius, this.rgb, 1.0F);
                Draw.end(Draw.GROUND_CHECKER, prog, parity, this.radius / this.cell);
            }
            return;
        }
        float local = t - wave;
        float grow = WpMath.easeOut(local / 3.0F);
        float out = Mth.clamp(1.0F - (local - 5.0F) / (ERUPT - 5.0F), 0.0F, 1.0F);
        float half = this.cell * 0.5F;
        int hot = Draw.mix(this.rgb, 0xFFFFFF, 0.55F);
        if (Draw.begin(Draw.Material.GROUND)) {
            for (Vec3 p : cells) {
                Draw.ground(p, half, this.rgb, out);
            }
            Draw.end(Draw.GROUND_SOULFIRE, 0.0F, 3.1F, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (Vec3 p : cells) {
                float h = (7.0F + 3.0F * hash((int) (p.x * 31.0 + p.z * 17.0))) * grow;
                Draw.cylinder(p, p.add(0.0, h, 0.0), half * 0.95F * grow, half * 0.35F, 10, this.rgb, out, 2.0F);
            }
            Draw.end(Draw.FLAME_COLUMN, 3.2F, 1.4F, 1.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            float flash = Mth.clamp(1.0F - local / 6.0F, 0.0F, 1.0F);
            for (Vec3 p : cells) {
                Draw.billboard(p.add(0.0, 0.8, 0.0), this.cell * 1.2F * grow, hot, flash);
            }
            Draw.end(Draw.FLAME_BALL, 1.0F, 2.0F, 1.0F);
        }
    }
}
