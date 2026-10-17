package com.witherplus.wither.skill;

import com.witherplus.WpConfig;
import com.witherplus.network.VfxPayload;
import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.WpMath;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class MaelstromSkill extends WitherSkill {
    public static final int DURATION = 100;
    public static final int ARMS = 3;
    public static final double ARM_WIDTH = 1.6;
    private final Int2IntOpenHashMap hitTimes = new Int2IntOpenHashMap();
    private Vec3 center = Vec3.ZERO;
    private float base;
    private float maxRadius;

    public MaelstromSkill(WitherBrain brain) {
        super(brain);
    }

    @Override
    protected boolean start() {
        if (this.brain.target() == null) {
            return false;
        }
        this.center = this.groundBelow(this.wither.position());
        this.base = this.wither.getRandom().nextFloat() * Mth.TWO_PI;
        this.maxRadius = WpConfig.MAELSTROM_RADIUS.get().floatValue();
        this.moveTarget = this.center.add(0.0, 5.0, 0.0);
        this.turnRate = 30.0F;
        CompoundTag data = new CompoundTag();
        VfxPayload.putVec(data, "center", this.center);
        data.putFloat("base", this.base);
        data.putInt("duration", DURATION);
        data.putFloat("radius", this.maxRadius);
        data.putInt("arms", ARMS);
        this.brain.sendVfx(VfxPayload.MAELSTROM, data);
        this.brain.playSound(this.center, SoundEvents.ELDER_GUARDIAN_CURSE, 3.0F, 0.5F);
        return true;
    }

    @Override
    protected void serverTick() {
        if (this.tick > DURATION) {
            this.finish();
            return;
        }
        float t = this.tick;
        this.faceDirection = WpMath.dir(this.base + t * 0.35F);
        float radius = WpMath.maelstromRadius(t, DURATION, this.maxRadius);
        double multiplier = WpConfig.MAELSTROM.damage.get();
        for (LivingEntity victim : this.brain.victims(new AABB(this.center, this.center).inflate(radius + 2.0, 5.0, radius + 2.0))) {
            double dx = victim.getX() - this.center.x;
            double dz = victim.getZ() - this.center.z;
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > radius + 0.5 || d < 1.0 || Math.abs(victim.getY() - this.center.y) > 4.0) {
                continue;
            }
            float angle = WpMath.yawOf(dx, dz);
            boolean hit = false;
            for (int arm = 0; arm < ARMS; arm++) {
                float armAngle = WpMath.maelstromArm(this.base, arm, ARMS, t, (float) d);
                float diff = Mth.wrapDegrees((angle - armAngle) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
                if (Math.abs(diff) * d < ARM_WIDTH) {
                    hit = true;
                    break;
                }
            }
            if (hit && this.hitTimes.getOrDefault(victim.getId(), -100) + 10 <= this.tick) {
                this.hitTimes.put(victim.getId(), this.tick);
                if (this.brain.damage(victim, multiplier)) {
                    WitherBrain.push(victim, -dz / d * 0.6, 0.25, dx / d * 0.6);
                }
            }
        }
        if (this.tick % 10 == 0) {
            this.brain.playSound(this.center, SoundEvents.SOUL_ESCAPE.value(), 3.0F, 0.5F + this.wither.getRandom().nextFloat() * 0.3F);
        }
    }
}
