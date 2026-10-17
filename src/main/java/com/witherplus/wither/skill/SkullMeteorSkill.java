package com.witherplus.wither.skill;

import com.witherplus.WpConfig;
import com.witherplus.network.VfxPayload;
import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.hazard.BlastHazard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class SkullMeteorSkill extends WitherSkill {
    public static final int FIRST = 30;
    public static final int SPACING = 8;
    public static final int FALL = 20;
    private final List<Vec3> skies = new ArrayList<>();
    private int[] times = new int[0];
    private int duration;

    public SkullMeteorSkill(WitherBrain brain) {
        super(brain);
    }

    @Override
    protected boolean start() {
        List<LivingEntity> targets = this.brain.combatTargets();
        if (targets.isEmpty()) {
            return false;
        }
        RandomSource random = this.wither.getRandom();
        int count = WpConfig.METEOR_COUNT.get();
        double radius = WpConfig.METEOR_RADIUS.get();
        double multiplier = WpConfig.SKULL_METEOR.damage.get();
        List<Vec3> impacts = new ArrayList<>();
        this.times = new int[count];
        for (int i = 0; i < count; i++) {
            LivingEntity target = targets.get(i % targets.size());
            Vec3 base = this.groundAt(target);
            double spread = 0.0;
            if (i >= targets.size()) {
                spread = 5.0;
            }
            float angle = random.nextFloat() * Mth.TWO_PI;
            double r = spread * Math.sqrt(random.nextDouble());
            double x = base.x + Mth.cos(angle) * r;
            double z = base.z + Mth.sin(angle) * r;
            Vec3 impact = new Vec3(x, this.brain.groundY(x, z, base.y), z);
            Vec3 sky = impact.add(random.nextDouble() * 16.0 - 8.0, 26.0, random.nextDouble() * 16.0 - 8.0);
            int time = FIRST + i * SPACING;
            impacts.add(impact);
            this.skies.add(sky);
            this.times[i] = time;
            this.brain.addHazard(new BlastHazard(impact.add(0.0, 0.5, 0.0), time, radius, multiplier, 0.6, 0.45, SoundEvents.GENERIC_EXPLODE, 2.0F, 0.8F));
        }
        this.duration = FIRST + (count - 1) * SPACING + 15;
        this.moveTarget = this.wither.position().add(0.0, 4.0, 0.0);
        this.faceTarget();
        CompoundTag data = new CompoundTag();
        VfxPayload.putPoints(data, "impact", impacts);
        VfxPayload.putPoints(data, "sky", this.skies);
        data.putIntArray("times", this.times);
        data.putInt("fall", FALL);
        data.putFloat("radius", (float) radius);
        this.brain.sendVfx(VfxPayload.METEOR, data);
        this.brain.playSound(this.wither.position(), SoundEvents.WITHER_AMBIENT, 4.0F, 0.5F);
        return true;
    }

    @Override
    protected void serverTick() {
        this.faceTarget();
        for (int i = 0; i < this.times.length; i++) {
            if (this.tick == this.times[i] - FALL) {
                this.brain.playSound(this.skies.get(i), SoundEvents.WITHER_SHOOT, 3.0F, 0.6F);
            }
        }
        if (this.tick >= this.duration) {
            this.finish();
        }
    }
}
