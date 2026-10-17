package com.witherplus.client.vfx;

import com.witherplus.client.CameraShake;
import com.witherplus.client.Draw;
import com.witherplus.client.VfxInstance;
import com.witherplus.client.VfxManager;
import com.witherplus.wither.WpMath;
import com.witherplus.wither.skill.SoulPyreSkill;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.phys.Vec3;

public final class PyreVfx extends VfxInstance {
    private static final int SAMPLES = 14;
    private final int witherId;
    private final float centerAngle;
    private final float halfArc;
    private final int charge;
    private final int sweep;
    private final int rgb;

    public PyreVfx(int witherId, float centerAngle, float halfArc, int charge, int sweep, int rgb) {
        super(charge + sweep + 12);
        this.witherId = witherId;
        this.centerAngle = centerAngle;
        this.halfArc = halfArc;
        this.charge = charge;
        this.sweep = sweep;
        this.rgb = rgb;
    }

    private Vec3 aim(Vec3 base, int head, float progress) {
        Vec3 offset = WpMath.pyreOffset(this.centerAngle, this.halfArc, head, progress);
        double x = base.x + offset.x;
        double z = base.z + offset.z;
        return new Vec3(x, Draw.groundY(x, z, base.y - 7.0), z);
    }

    @Override
    protected void onTick() {
        WitherBoss wither = wither(this.witherId);
        if (wither == null) {
            return;
        }
        int s = this.age - this.charge;
        if (s < 0 || s > this.sweep) {
            return;
        }
        if (s % 6 == 0) {
            CameraShake.addAt(wither.position(), 0.25F, 30.0F, 8);
        }
        if (s % SoulPyreSkill.SCORCH_INTERVAL == 0) {
            for (int head = 0; head < 3; head++) {
                VfxManager.add(new ScorchVfx(this.aim(wither.position(), head, s / (float) this.sweep), 1.8F, 80, this.rgb));
            }
        }
    }

    @Override
    public void render(float partialTick) {
        WitherBoss wither = wither(this.witherId);
        if (wither == null) {
            return;
        }
        float t = this.time(partialTick);
        int hot = Draw.mix(this.rgb, 0xFFFFFF, 0.6F);
        if (t < this.charge) {
            float f = t / this.charge;
            if (Draw.begin(Draw.Material.FLAME)) {
                for (int head = 0; head < 3; head++) {
                    Vec3 hp = head(wither, head, partialTick);
                    Draw.billboard(hp, 0.6F + 1.8F * f, hot, f);
                    for (int i = 0; i < 6; i++) {
                        float phase = (t * 0.08F + i / 6.0F) % 1.0F;
                        float angle = i * 1.0472F + head * 0.7F + t * 0.1F;
                        float dist = (1.0F - phase) * 2.6F;
                        Vec3 p = hp.add(Mth.cos(angle) * dist, (hash(i + head * 11) - 0.5F) * dist, Mth.sin(angle) * dist);
                        Draw.billboard(p, 0.35F, this.rgb, phase * f);
                    }
                }
                Draw.end(Draw.FLAME_BALL, 1.2F, 2.0F, 1.0F);
            }
            return;
        }
        float s = t - this.charge;
        float progress = Mth.clamp(s / this.sweep, 0.0F, 1.0F);
        float alpha = Mth.clamp(s / 3.0F, 0.0F, 1.0F) * Mth.clamp((this.sweep + 8.0F - s) / 8.0F, 0.0F, 1.0F);
        if (alpha <= 0.0F) {
            return;
        }
        Vec3 base = lerpPos(wither, partialTick);
        Vec3[][] streams = new Vec3[3][];
        Vec3[] aims = new Vec3[3];
        for (int head = 0; head < 3; head++) {
            Vec3 from = head(wither, head, partialTick);
            Vec3 to = this.aim(base, head, progress);
            aims[head] = to;
            Vec3 control = from.add(to).scale(0.5).add(0.0, 1.5, 0.0);
            Vec3[] points = new Vec3[SAMPLES];
            for (int i = 0; i < SAMPLES; i++) {
                points[i] = WpMath.bezier(from, control, to, i / (double) (SAMPLES - 1));
            }
            streams[head] = points;
        }
        float[] widths = new float[SAMPLES];
        float[] alphas = new float[SAMPLES];
        float[] coreWidths = new float[SAMPLES];
        for (int i = 0; i < SAMPLES; i++) {
            float k = i / (float) (SAMPLES - 1);
            widths[i] = (0.5F + 2.4F * k) * (0.9F + 0.2F * Mth.sin(t * 0.9F + i));
            coreWidths[i] = widths[i] * 0.38F;
            alphas[i] = alpha * (0.95F - 0.3F * k);
        }
        if (Draw.begin(Draw.Material.SMOKE)) {
            float[] smokeWidths = new float[SAMPLES];
            float[] smokeAlphas = new float[SAMPLES];
            for (int i = 0; i < SAMPLES; i++) {
                smokeWidths[i] = widths[i] * 1.5F;
                smokeAlphas[i] = alphas[i] * 0.4F;
            }
            for (Vec3[] points : streams) {
                Vec3[] lifted = new Vec3[SAMPLES];
                for (int i = 0; i < SAMPLES; i++) {
                    lifted[i] = points[i].add(0.0, 0.5 + i * 0.05, 0.0);
                }
                Draw.ribbon(lifted, smokeWidths, smokeAlphas, Draw.ASH, 0.0F, 1.0F);
            }
            Draw.end(Draw.FLAME_SMOKE, 1.0F, 1.0F, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (Vec3[] points : streams) {
                Draw.ribbon(points, widths, alphas, this.rgb, 0.0F, 2.0F);
                Draw.ribbon(points, coreWidths, alphas, hot, 0.0F, 2.0F);
            }
            Draw.end(Draw.FLAME_STRIP, 2.4F, 1.0F, 0.8F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (Vec3 to : aims) {
                Draw.billboard(to.add(0.0, 0.6, 0.0), 3.4F, this.rgb, alpha * 0.9F);
            }
            Draw.end(Draw.FLAME_BALL, 1.0F, 2.0F, 0.6F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (int head = 0; head < 3; head++) {
                for (int i = 0; i < 3; i++) {
                    float angle = i * 2.0944F + head;
                    Vec3 p = aims[head].add(Mth.cos(angle) * 0.6, 0.0, Mth.sin(angle) * 0.6);
                    Draw.uprightBillboard(p, 1.6F, 2.2F + 0.6F * Mth.sin(t * 0.7F + i + head), this.rgb, alpha, head * 3 + i);
                }
            }
            Draw.end(Draw.FLAME_TONGUE, 2.0F, 1.0F, 0.6F);
        }
    }
}
