package com.witherplus.client.vfx;

import com.witherplus.client.Draw;
import com.witherplus.client.VfxInstance;
import com.witherplus.client.VfxManager;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class MeteorVfx extends VfxInstance {
    private static final int TRAIL = 8;
    private final List<Vec3> impacts;
    private final List<Vec3> skies;
    private final int[] times;
    private final int fall;
    private final float radius;
    private final int rgb;

    public MeteorVfx(List<Vec3> impacts, List<Vec3> skies, int[] times, int fall, float radius, int rgb) {
        super(maxOf(times) + 30);
        this.impacts = impacts;
        this.skies = skies;
        this.times = times;
        this.fall = Math.max(1, fall);
        this.radius = radius;
        this.rgb = rgb;
    }

    private static int maxOf(int[] values) {
        int max = 0;
        for (int v : values) {
            max = Math.max(max, v);
        }
        return max;
    }

    private int count() {
        return Math.min(this.times.length, Math.min(this.impacts.size(), this.skies.size()));
    }

    @Override
    protected void onTick() {
        for (int i = 0; i < this.count(); i++) {
            if (this.age == this.times[i]) {
                VfxManager.add(new BurstVfx(this.impacts.get(i).add(0.0, 0.5, 0.0), this.radius, this.rgb, true));
            }
        }
    }

    private Vec3 skullPos(int i, float k) {
        float e = k * k;
        return this.skies.get(i).lerp(this.impacts.get(i), e);
    }

    @Override
    public void render(float partialTick) {
        float t = this.time(partialTick);
        int n = this.count();
        for (int i = 0; i < n; i++) {
            if (t >= this.times[i]) {
                continue;
            }
            if (Draw.begin(Draw.Material.GROUND)) {
                Draw.ground(this.impacts.get(i), this.radius, this.rgb, 0.9F);
                Draw.end(Draw.GROUND_WARNING, Mth.clamp(t / this.times[i], 0.0F, 1.0F), i * 0.37F, 0.0F);
            }
        }
        int hot = Draw.mix(this.rgb, 0xFFFFFF, 0.5F);
        float[] widths = new float[TRAIL];
        float[] alphas = new float[TRAIL];
        for (int j = 0; j < TRAIL; j++) {
            float k = 1.0F - j / (float) (TRAIL - 1);
            widths[j] = 0.3F + 1.8F * k;
            alphas[j] = 0.9F * k;
        }
        boolean any = false;
        for (int i = 0; i < n; i++) {
            float local = t - (this.times[i] - this.fall);
            if (local >= 0.0F && local < this.fall) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }
        if (Draw.begin(Draw.Material.SMOKE)) {
            for (int i = 0; i < n; i++) {
                float local = t - (this.times[i] - this.fall);
                if (local < 0.0F || local >= this.fall) {
                    continue;
                }
                Vec3[] points = this.trail(i, local / this.fall);
                float[] smokeWidths = new float[TRAIL];
                float[] smokeAlphas = new float[TRAIL];
                for (int j = 0; j < TRAIL; j++) {
                    smokeWidths[j] = widths[j] * 1.8F;
                    smokeAlphas[j] = alphas[j] * 0.5F;
                }
                Draw.ribbon(points, smokeWidths, smokeAlphas, Draw.ASH, 0.0F, 1.0F);
            }
            Draw.end(Draw.FLAME_SMOKE, 1.0F, 1.0F, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (int i = 0; i < n; i++) {
                float local = t - (this.times[i] - this.fall);
                if (local < 0.0F || local >= this.fall) {
                    continue;
                }
                Draw.ribbon(this.trail(i, local / this.fall), widths, alphas, this.rgb, 0.0F, 2.0F);
            }
            Draw.end(Draw.FLAME_STRIP, 2.0F, 1.0F, 0.8F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (int i = 0; i < n; i++) {
                float local = t - (this.times[i] - this.fall);
                if (local < 0.0F || local >= this.fall) {
                    continue;
                }
                Vec3 p = this.skullPos(i, local / this.fall);
                Draw.billboard(p, 4.2F, this.rgb, 0.6F);
                Draw.billboard(p, 2.0F, hot, 0.8F);
            }
            Draw.end(Draw.FLAME_BALL, 1.0F, 2.0F, 0.8F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (int i = 0; i < n; i++) {
                float local = t - (this.times[i] - this.fall);
                if (local < 0.0F || local >= this.fall) {
                    continue;
                }
                Draw.billboard(this.skullPos(i, local / this.fall), 2.8F, this.rgb, 1.0F);
            }
            Draw.end(Draw.FLAME_SKULL, 1.0F, 1.0F, 0.0F);
        }
    }

    private Vec3[] trail(int i, float k) {
        Vec3[] points = new Vec3[TRAIL];
        for (int j = 0; j < TRAIL; j++) {
            points[j] = this.skullPos(i, Math.max(0.0F, k - j * 0.06F)).add(0.0, j * 0.15, 0.0);
        }
        return points;
    }
}
