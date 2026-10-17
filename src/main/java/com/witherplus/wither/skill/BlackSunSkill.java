package com.witherplus.wither.skill;

import com.witherplus.WpConfig;
import com.witherplus.network.VfxPayload;
import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.WpMath;
import com.witherplus.wither.hazard.BlastHazard;
import com.witherplus.wither.hazard.BoltHazard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BlackSunSkill extends WitherSkill {
    public static final int FORM = 20;
    public static final int ACTIVE = 100;
    public static final int COLLAPSE = 15;
    public static final double PULL_RANGE = 20.0;
    public static final int BOLT_TRAVEL = 22;
    private Vec3 sun = Vec3.ZERO;

    public BlackSunSkill(WitherBrain brain) {
        super(brain);
    }

    @Override
    protected boolean start() {
        if (this.brain.target() == null) {
            return false;
        }
        this.moveTarget = this.wither.position();
        this.sun = this.wither.position().add(0.0, 7.5, 0.0);
        CompoundTag data = new CompoundTag();
        VfxPayload.putVec(data, "pos", this.sun);
        data.putInt("form", FORM);
        data.putInt("active", ACTIVE);
        data.putInt("collapse", COLLAPSE);
        this.brain.sendVfx(VfxPayload.BLACK_SUN, data);
        this.brain.playSound(this.sun, SoundEvents.BEACON_DEACTIVATE, 5.0F, 0.5F);
        this.brain.playSound(this.wither.position(), SoundEvents.WITHER_AMBIENT, 4.0F, 0.4F);
        return true;
    }

    @Override
    protected void serverTick() {
        this.faceTarget();
        if (this.tick < FORM) {
            return;
        }
        int a = this.tick - FORM;
        if (a < ACTIVE) {
            double pull = WpConfig.SUN_PULL.get();
            for (LivingEntity victim : this.brain.victims(new AABB(this.sun, this.sun).inflate(PULL_RANGE))) {
                Vec3 d = new Vec3(this.sun.x - victim.getX(), 0.0, this.sun.z - victim.getZ());
                double len = d.length();
                if (len > 2.0 && len < PULL_RANGE) {
                    WitherBrain.pull(victim, d.scale(pull * (1.0 - 0.5 * len / PULL_RANGE) / len));
                }
            }
            if (a % 5 == 0) {
                this.fireBolts(a);
            }
            if (a % 20 == 0) {
                this.brain.playSound(this.sun, SoundEvents.SOUL_ESCAPE, 4.0F, 0.4F);
            }
            return;
        }
        if (a - ACTIVE >= COLLAPSE) {
            Vec3 ground = this.groundBelow(new Vec3(this.sun.x, this.wither.getY(), this.sun.z));
            this.brain.addHazard(new BlastHazard(ground.add(0.0, 1.0, 0.0), 1, 9.0, WpConfig.BLACK_SUN.damage.get(), 1.2, 0.6, SoundEvents.GENERIC_EXPLODE, 4.0F, 0.5F));
            this.brain.playSound(this.sun, SoundEvents.WARDEN_SONIC_BOOM, 4.0F, 0.6F);
            this.finish();
        }
    }

    private void fireBolts(int a) {
        double multiplier = WpConfig.SUN_BOLT_MULTIPLIER.get();
        List<Vec3> starts = new ArrayList<>();
        List<Vec3> controls = new ArrayList<>();
        List<Vec3> ends = new ArrayList<>();
        int[] travels = new int[3];
        for (int k = 0; k < 3; k++) {
            float angle = a * 0.37F + k * Mth.TWO_PI / 3.0F;
            Vec3 dir = WpMath.dir(angle);
            Vec3 side = new Vec3(-dir.z, 0.0, dir.x);
            Vec3 start = this.sun.add(dir.scale(2.5));
            double reach = 6.0 + this.wither.getRandom().nextDouble() * 12.0;
            Vec3 end = this.groundBelow(new Vec3(this.sun.x + dir.x * reach, this.wither.getY(), this.sun.z + dir.z * reach));
            Vec3 control = start.add(end).scale(0.5).add(0.0, 6.0, 0.0).add(side.scale(4.0));
            starts.add(start);
            controls.add(control);
            ends.add(end);
            travels[k] = BOLT_TRAVEL;
            this.brain.addHazard(new BoltHazard(start, control, end, BOLT_TRAVEL, 0.8, 2.2, multiplier));
        }
        this.sendBolts(starts, controls, ends, travels, 1);
    }
}
