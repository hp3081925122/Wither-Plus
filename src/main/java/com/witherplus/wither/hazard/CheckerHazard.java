package com.witherplus.wither.hazard;

import com.witherplus.wither.WitherBrain;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class CheckerHazard extends Hazard {
    private final Vec3 center;
    private final double radius;
    private final double cell;
    private final int parity;
    private final int delay;
    private final double multiplier;

    public CheckerHazard(Vec3 center, double radius, double cell, int parity, int delay, double multiplier) {
        this.center = center;
        this.radius = radius;
        this.cell = cell;
        this.parity = parity;
        this.delay = delay;
        this.multiplier = multiplier;
    }

    public static int parityAt(Vec3 center, double cell, double x, double z) {
        int cx = Mth.floor((x - center.x) / cell);
        int cz = Mth.floor((z - center.z) / cell);
        return Math.floorMod(cx + cz, 2);
    }

    @Override
    public boolean tick(ServerLevel level, WitherBrain brain) {
        this.age++;
        if (this.age < this.delay) {
            return true;
        }
        AABB box = new AABB(this.center, this.center).inflate(this.radius, 8.0, this.radius);
        for (LivingEntity victim : brain.victims(box)) {
            double dx = victim.getX() - this.center.x;
            double dz = victim.getZ() - this.center.z;
            if (dx * dx + dz * dz > this.radius * this.radius || victim.getY() - this.center.y > 7.0) {
                continue;
            }
            if (parityAt(this.center, this.cell, victim.getX(), victim.getZ()) == this.parity) {
                brain.damage(victim, this.multiplier);
                WitherBrain.push(victim, 0.0, 0.7, 0.0);
            }
        }
        level.playSound(null, this.center.x, this.center.y, this.center.z, SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 6.0F, 0.5F);
        level.playSound(null, this.center.x, this.center.y, this.center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 4.0F, 0.7F);
        return false;
    }
}
