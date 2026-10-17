package com.witherplus.client.vfx;

import com.witherplus.client.CameraShake;
import com.witherplus.client.Draw;
import com.witherplus.client.VfxInstance;
import com.witherplus.wither.WpMath;
import com.witherplus.wither.skill.MaelstromSkill;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class MaelstromVfx extends VfxInstance {
    private static final double STEP = 0.7;
    private final Vec3 center;
    private final float base;
    private final int duration;
    private final float maxRadius;
    private final int arms;
    private final int rgb;

    public MaelstromVfx(int witherId, Vec3 center, float base, int duration, float maxRadius, int arms, int rgb) {
        super(duration + 10);
        this.center = center;
        this.base = base;
        this.duration = Math.max(1, duration);
        this.maxRadius = maxRadius;
        this.arms = Math.max(1, arms);
        this.rgb = rgb;
    }

    @Override
    protected void onTick() {
        if (this.age % 8 == 0 && this.age < this.duration) {
            CameraShake.addAt(this.center, 0.2F, this.maxRadius * 2.0F, 8);
        }
    }

    @Override
    public void render(float partialTick) {
        float raw = this.time(partialTick);
        float t = Math.min(raw, this.duration);
        float alpha = this.fade(partialTick, 6.0F, 10.0F);
        float radius = WpMath.maelstromRadius(t, this.duration, this.maxRadius);
        int hot = Draw.mix(this.rgb, 0xFFFFFF, 0.5F);
        int n = Math.max(2, (int) ((radius - 1.0) / STEP) + 1);
        Vec3[][] arms = new Vec3[this.arms][];
        for (int arm = 0; arm < this.arms; arm++) {
            Vec3[] points = new Vec3[n];
            for (int i = 0; i < n; i++) {
                double d = 1.0 + (radius - 1.0) * i / (n - 1);
                float angle = WpMath.maelstromArm(this.base, arm, this.arms, t, (float) d);
                double x = this.center.x + Mth.cos(angle) * d;
                double z = this.center.z + Mth.sin(angle) * d;
                double y = Mth.clamp(Draw.groundY(x, z, this.center.y), this.center.y - 3.0, this.center.y + 3.0) + 0.1;
                points[i] = new Vec3(x, y, z);
            }
            arms[arm] = points;
        }
        float[] widths = new float[n];
        float[] alphas = new float[n];
        float[] heights = new float[n];
        for (int i = 0; i < n; i++) {
            float k = i / (float) (n - 1);
            float taper = Mth.clamp(Math.min(k * 6.0F, (1.0F - k) * 6.0F), 0.0F, 1.0F);
            widths[i] = (float) (MaelstromSkill.ARM_WIDTH * 2.0) * (0.6F + 0.4F * taper);
            alphas[i] = alpha * (0.3F + 0.7F * taper);
            heights[i] = (2.2F - 1.2F * k) * (0.85F + 0.15F * Mth.sin(raw * 0.5F + i * 0.7F));
        }
        if (Draw.begin(Draw.Material.GROUND)) {
            Draw.ground(this.center, radius + 1.0F, this.rgb, alpha * 0.6F);
            Draw.end(Draw.GROUND_SIGIL, 1.0F, 1.3F, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (Vec3[] points : arms) {
                Draw.flatRibbon(points, widths, alphas, this.rgb, 0.0F, n * 0.15F);
            }
            Draw.end(Draw.FLAME_STRIP, 2.0F, 1.0F, 0.6F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (Vec3[] points : arms) {
                Draw.wall(points, heights, alphas, hot, 0.0F, n * 0.2F);
            }
            Draw.end(Draw.FLAME_COLUMN, 2.4F, 1.0F, 0.4F);
        }
        Vec3 top = this.center.add(0.0, 9.0, 0.0);
        if (Draw.begin(Draw.Material.SMOKE)) {
            Draw.cylinder(this.center, top, 3.4F, 5.5F, 16, Draw.ASH, alpha * 0.45F, 3.0F);
            Draw.end(Draw.FLAME_SMOKE, 1.0F, 1.0F, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            Draw.cylinder(this.center, top, 2.2F, 3.6F, 16, this.rgb, alpha * 0.8F, 3.0F);
            Draw.end(Draw.FLAME_COLUMN, 2.0F, 2.0F, 0.5F);
        }
    }
}
