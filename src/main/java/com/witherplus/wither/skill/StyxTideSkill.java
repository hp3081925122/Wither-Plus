package com.witherplus.wither.skill;

import com.witherplus.WpConfig;
import com.witherplus.network.VfxPayload;
import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.hazard.RingHazard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class StyxTideSkill extends WitherSkill {
    public static final int FIRST = 20;
    public static final int INTERVAL = 30;
    public static final double SPEED = 0.5;
    public static final double MAX_RADIUS = 30.0;
    public static final float GAP_HALF = 0.35F;
    public static final double HEIGHT = 3.5;
    private Vec3 center = Vec3.ZERO;
    private int waves;

    public StyxTideSkill(WitherBrain brain) {
        super(brain);
    }

    @Override
    protected boolean start() {
        if (this.brain.target() == null) {
            return false;
        }
        this.center = this.groundBelow(this.wither.position());
        this.waves = WpConfig.TIDE_WAVES.get();
        this.moveTarget = this.center.add(0.0, 10.0, 0.0);
        this.brain.playSound(this.wither.position(), SoundEvents.WITHER_AMBIENT, 3.0F, 0.5F);
        return true;
    }

    @Override
    protected void serverTick() {
        this.faceTarget();
        int local = this.tick - FIRST;
        if (local >= 0 && local % INTERVAL == 0 && local / INTERVAL < this.waves) {
            float gap = this.wither.getRandom().nextFloat() * Mth.TWO_PI;
            this.brain.addHazard(new RingHazard(this.center, 0, SPEED, MAX_RADIUS, WpConfig.STYX_TIDE.damage.get(), gap, GAP_HALF, false, HEIGHT));
            CompoundTag data = new CompoundTag();
            VfxPayload.putVec(data, "center", this.center);
            data.putFloat("speed", (float) SPEED);
            data.putFloat("radius", (float) MAX_RADIUS);
            data.putFloat("gap", gap);
            data.putFloat("gapHalf", GAP_HALF);
            data.putFloat("height", (float) HEIGHT);
            data.putBoolean("jumpable", false);
            this.brain.sendVfx(VfxPayload.TIDE, data);
            this.brain.playSound(this.center, SoundEvents.GENERIC_EXPLODE.value(), 3.0F, 0.5F);
            this.brain.playSound(this.center, SoundEvents.SOUL_ESCAPE.value(), 4.0F, 0.4F);
        }
        if (this.tick >= FIRST + (this.waves - 1) * INTERVAL + (int) (MAX_RADIUS / SPEED) + 5) {
            this.finish();
        }
    }
}
