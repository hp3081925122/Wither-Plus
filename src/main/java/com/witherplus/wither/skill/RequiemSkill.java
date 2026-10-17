package com.witherplus.wither.skill;

import com.witherplus.WpConfig;
import com.witherplus.network.VfxPayload;
import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.hazard.CheckerHazard;
import com.witherplus.wither.hazard.RingHazard;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

public final class RequiemSkill extends WitherSkill {
    public static final double CELL = 5.0;
    public static final int WAVE_A = 75;
    public static final int WAVE_B = 105;
    public static final int RING = 130;
    public static final double RING_SPEED = 0.7;
    public static final double RING_RADIUS = 30.0;
    public static final int DURATION = 175;
    private Vec3 center = Vec3.ZERO;

    public RequiemSkill(WitherBrain brain) {
        super(brain);
    }

    @Override
    protected boolean start() {
        this.invulnerable = true;
        this.center = this.groundBelow(this.wither.position());
        this.moveTarget = this.center.add(0.0, 14.0, 0.0);
        double radius = WpConfig.REQUIEM_RADIUS.get();
        double multiplier = WpConfig.REQUIEM.damage.get();
        this.brain.addHazard(new CheckerHazard(this.center, radius, CELL, 0, WAVE_A, multiplier));
        this.brain.addHazard(new CheckerHazard(this.center, radius, CELL, 1, WAVE_B, multiplier));
        this.brain.addHazard(new RingHazard(this.center, RING, RING_SPEED, RING_RADIUS, multiplier * 0.6, 0.0F, 0.0F, true, 2.0));
        CompoundTag data = new CompoundTag();
        VfxPayload.putVec(data, "center", this.center);
        data.putFloat("radius", (float) radius);
        data.putFloat("cell", (float) CELL);
        data.putInt("waveA", WAVE_A);
        data.putInt("waveB", WAVE_B);
        data.putInt("ring", RING);
        data.putFloat("ringSpeed", (float) RING_SPEED);
        data.putFloat("ringRadius", (float) RING_RADIUS);
        data.putInt("duration", DURATION);
        this.brain.sendVfx(VfxPayload.REQUIEM, data);
        this.brain.playSound(this.wither.position(), SoundEvents.WITHER_SPAWN, 5.0F, 0.5F);
        return true;
    }

    @Override
    protected void serverTick() {
        this.faceTarget();
        if (this.tick == WAVE_A - 20 || this.tick == WAVE_B - 20) {
            this.brain.playSound(this.center, SoundEvents.BELL_RESONATE, 4.0F, 0.5F);
        }
        if (this.tick == RING) {
            this.brain.playSound(this.center, SoundEvents.WARDEN_SONIC_BOOM, 4.0F, 0.5F);
        }
        if (this.tick >= DURATION) {
            this.finish();
        }
    }
}
