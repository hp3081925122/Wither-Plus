package com.witherplus.client.vfx;

import com.witherplus.client.CameraShake;
import com.witherplus.client.Draw;
import com.witherplus.client.ScreenFx;
import com.witherplus.client.VfxInstance;
import com.witherplus.client.VfxManager;
import com.witherplus.wither.WpMath;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class BurstVfx extends VfxInstance {
    private static final int PUFFS = 12;
    private final Vec3 pos;
    private final float radius;
    private final int rgb;
    private final boolean big;

    public BurstVfx(Vec3 pos, float radius, int rgb, boolean big) {
        super(26);
        this.pos = pos;
        this.radius = Math.max(0.5F, radius);
        this.rgb = rgb;
        this.big = big;
    }

    @Override
    protected void onTick() {
        if (this.age != 1) {
            return;
        }
        CameraShake.addAt(this.pos, this.radius * 0.22F, this.radius * 4.0F, 14);
        if (this.big) {
            Vec3 ground = new Vec3(this.pos.x, Draw.groundY(this.pos.x, this.pos.z, this.pos.y - 1.0), this.pos.z);
            if (Math.abs(ground.y - this.pos.y) < this.radius + 2.0) {
                VfxManager.add(new ScorchVfx(ground, this.radius * 0.8F, 90, this.rgb));
            }
            if (distanceToPlayer(this.pos) < this.radius * 2.0) {
                ScreenFx.flash(0.3F, this.rgb);
            }
        }
    }

    @Override
    public void render(float partialTick) {
        float t = this.time(partialTick);
        float k = WpMath.easeOut(t / 8.0F);
        float out = Mth.clamp(1.0F - t / this.lifetime, 0.0F, 1.0F);
        int hot = Draw.mix(this.rgb, 0xFFFFFF, 0.5F);
        if (Draw.begin(Draw.Material.SPHERE)) {
            Draw.sphere(this.pos, this.radius * (0.2F + 0.8F * k), 10, 18, this.rgb, Mth.clamp(1.0F - t / 14.0F, 0.0F, 1.0F) * 0.9F);
            Draw.end(Draw.SPHERE_SHELL, 2.4F, 2.0F, 0.02F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            Draw.billboard(this.pos, this.radius * 2.8F * (0.5F + 0.5F * k), hot, Mth.clamp(1.0F - t / 10.0F, 0.0F, 1.0F));
            Draw.billboard(this.pos, this.radius * 4.2F * k, this.rgb, Mth.clamp(1.0F - t / 16.0F, 0.0F, 1.0F) * 0.5F);
            Draw.end(Draw.FLAME_BALL, 0.6F, 2.0F, 1.5F);
        }
        if (Draw.begin(Draw.Material.SMOKE)) {
            for (int i = 0; i < PUFFS; i++) {
                float angle = i * Mth.TWO_PI / PUFFS + hash(i) * 0.5F;
                Vec3 dir = WpMath.dir(angle);
                Vec3 p = this.pos.add(dir.scale(this.radius * k * (0.7F + 0.4F * hash(i + 31)))).add(0.0, t * 0.05 + hash(i + 7) * 0.6, 0.0);
                Draw.billboard(p, this.radius * (0.5F + k) * (0.8F + 0.4F * hash(i + 13)), hash(i + 3) * Mth.TWO_PI + t * 0.03F, Draw.ASH, 0.7F * out);
            }
            Draw.end(Draw.FLAME_SMOKE, 0.4F, 1.5F, 0.0F);
        }
    }
}
