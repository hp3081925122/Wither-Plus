package com.witherplus.wither.skill;

import com.witherplus.WpConfig;
import com.witherplus.network.VfxPayload;
import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.WpMath;
import com.witherplus.wither.hazard.BlastHazard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class BoneSpiresSkill extends WitherSkill {
    public static final int STEPS = 10;
    public static final double SPACING = 2.2;
    public static final int STEP_DELAY = 3;
    public static final int FIRST = 15;
    public static final int SECOND = 25;
    private int duration;

    public BoneSpiresSkill(WitherBrain brain) {
        super(brain);
    }

    @Override
    protected boolean start() {
        LivingEntity target = this.brain.target();
        if (target == null) {
            return false;
        }
        Vec3 origin = this.groundBelow(this.wither.position());
        float base = WpMath.yawOf(target.getX() - origin.x, target.getZ() - origin.z);
        int rays = WpConfig.SPIRE_RAYS.get();
        double multiplier = WpConfig.BONE_SPIRES.damage.get();
        List<Vec3> points = new ArrayList<>();
        List<Integer> delays = new ArrayList<>();
        for (int set = 0; set < 2; set++) {
            float offset = set * Mth.PI / rays;
            int startDelay = FIRST + set * SECOND;
            for (int r = 0; r < rays; r++) {
                float angle = base + offset + r * Mth.TWO_PI / rays;
                for (int j = 1; j <= STEPS; j++) {
                    double x = origin.x + Mth.cos(angle) * SPACING * j;
                    double z = origin.z + Mth.sin(angle) * SPACING * j;
                    Vec3 point = new Vec3(x, this.brain.groundY(x, z, origin.y), z);
                    int delay = startDelay + j * STEP_DELAY;
                    SoundEvent sound = null;
                    if (j % 3 == 1) {
                        sound = SoundEvents.BONE_BLOCK_BREAK;
                    }
                    points.add(point);
                    delays.add(delay);
                    this.brain.addHazard(new BlastHazard(point.add(0.0, 0.5, 0.0), delay, 1.4, multiplier, 0.0, 0.9, sound, 1.2F, 0.6F));
                }
            }
        }
        this.duration = FIRST + SECOND + STEPS * STEP_DELAY + 20;
        this.moveTarget = this.hoverAbove(this.wither.position(), 5.0);
        this.faceTarget();
        int[] delayArray = new int[delays.size()];
        for (int i = 0; i < delayArray.length; i++) {
            delayArray[i] = delays.get(i);
        }
        CompoundTag data = new CompoundTag();
        VfxPayload.putPoints(data, "points", points);
        data.putIntArray("delays", delayArray);
        VfxPayload.putVec(data, "origin", origin);
        this.brain.sendVfx(VfxPayload.SPIRES, data);
        this.brain.playSound(this.wither.position(), SoundEvents.WITHER_AMBIENT, 3.0F, 0.4F);
        return true;
    }

    @Override
    protected void serverTick() {
        this.faceTarget();
        if (this.tick >= this.duration) {
            this.finish();
        }
    }
}
