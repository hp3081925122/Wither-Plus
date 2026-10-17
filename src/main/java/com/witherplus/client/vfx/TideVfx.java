package com.witherplus.client.vfx;

import com.witherplus.client.Draw;
import com.witherplus.client.VfxInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class TideVfx extends VfxInstance {
    private final Vec3 center;
    private final float speed;
    private final float maxRadius;
    private final float gap;
    private final float gapHalf;
    private final float height;
    private final boolean jumpable;
    private final int rgb;

    public TideVfx(Vec3 center, float speed, float maxRadius, float gap, float gapHalf, float height, boolean jumpable, int rgb) {
        super((int) (maxRadius / Math.max(0.05F, speed)) + 8);
        this.center = center;
        this.speed = Math.max(0.05F, speed);
        this.maxRadius = maxRadius;
        this.gap = gap;
        this.gapHalf = gapHalf;
        this.height = height;
        this.jumpable = jumpable;
        this.rgb = rgb;
    }

    @Override
    public void render(float partialTick) {
        float t = this.time(partialTick);
        float r = t * this.speed;
        if (r > this.maxRadius + 1.0F || r < 0.3F) {
            return;
        }
        float life = Mth.clamp(1.0F - (r / this.maxRadius) * (r / this.maxRadius) * 0.6F, 0.0F, 1.0F) * this.fade(partialTick, 3.0F, 8.0F);
        int segments = Mth.clamp((int) (r * 3.0F), 16, 120);
        float span = Mth.TWO_PI - this.gapHalf * 2.0F;
        Vec3[] bases = new Vec3[segments + 1];
        Vec3[] foam = new Vec3[segments + 1];
        float[] heights = new float[segments + 1];
        float[] coreHeights = new float[segments + 1];
        float[] alphas = new float[segments + 1];
        float[] widths = new float[segments + 1];
        float visual = this.height;
        if (this.jumpable) {
            visual = 1.2F;
        }
        for (int i = 0; i <= segments; i++) {
            float angle = this.gap + this.gapHalf + span * i / segments;
            double x = this.center.x + Mth.cos(angle) * r;
            double z = this.center.z + Mth.sin(angle) * r;
            double y = Mth.clamp(Draw.groundY(x, z, this.center.y), this.center.y - 1.5, this.center.y + 3.0);
            bases[i] = new Vec3(x, y, z);
            double fr = Math.max(0.2, r - 1.0);
            foam[i] = new Vec3(this.center.x + Mth.cos(angle) * fr, y + 0.08, this.center.z + Mth.sin(angle) * fr);
            float edge = 1.0F;
            if (this.gapHalf > 0.0F) {
                float k = i / (float) segments;
                edge = Mth.clamp(Math.min(k, 1.0F - k) * segments / 3.0F, 0.0F, 1.0F);
            }
            heights[i] = visual * (0.85F + 0.15F * Mth.sin(angle * 7.0F + t * 0.5F)) * edge;
            coreHeights[i] = heights[i] * 0.55F;
            alphas[i] = life * edge;
            widths[i] = 2.2F;
        }
        int hot = Draw.mix(this.rgb, 0xFFFFFF, 0.55F);
        if (Draw.begin(Draw.Material.FLAME)) {
            Draw.flatRibbon(foam, widths, alphas, this.rgb, 0.0F, segments * 0.2F);
            Draw.end(Draw.FLAME_STRIP, 2.0F, 1.0F, 0.4F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            Draw.wall(bases, heights, alphas, this.rgb, 0.0F, segments * 0.25F);
            Draw.wall(bases, coreHeights, alphas, hot, 0.0F, segments * 0.25F);
            Draw.end(Draw.FLAME_COLUMN, 2.6F, 1.0F, 0.6F);
        }
        if (Draw.begin(Draw.Material.SMOKE)) {
            for (int i = 0; i <= segments; i += 6) {
                Draw.billboard(bases[i].add(0.0, heights[i] + 0.4, 0.0), 2.0F, hash(i) * Mth.TWO_PI, Draw.ASH, alphas[i] * 0.45F);
            }
            Draw.end(Draw.FLAME_SMOKE, 1.0F, 1.2F, 0.0F);
        }
        if (this.gapHalf > 0.0F && Draw.begin(Draw.Material.FLAME)) {
            for (int side = -1; side <= 1; side += 2) {
                float angle = this.gap + this.gapHalf * side;
                Vec3 p = new Vec3(this.center.x + Mth.cos(angle) * r, bases[0].y + 1.0, this.center.z + Mth.sin(angle) * r);
                Draw.billboard(p, 1.8F, hot, life);
            }
            Draw.end(Draw.FLAME_BALL, 1.0F, 2.0F, 1.0F);
        }
    }
}
