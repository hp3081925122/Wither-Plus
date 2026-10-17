package com.witherplus.wither.skill;

import com.witherplus.WpConfig;
import com.witherplus.network.VfxPayload;
import com.witherplus.wither.WitherBrain;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class SoulChainsSkill extends WitherSkill {
    public static final int EXTEND = 12;
    public static final int HOLD = 50;
    public static final double REACH = 24.0;
    public static final double SNAP = 30.0;
    private final List<LivingEntity> links = new ArrayList<>();

    public SoulChainsSkill(WitherBrain brain) {
        super(brain);
    }

    @Override
    protected boolean start() {
        List<LivingEntity> candidates = new ArrayList<>();
        for (LivingEntity entity : this.brain.combatTargets()) {
            if (entity.distanceTo(this.wither) <= REACH) {
                candidates.add(entity);
            }
        }
        if (candidates.isEmpty()) {
            return false;
        }
        candidates.sort(Comparator.comparingDouble(e -> e.distanceToSqr(this.wither)));
        int max = WpConfig.CHAIN_TARGETS.get();
        int[] ids = new int[Math.min(max, candidates.size())];
        for (int i = 0; i < ids.length; i++) {
            LivingEntity entity = candidates.get(i);
            this.links.add(entity);
            ids[i] = entity.getId();
            this.brain.playSound(entity.position(), SoundEvents.CHAIN_PLACE, 2.0F, 0.5F);
        }
        this.moveTarget = this.wither.position().add(0.0, 2.0, 0.0);
        this.faceTarget();
        CompoundTag data = new CompoundTag();
        data.putIntArray("targets", ids);
        data.putInt("extend", EXTEND);
        data.putInt("hold", HOLD);
        this.brain.sendVfx(VfxPayload.CHAIN, data);
        this.brain.playSound(this.wither.position(), SoundEvents.SOUL_ESCAPE, 4.0F, 0.5F);
        return true;
    }

    @Override
    protected void serverTick() {
        this.faceTarget();
        if (this.tick < EXTEND) {
            return;
        }
        Vec3 center = this.brain.bodyCenter();
        double pull = WpConfig.CHAIN_PULL.get();
        double multiplier = WpConfig.SOUL_CHAINS.damage.get();
        Iterator<LivingEntity> iterator = this.links.iterator();
        while (iterator.hasNext()) {
            LivingEntity victim = iterator.next();
            if (!victim.isAlive() || victim.isRemoved() || victim.distanceTo(this.wither) > SNAP) {
                this.sendEnd(victim, true);
                this.brain.playSound(victim.position(), SoundEvents.CHAIN_BREAK, 2.0F, 0.6F);
                iterator.remove();
                continue;
            }
            Vec3 toward = center.subtract(victim.position());
            if (toward.length() > 5.0) {
                WitherBrain.pull(victim, toward.normalize().scale(pull));
            }
            if (this.tick % 10 == 0) {
                this.brain.damage(victim, multiplier * 0.15);
            }
        }
        if (this.links.isEmpty()) {
            this.finish();
            return;
        }
        if (this.tick >= EXTEND + HOLD) {
            for (LivingEntity victim : this.links) {
                this.brain.damage(victim, multiplier);
                this.brain.knockFrom(victim, this.wither.position(), 1.0, 0.5);
                this.sendEnd(victim, false);
                this.brain.playSound(victim.position(), SoundEvents.CHAIN_BREAK, 2.5F, 0.4F);
            }
            this.brain.playSound(center, SoundEvents.SOUL_ESCAPE, 5.0F, 0.4F);
            this.links.clear();
            this.finish();
        }
    }

    private void sendEnd(LivingEntity victim, boolean snapped) {
        CompoundTag data = new CompoundTag();
        data.putInt("target", victim.getId());
        data.putBoolean("snapped", snapped);
        VfxPayload.putVec(data, "pos", victim.position().add(0.0, victim.getBbHeight() * 0.5, 0.0));
        this.brain.sendVfx(VfxPayload.CHAIN_END, data);
    }
}
