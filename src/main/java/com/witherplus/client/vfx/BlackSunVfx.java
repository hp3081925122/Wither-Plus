package com.witherplus.client.vfx;

import com.witherplus.client.CameraShake;
import com.witherplus.client.Draw;
import com.witherplus.client.ScreenFx;
import com.witherplus.client.VfxInstance;
import com.witherplus.client.VfxManager;
import com.witherplus.wither.WpMath;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class BlackSunVfx extends VfxInstance {
    private static final int RAYS = 14;
    private static final int WISPS = 28;
    private final Vec3 pos;
    private final Vec3 ground;
    private final int form;
    private final int active;
    private final int collapse;
    private final int rgb;

    public BlackSunVfx(Vec3 pos, int form, int active, int collapse, int rgb) {
        super(form + active + collapse + 4);
        this.pos = pos;
        this.ground = new Vec3(pos.x, Draw.groundY(pos.x, pos.z, pos.y - 7.5), pos.z);
        this.form = Math.max(1, form);
        this.active = active;
        this.collapse = Math.max(1, collapse);
        this.rgb = rgb;
    }

    private float size(float t) {
        if (t < this.form) {
            return WpMath.easeOut(t / this.form) * 2.6F;
        }
        if (t < this.form + this.active) {
            return 2.6F + 0.15F * Mth.sin(t * 0.3F);
        }
        float k = Mth.clamp((t - this.form - this.active) / this.collapse, 0.0F, 1.0F);
        return 2.6F * (1.0F - WpMath.easeIn(k)) + 0.2F;
    }

    @Override
    protected void onTick() {
        int end = this.form + this.active + this.collapse;
        if (this.age < end && distanceToPlayer(this.pos) < 34.0) {
            float s = Mth.clamp(this.age / (float) this.form, 0.0F, 1.0F);
            ScreenFx.sustain(0.45F * s, 0.5F * s, 0x000000);
        }
        if (this.age > this.form && this.age < this.form + this.active && this.age % 10 == 0) {
            CameraShake.addAt(this.pos, 0.15F, 30.0F, 10);
        }
        if (this.age == end) {
            VfxManager.add(new BurstVfx(this.ground.add(0.0, 1.0, 0.0), 9.0F, this.rgb, true));
            CameraShake.addAt(this.pos, 2.0F, 50.0F, 25);
            if (distanceToPlayer(this.pos) < 30.0) {
                ScreenFx.flash(0.6F, Draw.mix(this.rgb, 0xFFFFFF, 0.5F));
            }
        }
    }

    @Override
    public void render(float partialTick) {
        float t = this.time(partialTick);
        float size = this.size(t);
        float strength = Mth.clamp(t / this.form, 0.0F, 1.0F);
        float collapseK = Mth.clamp((t - this.form - this.active) / this.collapse, 0.0F, 1.0F);
        int corona = Draw.mix(this.rgb, Draw.HELL, 0.35F);
        if (Draw.begin(Draw.Material.GROUND)) {
            Draw.ground(this.ground, 9.0F, this.rgb, strength * 0.8F);
            Draw.end(Draw.GROUND_SIGIL, strength, 2.1F, 0.0F);
        }
        if (collapseK > 0.0F && Draw.begin(Draw.Material.GROUND)) {
            Draw.ground(this.ground, 9.0F, Draw.HELL, 1.0F);
            Draw.end(Draw.GROUND_WARNING, collapseK, 0.7F, 0.0F);
        }
        if (Draw.begin(Draw.Material.SMOKE)) {
            for (int i = 0; i < WISPS; i++) {
                float phase = (hash(i) + t * 0.015F) % 1.0F;
                float r = 18.0F * (1.0F - phase);
                float angle = hash(i + 99) * Mth.TWO_PI + phase * 4.0F;
                double y = this.pos.y - 6.0 * (1.0 - phase) * hash(i + 7);
                Vec3 p = new Vec3(this.pos.x + Mth.cos(angle) * r, y, this.pos.z + Mth.sin(angle) * r);
                Draw.billboard(p, 2.0F * (1.0F - phase) + 0.6F, angle, Draw.ASH, phase * 0.6F * strength * (1.0F - collapseK));
            }
            Draw.end(Draw.FLAME_SMOKE, 0.5F, 1.3F, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            Draw.ring(this.pos, new Vec3(0.2, 1.0, 0.1), size * 1.3F, size * 2.9F, 48, corona, 0.0F, 0.9F * strength, 6.0F);
            Draw.end(Draw.FLAME_STRIP, 1.6F, 1.0F, 0.5F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            Vec3 right = Draw.cameraRight();
            Vec3 up = Draw.cameraUp();
            for (int i = 0; i < RAYS; i++) {
                float angle = i * Mth.TWO_PI / RAYS + t * 0.01F;
                Vec3 dir = right.scale(Mth.cos(angle)).add(up.scale(Mth.sin(angle)));
                float reach = size * (1.8F + 0.8F * Mth.sin(t * 0.2F + i * 1.7F));
                Vec3 a = this.pos.add(dir.scale(size * 0.95));
                Vec3 b = this.pos.add(dir.scale(reach));
                Draw.ribbon(new Vec3[]{a, a.lerp(b, 0.5), b}, new float[]{0.8F, 0.5F, 0.05F}, new float[]{strength, strength * 0.7F, 0.0F}, corona, 0.0F, 1.5F);
            }
            Draw.end(Draw.FLAME_STRIP, 2.0F, 1.0F, 0.6F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            Draw.billboard(this.pos, size * 4.2F, corona, 0.75F * strength);
            for (int i = 0; i < WISPS; i += 2) {
                float phase = (hash(i) + t * 0.015F) % 1.0F;
                float r = 18.0F * (1.0F - phase);
                float angle = hash(i + 99) * Mth.TWO_PI + phase * 4.0F + 0.2F;
                Vec3 p = new Vec3(this.pos.x + Mth.cos(angle) * r, this.pos.y - 6.0 * (1.0 - phase) * hash(i + 7), this.pos.z + Mth.sin(angle) * r);
                Draw.billboard(p, 0.5F, this.rgb, phase * strength * (1.0F - collapseK));
            }
            Draw.end(Draw.FLAME_BALL, 0.8F, 2.0F, 0.5F);
        }
        if (Draw.begin(Draw.Material.SPHERE_ALPHA)) {
            Draw.sphere(this.pos, size, 14, 20, corona, 1.0F);
            Draw.end(Draw.SPHERE_BLACK_SUN, 3.0F, 2.0F, 0.03F);
        }
    }
}
