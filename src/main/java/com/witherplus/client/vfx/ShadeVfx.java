package com.witherplus.client.vfx;

import com.witherplus.client.CameraShake;
import com.witherplus.client.Draw;
import com.witherplus.client.ScreenFx;
import com.witherplus.client.VfxInstance;
import com.witherplus.wither.WpMath;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class ShadeVfx extends VfxInstance {
    private static final int PUFFS = 18;
    private final Vec3 pos;
    private final boolean vanish;
    private final int rgb;

    public ShadeVfx(Vec3 pos, boolean vanish, int rgb) {
        super(30);
        this.pos = pos;
        this.vanish = vanish;
        this.rgb = rgb;
    }

    @Override
    protected void onTick() {
        if (this.age == 1) {
            CameraShake.addAt(this.pos, 0.5F, 24.0F, 10);
            if (distanceToPlayer(this.pos) < 20.0) {
                ScreenFx.desaturate(0.45F);
            }
        }
    }

    @Override
    public void render(float partialTick) {
        float t = this.time(partialTick);
        float k = WpMath.easeOut(t / 14.0F);
        float out = this.fade(partialTick, 0.0F, 14.0F);
        if (Draw.begin(Draw.Material.SMOKE)) {
            for (int i = 0; i < PUFFS; i++) {
                float angle = hash(i) * Mth.TWO_PI;
                float pitch = (hash(i + 40) - 0.5F) * 2.0F;
                Vec3 dir = new Vec3(Mth.cos(angle) * Math.sqrt(1.0 - pitch * pitch), pitch, Mth.sin(angle) * Math.sqrt(1.0 - pitch * pitch));
                Vec3 p = this.pos.add(dir.scale(4.0 * k * (0.6 + 0.6 * hash(i + 9)))).add(0.0, t * 0.04, 0.0);
                Draw.billboard(p, 2.2F + 1.8F * k, hash(i + 5) * Mth.TWO_PI, Draw.ASH, 0.85F * out);
            }
            Draw.end(Draw.FLAME_SMOKE, 0.6F, 1.4F, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            Draw.billboard(this.pos, 6.0F * k, this.rgb, 0.7F * Mth.clamp(1.0F - t / 10.0F, 0.0F, 1.0F));
            Draw.end(Draw.FLAME_BALL, 1.2F, 2.0F, 1.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (int i = 0; i < 3; i++) {
                float angle = i * 2.0944F + t * 0.05F;
                float dist;
                if (this.vanish) {
                    dist = 0.6F + 3.0F * k;
                } else {
                    dist = 3.6F * (1.0F - k);
                }
                Vec3 p = this.pos.add(Mth.cos(angle) * dist, 0.6 + Mth.sin(t * 0.3F + i) * 0.3, Mth.sin(angle) * dist);
                Draw.billboard(p, 1.6F, this.rgb, out);
            }
            Draw.end(Draw.FLAME_SKULL, 1.0F, 1.0F, 0.0F);
        }
    }
}
