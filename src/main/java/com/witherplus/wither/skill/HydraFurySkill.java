package com.witherplus.wither.skill;

import com.witherplus.WpConfig;
import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.hazard.BoltHazard;
import java.util.List;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class HydraFurySkill extends WitherSkill {
    public static final int WINDUP = 10;
    public static final int INTERVAL = 3;
    private Vec3 lastPos = Vec3.ZERO;
    private int fired;
    private int lastFire;

    public HydraFurySkill(WitherBrain brain) {
        super(brain);
    }

    @Override
    protected boolean start() {
        LivingEntity target = this.brain.target();
        if (target == null) {
            return false;
        }
        this.lastPos = target.position();
        this.moveTarget = this.wither.position().add(0.0, 2.0, 0.0);
        this.brain.playSound(this.wither.position(), SoundEvents.WITHER_AMBIENT, 3.0F, 1.2F);
        return true;
    }

    @Override
    protected void serverTick() {
        LivingEntity target = this.brain.target();
        if (target == null) {
            this.finish();
            return;
        }
        this.faceTarget();
        Vec3 velocity = target.position().subtract(this.lastPos);
        this.lastPos = target.position();
        if (this.fired >= WpConfig.HYDRA_BOLTS.get()) {
            if (this.tick > this.lastFire + 30) {
                this.finish();
            }
            return;
        }
        if (this.tick < WINDUP || (this.tick - WINDUP) % INTERVAL != 0) {
            return;
        }
        RandomSource random = this.wither.getRandom();
        int head = this.fired % 3;
        Vec3 start = this.brain.head(head);
        Vec3 aim = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).add(velocity.scale(6.0));
        double distance = start.distanceTo(aim);
        int travel = Mth.clamp((int) (distance / 0.9), 12, 40);
        Vec3 flat = new Vec3(aim.x - start.x, 0.0, aim.z - start.z);
        Vec3 side = Vec3.ZERO;
        if (flat.lengthSqr() > 1.0E-4) {
            flat = flat.normalize();
            side = new Vec3(-flat.z, 0.0, flat.x);
        }
        double sign = 1.0;
        if (this.fired % 2 == 1) {
            sign = -1.0;
        }
        Vec3 control = start.add(aim).scale(0.5).add(0.0, 4.0 + random.nextDouble() * 4.0, 0.0).add(side.scale(sign * (2.0 + random.nextDouble() * 3.0)));
        this.brain.addHazard(new BoltHazard(start, control, aim, travel, 0.7, 1.8, WpConfig.HYDRA_FURY.damage.get()));
        this.sendBolts(List.of(start), List.of(control), List.of(aim), new int[]{travel}, 0);
        this.brain.playSound(start, SoundEvents.WITHER_SHOOT, 1.0F, 1.0F + random.nextFloat() * 0.4F);
        this.fired++;
        this.lastFire = this.tick;
    }
}
