package com.witherplus.wither.hazard;

import com.witherplus.wither.WitherBrain;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ScorchHazard extends Hazard {
    private final Vec3 pos;
    private final double radius;
    private final int duration;
    private final int interval;
    private final double multiplier;

    public ScorchHazard(Vec3 pos, double radius, int duration, int interval, double multiplier) {
        this.pos = pos;
        this.radius = radius;
        this.duration = duration;
        this.interval = interval;
        this.multiplier = multiplier;
    }

    @Override
    public boolean tick(ServerLevel level, WitherBrain brain) {
        this.age++;
        if (this.age % this.interval == 0) {
            AABB box = new AABB(this.pos, this.pos).inflate(this.radius, 2.0, this.radius);
            for (LivingEntity victim : brain.victims(box)) {
                double dx = victim.getX() - this.pos.x;
                double dz = victim.getZ() - this.pos.z;
                if (dx * dx + dz * dz <= this.radius * this.radius && Math.abs(victim.getY() - this.pos.y) < 2.0) {
                    brain.damage(victim, this.multiplier);
                    victim.setSecondsOnFire(2);
                }
            }
        }
        return this.age < this.duration;
    }
}
