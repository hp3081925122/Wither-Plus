package com.witherplus.wither.hazard;

import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.WpMath;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BoltHazard extends Hazard {
    private final Vec3 start;
    private final Vec3 control;
    private final Vec3 end;
    private final int travel;
    private final double hitRadius;
    private final double blastRadius;
    private final double multiplier;

    public BoltHazard(Vec3 start, Vec3 control, Vec3 end, int travel, double hitRadius, double blastRadius, double multiplier) {
        this.start = start;
        this.control = control;
        this.end = end;
        this.travel = travel;
        this.hitRadius = hitRadius;
        this.blastRadius = blastRadius;
        this.multiplier = multiplier;
    }

    @Override
    public boolean tick(ServerLevel level, WitherBrain brain) {
        this.age++;
        Vec3 pos = WpMath.bezier(this.start, this.control, this.end, Math.min(1.0, this.age / (double) this.travel));
        boolean impact = this.age >= this.travel;
        if (!impact && this.age > 2) {
            BlockPos block = BlockPos.containing(pos);
            impact = !level.getBlockState(block).getCollisionShape(level, block).isEmpty();
        }
        if (!impact) {
            for (LivingEntity victim : brain.victims(new AABB(pos, pos).inflate(this.hitRadius + 1.0))) {
                if (victim.getBoundingBox().inflate(this.hitRadius).contains(pos)) {
                    impact = true;
                    break;
                }
            }
        }
        if (!impact) {
            return true;
        }
        for (LivingEntity victim : brain.victims(new AABB(pos, pos).inflate(this.blastRadius))) {
            if (victim.getBoundingBox().inflate(this.blastRadius * 0.5).contains(pos) || victim.position().distanceTo(pos) <= this.blastRadius) {
                brain.damage(victim, this.multiplier);
                brain.knockFrom(victim, pos, 0.3, 0.2);
            }
        }
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 0.6F, 1.6F + level.random.nextFloat() * 0.3F);
        return false;
    }
}
