package com.witherplus.wither.hazard;

import com.witherplus.wither.WitherBrain;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PillarHazard extends Hazard {
    private final Vec3 pos;
    private final int delay;
    private final double radius;
    private final double height;
    private final double multiplier;

    public PillarHazard(Vec3 pos, int delay, double radius, double height, double multiplier) {
        this.pos = pos;
        this.delay = delay;
        this.radius = radius;
        this.height = height;
        this.multiplier = multiplier;
    }

    @Override
    public boolean tick(ServerLevel level, WitherBrain brain) {
        this.age++;
        if (this.age < this.delay) {
            return true;
        }
        AABB box = new AABB(this.pos.x - this.radius, this.pos.y - 1.0, this.pos.z - this.radius, this.pos.x + this.radius, this.pos.y + this.height, this.pos.z + this.radius);
        for (LivingEntity victim : brain.victims(box)) {
            double dx = victim.getX() - this.pos.x;
            double dz = victim.getZ() - this.pos.z;
            if (dx * dx + dz * dz <= this.radius * this.radius) {
                brain.damage(victim, this.multiplier);
                WitherBrain.push(victim, 0.0, 0.6, 0.0);
            }
        }
        level.playSound(null, this.pos.x, this.pos.y, this.pos.z, SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 3.0F, 0.6F);
        level.playSound(null, this.pos.x, this.pos.y, this.pos.z, SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.5F, 0.5F);
        return false;
    }
}
