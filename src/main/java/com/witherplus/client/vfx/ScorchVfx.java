package com.witherplus.client.vfx;

import com.witherplus.client.Draw;
import com.witherplus.client.VfxInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class ScorchVfx extends VfxInstance {
    private static int counter;
    private final Vec3 pos;
    private final float radius;
    private final int rgb;
    private final float seed;
    private final int salt;

    public ScorchVfx(Vec3 pos, float radius, int lifetime, int rgb) {
        super(lifetime);
        this.pos = pos;
        this.radius = radius;
        this.rgb = rgb;
        counter++;
        this.salt = counter;
        this.seed = hash(counter) * 7.0F;
    }

    @Override
    public void render(float partialTick) {
        float a = this.fade(partialTick, 4.0F, 25.0F);
        if (a <= 0.0F) {
            return;
        }
        float t = this.time(partialTick);
        if (Draw.begin(Draw.Material.GROUND_ALPHA)) {
            Draw.terrain(this.pos, this.radius * 1.15F, 4, 0.03F, Draw.ASH, a * 0.85F);
            Draw.end(Draw.GROUND_ASH, 0.0F, this.seed, 0.0F);
        }
        float fire = a * Mth.clamp(1.0F - t / (this.lifetime * 0.8F), 0.0F, 1.0F);
        if (fire <= 0.01F) {
            return;
        }
        if (Draw.begin(Draw.Material.GROUND)) {
            Draw.terrain(this.pos, this.radius, 4, 0.06F, this.rgb, fire);
            Draw.end(Draw.GROUND_SOULFIRE, 0.0F, this.seed, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (int i = 0; i < 5; i++) {
                float angle = hash(this.salt * 7 + i) * Mth.TWO_PI;
                float dist = this.radius * 0.7F * Mth.sqrt(hash(this.salt * 13 + i));
                Vec3 base = this.pos.add(Mth.cos(angle) * dist, -0.05, Mth.sin(angle) * dist);
                float size = 0.55F + 0.6F * hash(this.salt * 5 + i);
                float h = this.radius * 1.1F * size * (0.85F + 0.15F * Mth.sin(t * 0.4F + i));
                Draw.uprightBillboard(base, this.radius * 0.75F * size, h, this.rgb, fire, this.salt * 5 + i);
            }
            Draw.end(Draw.FLAME_TONGUE, 1.8F, 1.0F, 0.6F);
        }
    }
}
