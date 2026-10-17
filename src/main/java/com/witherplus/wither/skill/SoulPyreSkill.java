package com.witherplus.wither.skill;

import com.witherplus.WpConfig;
import com.witherplus.network.VfxPayload;
import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.WpMath;
import com.witherplus.wither.hazard.ScorchHazard;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SoulPyreSkill extends WitherSkill {
    public static final int CHARGE = 20;
    public static final int SWEEP = 60;
    public static final float HALF_ARC = 0.7F;
    public static final int SCORCH_INTERVAL = 8;
    private final Int2IntOpenHashMap hitTimes = new Int2IntOpenHashMap();
    private float centerAngle;

    public SoulPyreSkill(WitherBrain brain) {
        super(brain);
    }

    @Override
    protected boolean start() {
        LivingEntity target = this.brain.target();
        if (target == null) {
            return false;
        }
        Vec3 away = new Vec3(this.wither.getX() - target.getX(), 0.0, this.wither.getZ() - target.getZ());
        if (away.lengthSqr() < 1.0) {
            away = WpMath.dir(this.wither.getRandom().nextFloat() * Mth.TWO_PI);
        }
        Vec3 anchor = target.position().add(away.normalize().scale(12.0));
        this.moveTarget = this.hoverAbove(anchor, 7.0);
        this.centerAngle = WpMath.yawOf(target.getX() - this.moveTarget.x, target.getZ() - this.moveTarget.z);
        this.faceDirection = WpMath.dir(this.centerAngle);
        CompoundTag data = new CompoundTag();
        data.putFloat("center", this.centerAngle);
        data.putFloat("half", HALF_ARC);
        data.putInt("charge", CHARGE);
        data.putInt("sweep", SWEEP);
        this.brain.sendVfx(VfxPayload.PYRE, data);
        this.brain.playSound(this.wither.position(), SoundEvents.WITHER_AMBIENT, 3.0F, 0.6F);
        return true;
    }

    @Override
    protected void serverTick() {
        if (this.tick < CHARGE) {
            return;
        }
        if (this.tick == CHARGE) {
            this.moveTarget = this.wither.position();
            this.brain.playSound(this.wither.position(), SoundEvents.FIRECHARGE_USE, 3.0F, 0.5F);
        }
        int s = this.tick - CHARGE;
        if (s > SWEEP) {
            this.finish();
            return;
        }
        if (s % 10 == 0) {
            this.brain.playSound(this.wither.position(), SoundEvents.BLAZE_SHOOT, 2.5F, 0.5F);
        }
        if (s % 4 != 0) {
            return;
        }
        float progress = s / (float) SWEEP;
        double scorch = WpConfig.PYRE_SCORCH_MULTIPLIER.get();
        double multiplier = WpConfig.SOUL_PYRE.damage.get();
        for (int head = 0; head < 3; head++) {
            Vec3 offset = WpMath.pyreOffset(this.centerAngle, HALF_ARC, head, progress);
            double px = this.wither.getX() + offset.x;
            double pz = this.wither.getZ() + offset.z;
            Vec3 aim = new Vec3(px, this.brain.groundY(px, pz, this.wither.getY() - 7.0), pz);
            Vec3 from = this.brain.head(head);
            for (LivingEntity victim : this.brain.victims(new AABB(from, aim).inflate(2.0))) {
                Vec3 center = victim.position().add(0.0, victim.getBbHeight() * 0.5, 0.0);
                if (WpMath.segmentDistance(center, from, aim) < 1.5 + victim.getBbWidth() * 0.5 && this.hitTimes.getOrDefault(victim.getId(), -100) + 10 <= this.tick) {
                    this.hitTimes.put(victim.getId(), this.tick);
                    if (this.brain.damage(victim, multiplier)) {
                        victim.igniteForSeconds(3);
                    }
                }
            }
            if (s % SCORCH_INTERVAL == 0) {
                this.brain.addHazard(new ScorchHazard(aim, 1.8, 80, 10, scorch));
            }
        }
    }
}
