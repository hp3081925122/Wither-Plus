package com.witherplus.wither.skill;

import com.witherplus.network.VfxPayload;
import com.witherplus.wither.WitherBrain;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.phys.Vec3;

public abstract class WitherSkill {
    protected final WitherBrain brain;
    protected final WitherBoss wither;
    protected int tick;
    @Nullable
    protected Vec3 moveTarget;
    protected double moveSpeed = 0.8;
    @Nullable
    protected Vec3 faceDirection;
    protected float turnRate = 12.0F;
    protected boolean invulnerable;
    private boolean done;

    protected WitherSkill(WitherBrain brain) {
        this.brain = brain;
        this.wither = brain.wither();
    }

    public final boolean begin() {
        return this.start();
    }

    public final void tickSkill() {
        this.tick++;
        this.serverTick();
        if (!this.done) {
            this.move();
        }
    }

    protected abstract boolean start();

    protected abstract void serverTick();

    public void end() {
    }

    protected void finish() {
        this.done = true;
    }

    public boolean done() {
        return this.done;
    }

    public boolean invulnerable() {
        return this.invulnerable;
    }

    protected Vec3 hoverAbove(Vec3 point, double height) {
        double ground = this.brain.groundY(point.x, point.z, point.y);
        return new Vec3(point.x, ground + height, point.z);
    }

    protected boolean arrived(double distance) {
        return this.moveTarget == null || this.wither.position().distanceTo(this.moveTarget) < distance;
    }

    protected Vec3 groundBelow(Vec3 point) {
        return new Vec3(point.x, this.brain.groundY(point.x, point.z, point.y), point.z);
    }

    protected Vec3 groundAt(LivingEntity entity) {
        double y = this.brain.groundY(entity.getX(), entity.getZ(), entity.getY());
        if (y > entity.getY() + 0.5) {
            y = entity.getY();
        }
        return new Vec3(entity.getX(), y, entity.getZ());
    }

    protected void sendBolts(List<Vec3> starts, List<Vec3> controls, List<Vec3> ends, int[] travels, int style) {
        CompoundTag data = new CompoundTag();
        VfxPayload.putPoints(data, "start", starts);
        VfxPayload.putPoints(data, "control", controls);
        VfxPayload.putPoints(data, "end", ends);
        data.putIntArray("travel", travels);
        data.putInt("style", style);
        this.brain.sendVfx(VfxPayload.BOLTS, data);
    }

    protected void faceTarget() {
        LivingEntity target = this.brain.target();
        if (target != null) {
            this.faceDirection = target.position().subtract(this.wither.position());
        }
    }

    private void move() {
        this.wither.setDeltaMovement(Vec3.ZERO);
        if (this.moveTarget != null) {
            Vec3 pos = this.wither.position();
            Vec3 delta = this.moveTarget.subtract(pos);
            double len = delta.length();
            if (len > 0.01) {
                double step = Math.min(len, Math.min(this.moveSpeed, Math.max(0.1, len * 0.12)));
                Vec3 next = pos.add(delta.scale(step / len));
                this.wither.setPos(next.x, next.y, next.z);
            }
        }
        if (this.faceDirection != null && this.faceDirection.x * this.faceDirection.x + this.faceDirection.z * this.faceDirection.z > 1.0E-6) {
            float yaw = (float) (Mth.atan2(this.faceDirection.z, this.faceDirection.x) * Mth.RAD_TO_DEG) - 90.0F;
            float current = this.wither.getYRot();
            float next = current + Mth.clamp(Mth.wrapDegrees(yaw - current), -this.turnRate, this.turnRate);
            this.wither.setYRot(next);
            this.wither.yBodyRot = next;
            this.wither.setYHeadRot(next);
        }
    }
}
