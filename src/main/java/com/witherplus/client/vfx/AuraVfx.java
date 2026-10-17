package com.witherplus.client.vfx;

import com.witherplus.client.Draw;
import com.witherplus.client.VfxInstance;
import com.witherplus.client.WitherStageClient;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.phys.Vec3;

public final class AuraVfx extends VfxInstance {
    private static final int TRAIL = 10;
    private static final int MISSING_LIMIT = 100;
    private final int witherId;
    private final Vec3[][] trails = new Vec3[3][TRAIL];
    private int trailCount;
    private int trailHead;
    private int missing;
    private boolean finished;
    private float strength;
    private float strengthO;
    private float rage;
    private float rageO;
    private float awaken;
    private float awakenO;

    public AuraVfx(int witherId) {
        super(Integer.MAX_VALUE);
        this.witherId = witherId;
    }

    public boolean finished() {
        return this.finished;
    }

    public float rage() {
        return this.rage;
    }

    @Override
    public boolean disposable() {
        return false;
    }

    @Override
    public boolean tick() {
        this.age++;
        this.onTick();
        return !this.finished;
    }

    @Override
    protected void onTick() {
        this.strengthO = this.strength;
        this.rageO = this.rage;
        this.awakenO = this.awaken;
        WitherBoss wither = wither(this.witherId);
        if (wither == null) {
            this.missing++;
            if (this.missing > MISSING_LIMIT) {
                this.finished = true;
                WitherStageClient.remove(this.witherId);
            }
            return;
        }
        this.missing = 0;
        int stage = WitherStageClient.stage(this.witherId);
        float targetStrength = 0.0F;
        if (stage >= 1 && wither.isAlive()) {
            targetStrength = 1.0F;
        }
        float targetRage = 0.0F;
        if (stage >= 3 && wither.isAlive()) {
            targetRage = 1.0F;
        }
        float targetAwaken = Mth.clamp(wither.getInvulnerableTicks() / 220.0F, 0.0F, 1.0F);
        this.strength = approach(this.strength, targetStrength, 0.04F);
        this.rage = approach(this.rage, targetRage, 0.025F);
        this.awaken = approach(this.awaken, targetAwaken, 0.1F);
        this.trailHead = (this.trailHead + 1) % TRAIL;
        for (int i = 0; i < 3; i++) {
            this.trails[i][this.trailHead] = head(wither, i, 1.0F);
        }
        this.trailCount = Math.min(TRAIL, this.trailCount + 1);
    }

    private static float approach(float value, float target, float step) {
        if (value < target) {
            return Math.min(target, value + step);
        }
        return Math.max(target, value - step);
    }

    @Override
    public void render(float partialTick) {
        WitherBoss wither = wither(this.witherId);
        if (wither == null || wither.isInvisible()) {
            return;
        }
        float strength = Mth.lerp(partialTick, this.strengthO, this.strength);
        float rage = Mth.lerp(partialTick, this.rageO, this.rage);
        float awaken = Mth.lerp(partialTick, this.awakenO, this.awaken);
        if (strength < 0.01F && awaken < 0.01F) {
            return;
        }
        float t = this.time(partialTick);
        int stage = WitherStageClient.stage(this.witherId);
        int rgb = WitherStageClient.color(Math.max(1, stage));
        int hot = Draw.mix(rgb, 0xFFFFFF, 0.5F);
        Vec3 body = center(wither, partialTick);
        Vec3 feet = lerpPos(wither, partialTick);
        if (strength > 0.01F && Draw.begin(Draw.Material.SMOKE)) {
            for (int i = 0; i < 3; i++) {
                for (int k = 1; k < this.trailCount; k++) {
                    int index = Math.floorMod(this.trailHead - k, TRAIL);
                    Vec3 p = this.trails[i][index];
                    if (p == null) {
                        continue;
                    }
                    float fadeK = 1.0F - k / (float) TRAIL;
                    Draw.billboard(p.add(0.0, 0.15 * k, 0.0), 0.5F + 0.12F * k, k * 0.7F + t * 0.05F, Draw.ASH, strength * fadeK * 0.5F);
                }
            }
            for (int i = 0; i < 6; i++) {
                float phase = (hash(i + 5) + t * 0.02F) % 1.0F;
                float angle = hash(i + 41) * Mth.TWO_PI + t * 0.01F;
                Vec3 p = body.add(Mth.cos(angle) * 1.4, -1.2 - phase * 2.5, Mth.sin(angle) * 1.4);
                Draw.billboard(p, 0.9F + phase, phase * 3.0F, Draw.ASH, strength * Mth.sin(phase * Mth.PI) * 0.5F);
            }
            Draw.end(Draw.FLAME_SMOKE, 0.6F, 1.5F, 0.0F);
        }
        if (strength > 0.01F && Draw.begin(Draw.Material.FLAME)) {
            for (int i = 0; i < 3; i++) {
                Vec3 eye = head(wither, i, partialTick);
                float size = 0.55F;
                if (i == 0) {
                    size = 0.75F;
                }
                float flicker = 0.85F + 0.15F * Mth.sin(t * 0.7F + i * 2.1F);
                Draw.billboard(eye, size * flicker * (1.0F + 0.3F * rage), hot, strength * 0.9F);
            }
            Draw.end(Draw.FLAME_BALL, 1.0F, 2.0F, 0.8F);
        }
        if (stage >= 2 && strength > 0.01F && Draw.begin(Draw.Material.SPHERE)) {
            Draw.sphere(body, 2.4F + 0.15F * Mth.sin(t * 0.1F), 10, 16, rgb, strength * 0.28F);
            Draw.end(Draw.SPHERE_SHELL, 2.0F, 1.5F, 0.015F);
        }
        if (rage > 0.01F && Draw.begin(Draw.Material.FLAME)) {
            Draw.cylinder(feet.add(0.0, -0.5, 0.0), feet.add(0.0, 4.2, 0.0), 1.3F, 0.4F, 14, Draw.HELL, rage * 0.6F, 2.0F);
            for (int i = 0; i < 5; i++) {
                float phase = (hash(i + 77) + t * 0.035F) % 1.0F;
                float angle = hash(i + 13) * Mth.TWO_PI;
                Vec3 p = body.add(Mth.cos(angle) * 1.1, phase * 2.5 - 0.8, Mth.sin(angle) * 1.1);
                Draw.billboard(p, 0.45F * (1.0F - phase), Draw.HELL, rage * (1.0F - phase));
            }
            Draw.end(Draw.FLAME_COLUMN, 2.4F, 1.5F, 0.7F);
        }
        if (awaken > 0.01F) {
            float charge = 1.0F - awaken;
            if (Draw.begin(Draw.Material.GROUND)) {
                Draw.ground(feet, 5.0F + charge * 3.0F, Draw.SOUL, awaken);
                Draw.end(Draw.GROUND_SIGIL, charge, 2.3F, 0.0F);
            }
            if (Draw.begin(Draw.Material.FLAME)) {
                Draw.cylinder(feet, feet.add(0.0, 7.0, 0.0), 3.5F - charge * 1.5F, 1.0F, 18, Draw.SOUL, awaken * 0.55F, 3.0F);
                Draw.ring(body, new Vec3(0.0, 1.0, 0.0), 1.5F + charge * 2.0F, 2.6F + charge * 2.0F, 32, Draw.SOUL, 0.0F, awaken * 0.8F, 6.0F);
                Draw.end(Draw.FLAME_COLUMN, 3.0F, 2.0F, 0.6F);
            }
        }
    }
}
