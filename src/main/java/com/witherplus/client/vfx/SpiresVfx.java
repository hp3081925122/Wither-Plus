package com.witherplus.client.vfx;

import com.witherplus.client.CameraShake;
import com.witherplus.client.Draw;
import com.witherplus.client.VfxInstance;
import com.witherplus.wither.WpMath;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class SpiresVfx extends VfxInstance {
    private static final int WARN = 14;
    private static final int STAND = 26;
    private static final int SINK = 8;
    private final List<Vec3> points;
    private final int[] delays;
    private final Vec3 origin;
    private final int rgb;
    private final float[] lights;

    public SpiresVfx(List<Vec3> points, int[] delays, Vec3 origin, int rgb) {
        super(maxOf(delays) + STAND + SINK + 2);
        this.points = points;
        this.delays = delays;
        this.origin = origin;
        this.rgb = rgb;
        this.lights = new float[points.size()];
        for (int i = 0; i < points.size(); i++) {
            this.lights[i] = Draw.lightAt(points.get(i).add(0.0, 0.5, 0.0));
        }
    }

    private static int maxOf(int[] values) {
        int max = 0;
        for (int v : values) {
            max = Math.max(max, v);
        }
        return max;
    }

    private int count() {
        return Math.min(this.points.size(), this.delays.length);
    }

    @Override
    protected void onTick() {
        for (int i = 0; i < this.count(); i += 4) {
            if (this.age == this.delays[i]) {
                CameraShake.addAt(this.points.get(i), 0.3F, 14.0F, 6);
            }
        }
    }

    @Override
    public void render(float partialTick) {
        float t = this.time(partialTick);
        int n = this.count();
        if (Draw.begin(Draw.Material.GROUND)) {
            for (int i = 0; i < n; i++) {
                float local = t - (this.delays[i] - WARN);
                if (local >= 0.0F && local < WARN) {
                    Draw.ground(this.points.get(i), 1.4F, this.rgb, local / WARN);
                }
            }
            Draw.end(Draw.GROUND_WARNING, 0.7F, 0.3F, 0.0F);
        }
        ResourceLocation[] textures = {Draw.BONE_SPIKE_BASE, Draw.BONE_SPIKE_MIDDLE, Draw.BONE_SPIKE_TIP};
        for (int pass = 0; pass < 3; pass++) {
            if (!Draw.begin(Draw.Material.BLOCK, textures[pass])) {
                break;
            }
            for (int i = 0; i < n; i++) {
                float local = t - this.delays[i];
                if (local < 0.0F || local > STAND + SINK) {
                    continue;
                }
                int middles = Math.min(2, (int) (hash(i) * 3.0F));
                int segments = middles + 2;
                float rise = WpMath.easeOut(local / 3.0F);
                float sink = Mth.clamp((local - STAND) / SINK, 0.0F, 1.0F);
                float drop = segments * (1.0F - rise) + segments * WpMath.easeIn(sink);
                if (drop >= segments) {
                    continue;
                }
                Vec3 p = this.points.get(i);
                Vec3 out = new Vec3(p.x - this.origin.x, 0.0, p.z - this.origin.z);
                if (out.lengthSqr() > 1.0E-4) {
                    out = out.normalize();
                } else {
                    out = new Vec3(1.0, 0.0, 0.0);
                }
                float light = this.lights[i];
                Vec3 base = p.add(0.0, -0.15 - drop, 0.0);
                if (pass == 0) {
                    Draw.crossSprite(base, 1.0F, 1.0F, out.x, out.z, light);
                } else if (pass == 1) {
                    for (int k = 0; k < middles; k++) {
                        Draw.crossSprite(base.add(0.0, 1 + k, 0.0), 1.0F, 1.0F, out.x, out.z, light);
                    }
                } else {
                    Draw.crossSprite(base.add(0.0, middles + 1, 0.0), 1.0F, 1.0F, out.x, out.z, light);
                    for (int k = 0; k < 2; k++) {
                        float angle = hash(i * 3 + k) * Mth.TWO_PI;
                        double sx = Mth.cos(angle);
                        double sz = Mth.sin(angle);
                        Draw.crossSprite(base.add(sx * 0.6, -0.25, sz * 0.6), 1.0F, 1.0F, sx, sz, light);
                    }
                }
            }
            Draw.end(0.0F, 0.0F, 0.0F, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (int i = 0; i < n; i++) {
                float local = t - this.delays[i];
                if (local >= 0.0F && local < 7.0F) {
                    Draw.billboard(this.points.get(i).add(0.0, 0.6, 0.0), 2.6F, this.rgb, 1.0F - local / 7.0F);
                }
            }
            Draw.end(Draw.FLAME_BALL, 1.2F, 2.0F, 0.8F);
        }
        if (Draw.begin(Draw.Material.SMOKE)) {
            for (int i = 0; i < n; i += 2) {
                float local = t - this.delays[i];
                if (local >= 0.0F && local < 16.0F) {
                    Draw.billboard(this.points.get(i).add(0.0, 0.4 + local * 0.05, 0.0), 1.6F + local * 0.1F, hash(i) * Mth.TWO_PI, Draw.ASH, 0.6F * (1.0F - local / 16.0F));
                }
            }
            Draw.end(Draw.FLAME_SMOKE, 0.5F, 1.4F, 0.0F);
        }
    }
}
