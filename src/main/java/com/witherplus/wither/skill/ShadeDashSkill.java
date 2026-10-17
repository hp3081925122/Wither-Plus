package com.witherplus.wither.skill;

import com.witherplus.WpConfig;
import com.witherplus.network.VfxPayload;
import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.WpMath;
import com.witherplus.wither.hazard.BlastHazard;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ShadeDashSkill extends WitherSkill {
    public static final int TELEGRAPH = 12;
    public static final int DASH = 6;
    public static final int GAP = 4;
    private static final double BODY = 1.75;
    private final IntOpenHashSet hit = new IntOpenHashSet();
    private Vec3 from = Vec3.ZERO;
    private Vec3 to = Vec3.ZERO;
    private int dash;
    private int phaseStart;

    public ShadeDashSkill(WitherBrain brain) {
        super(brain);
    }

    @Override
    protected boolean start() {
        if (this.brain.target() == null) {
            return false;
        }
        this.invulnerable = true;
        this.turnRate = 40.0F;
        this.wither.setInvisible(true);
        this.sendShade(true);
        this.brain.playSound(this.wither.position(), SoundEvents.ENDERMAN_TELEPORT, 3.0F, 0.5F);
        return this.planDash();
    }

    private boolean planDash() {
        LivingEntity target = this.brain.target();
        if (target == null) {
            return false;
        }
        this.from = this.wither.position();
        Vec3 through = target.position().add(0.0, target.getBbHeight() * 0.5 - BODY, 0.0);
        Vec3 flat = new Vec3(through.x - this.from.x, 0.0, through.z - this.from.z);
        if (flat.lengthSqr() < 1.0) {
            flat = WpMath.dir(this.wither.getRandom().nextFloat() * 6.2831855F);
        }
        Vec3 end = through.add(flat.normalize().scale(8.0));
        double floor = this.brain.groundY(end.x, end.z, end.y) + 0.5;
        if (end.y < floor) {
            end = new Vec3(end.x, floor, end.z);
        }
        this.to = end;
        this.phaseStart = this.tick;
        this.hit.clear();
        this.faceDirection = this.to.subtract(this.from);
        CompoundTag data = new CompoundTag();
        VfxPayload.putVec(data, "from", this.from.add(0.0, BODY, 0.0));
        VfxPayload.putVec(data, "to", this.to.add(0.0, BODY, 0.0));
        data.putInt("telegraph", TELEGRAPH);
        data.putInt("duration", DASH);
        this.brain.sendVfx(VfxPayload.DASH, data);
        this.brain.playSound(this.from, SoundEvents.PHANTOM_SWOOP, 3.0F, 0.6F);
        return true;
    }

    @Override
    protected void serverTick() {
        int local = this.tick - this.phaseStart;
        if (local < TELEGRAPH) {
            return;
        }
        if (local < TELEGRAPH + DASH) {
            float k = (local - TELEGRAPH + 1) / (float) DASH;
            Vec3 prev = this.wither.position();
            Vec3 next = this.from.lerp(this.to, k);
            this.wither.setPos(next.x, next.y, next.z);
            Vec3 a = prev.add(0.0, BODY, 0.0);
            Vec3 b = next.add(0.0, BODY, 0.0);
            double multiplier = WpConfig.SHADE_DASH.damage.get();
            for (LivingEntity victim : this.brain.victims(new AABB(a, b).inflate(3.0))) {
                Vec3 center = victim.position().add(0.0, victim.getBbHeight() * 0.5, 0.0);
                if (WpMath.segmentDistance(center, a, b) < 2.0 + victim.getBbWidth() * 0.5 && this.hit.add(victim.getId())) {
                    this.brain.damage(victim, multiplier);
                    this.brain.knockFrom(victim, prev, 0.8, 0.35);
                }
            }
            if (local == TELEGRAPH) {
                this.brain.playSound(next, SoundEvents.WITHER_SHOOT, 2.0F, 0.4F);
            }
            return;
        }
        if (local >= TELEGRAPH + DASH + GAP) {
            this.dash++;
            if (this.dash >= WpConfig.DASH_COUNT.get() || !this.planDash()) {
                this.reveal();
                this.finish();
            }
        }
    }

    private void reveal() {
        this.wither.setInvisible(false);
        this.invulnerable = false;
        this.sendShade(false);
        Vec3 center = this.brain.bodyCenter();
        this.brain.addHazard(new BlastHazard(center, 1, 4.0, WpConfig.SHADE_DASH.damage.get() * 0.5, 0.7, 0.3, SoundEvents.GENERIC_EXPLODE, 1.5F, 1.1F));
        CompoundTag data = new CompoundTag();
        VfxPayload.putVec(data, "pos", center);
        data.putFloat("radius", 4.0F);
        this.brain.sendVfx(VfxPayload.BURST, data);
    }

    private void sendShade(boolean on) {
        CompoundTag data = new CompoundTag();
        data.putBoolean("on", on);
        VfxPayload.putVec(data, "pos", this.brain.bodyCenter());
        this.brain.sendVfx(VfxPayload.SHADE, data);
    }

    @Override
    public void end() {
        if (this.wither.isInvisible()) {
            this.wither.setInvisible(false);
            this.sendShade(false);
        }
    }
}
