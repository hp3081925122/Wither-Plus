package com.witherplus.client.vfx;

import com.witherplus.client.CameraShake;
import com.witherplus.client.Draw;
import com.witherplus.client.ScreenFx;
import com.witherplus.client.VfxInstance;
import com.witherplus.wither.WpMath;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class PillarVfx extends VfxInstance {
    private static final float HEIGHT = 14.0F;
    private static final int BLAZE = 24;
    private final Vec3 pos;
    private final int delay;
    private final int style;
    private final float radius;
    private final int rgb;

    public PillarVfx(Vec3 pos, int delay, int style, float radius, int rgb) {
        super(delay + BLAZE + 4);
        this.pos = pos;
        this.delay = delay;
        this.style = style;
        this.radius = radius;
        this.rgb = rgb;
    }

    @Override
    protected void onTick() {
        if (this.age == this.delay && this.style == 0) {
            CameraShake.addAt(this.pos, 0.8F, 24.0F, 12);
            if (distanceToPlayer(this.pos) < this.radius * 2.0F) {
                ScreenFx.flash(0.3F, this.rgb);
            }
        }
    }

    @Override
    public void render(float partialTick) {
        float t = this.time(partialTick);
        int hot = Draw.mix(this.rgb, 0xFFFFFF, 0.6F);
        if (this.style == 1) {
            this.renderSummon(t, partialTick);
            return;
        }
        if (t < this.delay) {
            float k = t / Math.max(1, this.delay);
            if (Draw.begin(Draw.Material.GROUND)) {
                Draw.ground(this.pos, this.radius, this.rgb, 1.0F);
                Draw.end(Draw.GROUND_WARNING, k, 0.4F, 0.0F);
            }
            if (Draw.begin(Draw.Material.FLAME)) {
                for (int i = 0; i < 6; i++) {
                    float phase = (hash(i) + t * 0.06F) % 1.0F;
                    float angle = hash(i + 11) * Mth.TWO_PI;
                    Vec3 p = this.pos.add(Mth.cos(angle) * this.radius * 0.8, phase * 4.0, Mth.sin(angle) * this.radius * 0.8);
                    Draw.billboard(p, 0.35F, this.rgb, (1.0F - phase) * k);
                }
                Draw.end(Draw.FLAME_BALL, 1.0F, 2.0F, 0.5F);
            }
            return;
        }
        float local = t - this.delay;
        float grow = WpMath.easeOut(local / 3.0F);
        float out = Mth.clamp(1.0F - (local - 8.0F) / (BLAZE - 8.0F), 0.0F, 1.0F);
        Vec3 top = this.pos.add(0.0, HEIGHT * grow, 0.0);
        if (Draw.begin(Draw.Material.GROUND)) {
            Draw.terrain(this.pos, this.radius * 1.4F, 4, 0.06F, this.rgb, out);
            Draw.end(Draw.GROUND_SOULFIRE, 0.0F, 0.8F, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            Draw.cylinder(this.pos, top, this.radius * grow, this.radius * 0.5F, 16, this.rgb, out, 3.0F);
            Draw.cylinder(this.pos, top, this.radius * 0.45F * grow, this.radius * 0.2F, 12, hot, out, 2.0F);
            Draw.end(Draw.FLAME_COLUMN, 3.0F, 1.5F, 1.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            Draw.billboard(top, this.radius * 3.0F, this.rgb, out * 0.7F);
            Draw.billboard(this.pos.add(0.0, 0.6, 0.0), this.radius * 4.0F * grow, hot, Mth.clamp(1.0F - local / 8.0F, 0.0F, 1.0F));
            Draw.end(Draw.FLAME_BALL, 1.0F, 2.0F, 1.0F);
        }
    }

    private void renderSummon(float t, float partialTick) {
        float alpha = this.fade(partialTick, 3.0F, 10.0F);
        float grow = WpMath.easeOut(t / 6.0F);
        if (Draw.begin(Draw.Material.GROUND)) {
            Draw.ground(this.pos, 2.4F, this.rgb, alpha);
            Draw.end(Draw.GROUND_SIGIL, 1.0F, 0.2F, 0.0F);
        }
        if (Draw.begin(Draw.Material.SMOKE)) {
            Draw.cylinder(this.pos, this.pos.add(0.0, 5.0 * grow, 0.0), 1.4F, 2.2F, 12, Draw.ASH, alpha * 0.6F, 2.0F);
            Draw.end(Draw.FLAME_SMOKE, 1.5F, 1.0F, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            Draw.cylinder(this.pos, this.pos.add(0.0, 4.0 * grow, 0.0), this.radius * 0.7F, this.radius * 0.3F, 12, this.rgb, alpha, 2.0F);
            Draw.end(Draw.FLAME_COLUMN, 2.4F, 1.5F, 0.6F);
        }
    }
}
