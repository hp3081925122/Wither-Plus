package com.witherplus.wither.skill;

import com.witherplus.WitherPlus;
import com.witherplus.WpConfig;
import com.witherplus.network.VfxPayload;
import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.WpMath;
import com.witherplus.wither.hazard.BlastHazard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.phys.Vec3;

public final class BoneCrownSkill extends WitherSkill {
    public static final int FIRST_SUMMON = 30;
    public static final int SUMMON_INTERVAL = 10;
    public static final double SUMMON_RADIUS = 8.0;
    public static final double BLAST_RADIUS = 12.0;
    private final List<Vec3> summonPoints = new ArrayList<>();
    private Vec3 center = Vec3.ZERO;
    private int duration;

    public BoneCrownSkill(WitherBrain brain) {
        super(brain);
    }

    @Override
    protected boolean start() {
        this.invulnerable = true;
        this.duration = WpConfig.CROWN_DURATION.get();
        this.center = this.groundBelow(this.wither.position());
        this.moveTarget = new Vec3(this.wither.getX(), Math.max(this.wither.getY(), this.center.y + 8.0), this.wither.getZ());
        int summons = WpConfig.CROWN_SUMMONS.get();
        float offset = this.wither.getRandom().nextFloat() * Mth.TWO_PI;
        for (int i = 0; i < summons; i++) {
            Vec3 dir = WpMath.dir(offset + i * Mth.TWO_PI / summons);
            this.summonPoints.add(this.groundBelow(this.center.add(dir.scale(SUMMON_RADIUS))));
        }
        CompoundTag data = new CompoundTag();
        VfxPayload.putVec(data, "center", this.center);
        data.putInt("duration", this.duration);
        data.putFloat("radius", (float) BLAST_RADIUS);
        this.brain.sendVfx(VfxPayload.CROWN, data);
        this.brain.playSound(this.wither.position(), SoundEvents.WITHER_SPAWN, 3.0F, 1.0F);
        return true;
    }

    @Override
    protected void serverTick() {
        this.faceTarget();
        for (int i = 0; i < this.summonPoints.size(); i++) {
            if (this.tick == FIRST_SUMMON + i * SUMMON_INTERVAL && this.tick < this.duration - 10) {
                this.summon(this.summonPoints.get(i));
            }
        }
        if (this.tick == this.duration - 10) {
            this.brain.addHazard(new BlastHazard(this.center.add(0.0, 1.0, 0.0), 1, BLAST_RADIUS, WpConfig.BONE_CROWN.damage.get(), 1.0, 0.5, SoundEvents.GENERIC_EXPLODE.value(), 4.0F, 0.6F));
            CompoundTag data = new CompoundTag();
            VfxPayload.putVec(data, "pos", this.center.add(0.0, 1.0, 0.0));
            data.putFloat("radius", (float) BLAST_RADIUS);
            this.brain.sendVfx(VfxPayload.BURST, data);
        }
        if (this.tick >= this.duration) {
            this.finish();
        }
    }

    private void summon(Vec3 point) {
        WitherSkeleton skeleton = EntityType.WITHER_SKELETON.spawn(this.brain.level(), BlockPos.containing(point), EntitySpawnReason.MOB_SUMMONED);
        if (skeleton == null) {
            if (WpConfig.DEBUG_LOGGING.get()) {
                WitherPlus.LOGGER.info("Bone crown failed to summon wither skeleton at {}", point);
            }
            return;
        }
        LivingEntity target = this.brain.target();
        if (target != null) {
            skeleton.setTarget(target);
        }
        List<Vec3> points = new ArrayList<>();
        points.add(point);
        CompoundTag data = new CompoundTag();
        VfxPayload.putPoints(data, "points", points);
        data.putInt("delay", 0);
        data.putInt("style", 1);
        data.putFloat("radius", 1.2F);
        this.brain.sendVfx(VfxPayload.PILLAR, data);
        this.brain.playSound(point, SoundEvents.EVOKER_PREPARE_SUMMON, 2.0F, 0.6F);
    }
}
