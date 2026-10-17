package com.witherplus.client.vfx;

import com.witherplus.client.Draw;
import com.witherplus.client.ScreenFx;
import com.witherplus.client.VfxInstance;
import com.witherplus.wither.WpMath;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.phys.Vec3;

public final class CrownVfx extends VfxInstance {
    private static final int SPIKES = 9;
    private static final int WARN = 30;
    private final int witherId;
    private final Vec3 center;
    private final int duration;
    private final float radius;
    private final int rgb;

    public CrownVfx(int witherId, Vec3 center, int duration, float radius, int rgb) {
        super(duration + 4);
        this.witherId = witherId;
        this.center = center;
        this.duration = duration;
        this.radius = radius;
        this.rgb = rgb;
    }

    @Override
    protected void onTick() {
        if (this.age < this.duration && distanceToPlayer(this.center) < 40.0) {
            ScreenFx.sustain(0.25F, 0.45F, 0x001830);
        }
    }

    @Override
    public void render(float partialTick) {
        WitherBoss wither = wither(this.witherId);
        float t = this.time(partialTick);
        float alpha = this.fade(partialTick, 8.0F, 6.0F);
        float grow = WpMath.easeOut(t / 15.0F);
        if (Draw.begin(Draw.Material.GROUND)) {
            Draw.ground(this.center, this.radius, this.rgb, alpha);
            Draw.end(Draw.GROUND_SIGIL, Mth.clamp(t / 40.0F, 0.0F, 1.0F), 4.2F, 0.0F);
        }
        float warnStart = this.duration - 10 - WARN;
        if (t > warnStart && t < this.duration - 10 && Draw.begin(Draw.Material.GROUND)) {
            Draw.ground(this.center, this.radius, Draw.HELL, 1.0F);
            Draw.end(Draw.GROUND_WARNING, (t - warnStart) / WARN, 1.7F, 0.0F);
        }
        if (wither == null) {
            return;
        }
        Vec3 crown = head(wither, 0, partialTick).add(0.0, 0.9, 0.0);
        float light = Draw.lightAt(crown);
        if (alpha > 0.5F && grow > 0.99F && Draw.begin(Draw.Material.BLOCK, Draw.BONE_SPIKE_TIP)) {
            for (int i = 0; i < SPIKES; i++) {
                float angle = i * Mth.TWO_PI / SPIKES + t * 0.02F;
                Vec3 dir = WpMath.dir(angle);
                Vec3 base = crown.add(dir.scale(1.0)).add(0.0, (hash(i) - 0.5) * 0.25, 0.0);
                Draw.crossSprite(base, 1.0F, 1.0F, dir.x, dir.z, light);
            }
            Draw.end(0.0F, 0.0F, 0.0F, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            Draw.ring(crown, new Vec3(0.0, 1.0, 0.0), 0.6F, 1.4F * grow, 32, this.rgb, 0.0F, alpha, 4.0F);
            Draw.end(Draw.FLAME_STRIP, 2.0F, 1.0F, 0.6F);
        }
        if (Draw.begin(Draw.Material.SPHERE)) {
            Draw.sphere(center(wither, partialTick), 3.4F * grow, 12, 20, this.rgb, alpha * 0.55F);
            Draw.end(Draw.SPHERE_SHELL, 2.6F, 2.0F, 0.02F);
        }
    }
}
