package com.witherplus.client.vfx;

import com.witherplus.client.CameraShake;
import com.witherplus.client.Draw;
import com.witherplus.client.VfxInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class DashVfx extends VfxInstance {
    private static final int SAMPLES = 12;
    private static final int LINGER = 24;
    private final Vec3 from;
    private final Vec3 to;
    private final int telegraph;
    private final int duration;
    private final int rgb;

    public DashVfx(Vec3 from, Vec3 to, int telegraph, int duration, int rgb) {
        super(telegraph + duration + LINGER);
        this.from = from;
        this.to = to;
        this.telegraph = telegraph;
        this.duration = Math.max(1, duration);
        this.rgb = rgb;
    }

    @Override
    protected void onTick() {
        if (this.age == this.telegraph) {
            CameraShake.addAt(this.from.lerp(this.to, 0.5), 0.8F, 24.0F, 12);
        }
    }

    @Override
    public void render(float partialTick) {
        float t = this.time(partialTick);
        int hot = Draw.mix(this.rgb, 0xFFFFFF, 0.5F);
        if (t < this.telegraph) {
            float k = t / this.telegraph;
            float flicker = 0.7F + 0.3F * Mth.sin(t * 2.3F);
            if (Draw.begin(Draw.Material.FLAME)) {
                Draw.beam(this.from, this.to, 0.15F + 0.35F * k, this.rgb, k * flicker, 4.0F);
                Draw.end(Draw.FLAME_STRIP, 3.0F, 1.0F, 0.5F);
            }
            if (Draw.begin(Draw.Material.FLAME)) {
                for (int i = 0; i < 3; i++) {
                    float angle = i * 2.0944F + t * 0.2F;
                    Draw.billboard(this.from.add(Mth.cos(angle) * 1.2, Mth.sin(t * 0.25F + i) * 0.4, Mth.sin(angle) * 1.2), 1.4F, this.rgb, k);
                }
                Draw.end(Draw.FLAME_SKULL, 1.0F, 1.0F, 0.0F);
            }
            return;
        }
        float dk = Mth.clamp((t - this.telegraph) / this.duration, 0.0F, 1.0F);
        float linger = Mth.clamp(1.0F - (t - this.telegraph - this.duration) / LINGER, 0.0F, 1.0F);
        Vec3 headPos = this.from.lerp(this.to, dk);
        Vec3[] points = new Vec3[SAMPLES];
        float[] widths = new float[SAMPLES];
        float[] smokeWidths = new float[SAMPLES];
        float[] alphas = new float[SAMPLES];
        float[] smokeAlphas = new float[SAMPLES];
        for (int i = 0; i < SAMPLES; i++) {
            float s = i / (float) (SAMPLES - 1);
            points[i] = this.from.lerp(headPos, s);
            widths[i] = 0.4F + 1.4F * s;
            smokeWidths[i] = 1.2F + 2.8F * s;
            alphas[i] = linger * (0.2F + 0.8F * s);
            smokeAlphas[i] = linger * (0.15F + 0.55F * s);
        }
        if (Draw.begin(Draw.Material.SMOKE)) {
            Draw.ribbon(points, smokeWidths, smokeAlphas, Draw.ASH, 0.0F, 2.0F);
            if (dk < 1.0F) {
                for (int i = 0; i < 8; i++) {
                    float angle = hash(i) * Mth.TWO_PI + t * 0.1F;
                    Draw.billboard(headPos.add(Mth.cos(angle) * 1.2, (hash(i + 3) - 0.5) * 2.0, Mth.sin(angle) * 1.2), 3.0F, angle, Draw.ASH, 0.8F);
                }
            }
            Draw.end(Draw.FLAME_SMOKE, 1.0F, 1.2F, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            Draw.ribbon(points, widths, alphas, this.rgb, 0.0F, 3.0F);
            Draw.end(Draw.FLAME_STRIP, 3.0F, 1.0F, 0.8F);
        }
        if (dk < 1.0F && Draw.begin(Draw.Material.FLAME)) {
            Vec3 axis = this.to.subtract(this.from);
            Vec3 side = Draw.basis(axis)[0];
            Draw.billboard(headPos, 1.8F, hot, 1.0F);
            Draw.billboard(headPos.add(side.scale(1.3)).add(0.0, -0.6, 0.0), 1.3F, this.rgb, 1.0F);
            Draw.billboard(headPos.subtract(side.scale(1.3)).add(0.0, -0.6, 0.0), 1.3F, this.rgb, 1.0F);
            Draw.end(Draw.FLAME_SKULL, 1.0F, 1.0F, 0.0F);
        }
    }
}
