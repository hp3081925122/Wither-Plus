package com.witherplus.wither.skill;

import com.witherplus.WpConfig;
import com.witherplus.network.VfxPayload;
import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.hazard.PillarHazard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class DeathSentenceSkill extends WitherSkill {
    public static final int ROUND = 50;
    public static final int LOCK = 35;
    public static final int STRIKE = 15;
    public static final double RADIUS = 2.2;
    public static final double HEIGHT = 14.0;
    private final List<LivingEntity> marked = new ArrayList<>();
    private int rounds;

    public DeathSentenceSkill(WitherBrain brain) {
        super(brain);
    }

    @Override
    protected boolean start() {
        if (this.brain.combatTargets().isEmpty()) {
            return false;
        }
        this.rounds = WpConfig.SENTENCE_ROUNDS.get();
        this.moveTarget = this.wither.position().add(0.0, 3.0, 0.0);
        this.brain.playSound(this.wither.position(), SoundEvents.ELDER_GUARDIAN_CURSE, 3.0F, 0.6F);
        return true;
    }

    @Override
    protected void serverTick() {
        this.faceTarget();
        int round = (this.tick - 1) / ROUND;
        int local = (this.tick - 1) % ROUND;
        if (round >= this.rounds) {
            if (this.tick >= this.rounds * ROUND + STRIKE + 5) {
                this.finish();
            }
            return;
        }
        if (local == 0) {
            this.marked.clear();
            this.marked.addAll(this.brain.combatTargets());
            int[] ids = new int[this.marked.size()];
            for (int i = 0; i < ids.length; i++) {
                ids[i] = this.marked.get(i).getId();
            }
            CompoundTag data = new CompoundTag();
            data.putIntArray("targets", ids);
            data.putInt("lock", LOCK);
            this.brain.sendVfx(VfxPayload.MARK, data);
            this.brain.playSound(this.wither.position(), SoundEvents.BELL_RESONATE, 3.0F, 0.5F);
        }
        if (local == LOCK) {
            List<Vec3> points = new ArrayList<>();
            double multiplier = WpConfig.DEATH_SENTENCE.damage.get();
            for (LivingEntity victim : this.marked) {
                if (!victim.isAlive() || victim.isRemoved()) {
                    continue;
                }
                Vec3 point = this.groundAt(victim);
                points.add(point);
                this.brain.addHazard(new PillarHazard(point, STRIKE, RADIUS, HEIGHT, multiplier));
                this.brain.playSound(point, SoundEvents.BEACON_ACTIVATE, 2.0F, 1.6F);
            }
            if (!points.isEmpty()) {
                CompoundTag data = new CompoundTag();
                VfxPayload.putPoints(data, "points", points);
                data.putInt("delay", STRIKE);
                data.putInt("style", 0);
                data.putFloat("radius", (float) RADIUS);
                this.brain.sendVfx(VfxPayload.PILLAR, data);
            }
        }
    }
}
