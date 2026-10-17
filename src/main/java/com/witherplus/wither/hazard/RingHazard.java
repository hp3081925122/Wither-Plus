package com.witherplus.wither.hazard;

import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.WpMath;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class RingHazard extends Hazard {
    private static final double THICKNESS = 1.1;
    private final Vec3 center;
    private final int delay;
    private final double speed;
    private final double maxRadius;
    private final double multiplier;
    private final float gapAngle;
    private final float gapHalf;
    private final boolean jumpable;
    private final double height;
    private final IntOpenHashSet hit = new IntOpenHashSet();

    public RingHazard(Vec3 center, int delay, double speed, double maxRadius, double multiplier, float gapAngle, float gapHalf, boolean jumpable, double height) {
        this.center = center;
        this.delay = delay;
        this.speed = speed;
        this.maxRadius = maxRadius;
        this.multiplier = multiplier;
        this.gapAngle = gapAngle;
        this.gapHalf = gapHalf;
        this.jumpable = jumpable;
        this.height = height;
    }

    @Override
    public boolean tick(ServerLevel level, WitherBrain brain) {
        this.age++;
        int local = this.age - this.delay;
        if (local < 0) {
            return true;
        }
        double r = local * this.speed;
        if (r > this.maxRadius) {
            return false;
        }
        AABB box = new AABB(this.center, this.center).inflate(r + THICKNESS, this.height + 1.0, r + THICKNESS);
        for (LivingEntity victim : brain.victims(box)) {
            if (this.hit.contains(victim.getId())) {
                continue;
            }
            if (this.jumpable && !victim.onGround()) {
                continue;
            }
            double dy = victim.getY() - this.center.y;
            if (dy < -1.5 || dy >= this.height) {
                continue;
            }
            double dx = victim.getX() - this.center.x;
            double dz = victim.getZ() - this.center.z;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (Math.abs(d - r) > THICKNESS) {
                continue;
            }
            if (this.gapHalf > 0.0F && WpMath.inGap(WpMath.yawOf(dx, dz), this.gapAngle, this.gapHalf)) {
                continue;
            }
            this.hit.add(victim.getId());
            brain.damage(victim, this.multiplier);
            brain.knockFrom(victim, this.center, 0.7, 0.4);
        }
        return true;
    }
}
