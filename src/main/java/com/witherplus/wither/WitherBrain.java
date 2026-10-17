package com.witherplus.wither;

import com.witherplus.WitherPlus;
import com.witherplus.WpConfig;
import com.witherplus.network.VfxPayload;
import com.witherplus.network.WpNetwork;
import com.witherplus.wither.hazard.Hazard;
import com.witherplus.wither.skill.WitherSkill;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class WitherBrain {
    public static final int COLOR_STAGE_ONE = 0x5CF0FF;
    public static final int COLOR_STAGE_TWO = 0x2F8BFF;
    public static final int COLOR_STAGE_THREE = 0xFF5A1F;

    private final WitherBoss wither;
    private final int[] cooldowns = new int[SkillType.values().length];
    private final List<Hazard> hazards = new ArrayList<>();
    private final List<Hazard> pending = new ArrayList<>();
    private int globalCooldown = 80;
    private boolean stageTwoReached;
    private boolean stageThreeReached;
    @Nullable
    private WitherSkill current;
    @Nullable
    private LivingEntity target;

    public WitherBrain(WitherBoss wither) {
        this.wither = wither;
    }

    public WitherBoss wither() {
        return this.wither;
    }

    public ServerLevel level() {
        return (ServerLevel) this.wither.level();
    }

    public boolean active() {
        return WpConfig.ENHANCE_ALL.get();
    }

    public boolean busy() {
        return this.current != null;
    }

    public boolean invulnerable() {
        return this.current != null && this.current.invulnerable();
    }

    public void tick() {
        ServerLevel level = this.level();
        this.hazards.addAll(this.pending);
        this.pending.clear();
        this.hazards.removeIf(h -> !h.tick(level, this));
        if (this.wither.getInvulnerableTicks() > 0) {
            if (this.wither.tickCount % 10 == 0) {
                float extra = this.wither.getMaxHealth() / 33.0F - 10.0F;
                if (extra > 0.0F) {
                    this.wither.heal(extra);
                }
            }
            return;
        }
        for (int i = 0; i < this.cooldowns.length; i++) {
            if (this.cooldowns[i] > 0) {
                this.cooldowns[i]--;
            }
        }
        if (this.globalCooldown > 0) {
            this.globalCooldown--;
        }
        if (this.current != null) {
            this.current.tickSkill();
            if (this.current.done()) {
                this.current.end();
                this.current = null;
            }
            return;
        }
        float ratio = this.wither.getHealth() / this.wither.getMaxHealth();
        if (!this.stageTwoReached && ratio <= WpConfig.STAGE_TWO_HEALTH.get()) {
            this.stageTwoReached = true;
            WitherPlus.LOGGER.info("Wither {} entered stage two at health ratio {}", this.wither.getId(), ratio);
            this.broadcastStage();
            if (WpConfig.BONE_CROWN.enabled.get()) {
                this.startSkill(SkillType.BONE_CROWN, this.pickTarget());
                return;
            }
        }
        if (!this.stageThreeReached && ratio <= WpConfig.STAGE_THREE_HEALTH.get()) {
            this.stageThreeReached = true;
            this.stageTwoReached = true;
            WitherPlus.LOGGER.info("Wither {} entered stage three at health ratio {}", this.wither.getId(), ratio);
            this.broadcastStage();
            if (WpConfig.REQUIEM.enabled.get()) {
                this.startSkill(SkillType.REQUIEM, this.pickTarget());
                return;
            }
        }
        if (this.globalCooldown <= 0) {
            LivingEntity candidate = this.pickTarget();
            if (candidate != null) {
                this.tryStartSkill(candidate);
            }
        }
    }

    private void broadcastStage() {
        WpNetwork.sendNear(this.level(), this.wither.getX(), this.wither.getY(), this.wither.getZ(), 160.0, this.stagePayload());
    }

    public int stage() {
        if (this.stageThreeReached) {
            return 3;
        }
        if (this.stageTwoReached) {
            return 2;
        }
        return 1;
    }

    public VfxPayload stagePayload() {
        CompoundTag data = new CompoundTag();
        data.putInt("stage", this.stage());
        return new VfxPayload(VfxPayload.STAGE, this.wither.getId(), data);
    }

    public int stageColor() {
        int stage = this.stage();
        if (stage == 3) {
            return COLOR_STAGE_THREE;
        }
        if (stage == 2) {
            return COLOR_STAGE_TWO;
        }
        return COLOR_STAGE_ONE;
    }

    public boolean tryStartSkill(LivingEntity candidate) {
        int stage = this.stage();
        List<SkillType> options = new ArrayList<>();
        int total = 0;
        for (SkillType type : SkillType.values()) {
            WpConfig.Skill config = type.config();
            if (type.random && config.enabled.get() && stage >= type.minStage && this.cooldowns[type.ordinal()] <= 0 && config.weight.get() > 0) {
                options.add(type);
                total += config.weight.get();
            }
        }
        if (options.isEmpty()) {
            return false;
        }
        int roll = this.wither.getRandom().nextInt(total);
        SkillType chosen = options.get(options.size() - 1);
        for (SkillType type : options) {
            roll -= type.config().weight.get();
            if (roll < 0) {
                chosen = type;
                break;
            }
        }
        return this.startSkill(chosen, candidate);
    }

    public boolean startSkill(SkillType type, @Nullable LivingEntity candidate) {
        this.target = candidate;
        double multiplier = 1.0;
        if (this.stage() == 3) {
            multiplier = WpConfig.RAGE_COOLDOWN_MULTIPLIER.get();
        }
        WitherSkill skill = type.create(this);
        if (!skill.begin()) {
            this.globalCooldown = 20;
            if (WpConfig.DEBUG_LOGGING.get()) {
                WitherPlus.LOGGER.info("Wither {} failed to start {}", this.wither.getId(), type);
            }
            return false;
        }
        this.cooldowns[type.ordinal()] = (int) (type.config().cooldown.get() * multiplier);
        this.globalCooldown = (int) (WpConfig.ACTION_INTERVAL.get() * multiplier);
        this.current = skill;
        if (WpConfig.DEBUG_LOGGING.get()) {
            WitherPlus.LOGGER.info("Wither {} casts {} at stage {} targeting {}", this.wither.getId(), type, this.stage(), candidate);
        }
        return true;
    }

    public void abort() {
        if (this.current != null) {
            this.current.end();
            this.current = null;
        }
        this.hazards.clear();
        this.pending.clear();
    }

    @Nullable
    public LivingEntity target() {
        if (this.validTarget(this.target)) {
            return this.target;
        }
        this.target = this.pickTarget();
        return this.target;
    }

    private boolean validTarget(@Nullable LivingEntity entity) {
        return entity != null && entity.isAlive() && !entity.isRemoved() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)
                && entity.level() == this.wither.level() && entity.distanceToSqr(this.wither) < this.rangeSqr();
    }

    @Nullable
    public LivingEntity pickTarget() {
        LivingEntity aggro = this.wither.getTarget();
        if (this.validTarget(aggro)) {
            return aggro;
        }
        List<LivingEntity> targets = this.combatTargets();
        if (targets.isEmpty()) {
            return null;
        }
        return targets.get(this.wither.getRandom().nextInt(targets.size()));
    }

    public List<LivingEntity> combatTargets() {
        double range = this.rangeSqr();
        List<LivingEntity> list = new ArrayList<>(this.level().getPlayers(p -> p.isAlive() && !p.isSpectator() && !p.isCreative() && p.distanceToSqr(this.wither) < range));
        LivingEntity aggro = this.wither.getTarget();
        if (this.validTarget(aggro) && !list.contains(aggro)) {
            list.add(aggro);
        }
        return list;
    }

    private double rangeSqr() {
        double range = WpConfig.COMBAT_RANGE.get();
        return range * range;
    }

    public double attack() {
        return this.wither.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    public boolean damage(LivingEntity victim, double multiplier) {
        boolean hit = victim.hurtServer(this.level(), this.wither.damageSources().mobAttack(this.wither), (float) (this.attack() * multiplier));
        int seconds = WpConfig.WITHER_SECONDS.get();
        if (hit && seconds > 0) {
            victim.addEffect(new MobEffectInstance(MobEffects.WITHER, seconds * 20, WpConfig.WITHER_AMPLIFIER.get()), this.wither);
        }
        return hit;
    }

    public static void push(Entity entity, double x, double y, double z) {
        double scale = WpConfig.KNOCKBACK_MULTIPLIER.get();
        if (entity instanceof LivingEntity living) {
            scale *= Math.max(0.0, 1.0 - living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        }
        if (scale <= 0.0) {
            return;
        }
        entity.push(x * scale, y * scale, z * scale);
        entity.hurtMarked = true;
    }

    public static void pull(LivingEntity entity, Vec3 velocity) {
        double scale = Math.max(0.0, 1.0 - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        if (scale <= 0.0) {
            return;
        }
        entity.push(velocity.x * scale, velocity.y * scale, velocity.z * scale);
        entity.fallDistance = 0.0F;
        entity.hurtMarked = true;
    }

    public void knockFrom(LivingEntity victim, Vec3 from, double horizontal, double vertical) {
        Vec3 d = new Vec3(victim.getX() - from.x, 0.0, victim.getZ() - from.z);
        if (d.lengthSqr() < 1.0E-4) {
            float a = this.wither.getRandom().nextFloat() * Mth.TWO_PI;
            d = new Vec3(Mth.cos(a), 0.0, Mth.sin(a));
        }
        d = d.normalize();
        push(victim, d.x * horizontal, vertical, d.z * horizontal);
    }

    public List<LivingEntity> victims(AABB box) {
        return this.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != this.wither && e.isAlive() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e) && !(e instanceof WitherBoss) && this.hostileTo(e));
    }

    private boolean hostileTo(LivingEntity entity) {
        if (entity instanceof Player) {
            return true;
        }
        return entity == this.wither.getTarget() || !entity.getType().builtInRegistryHolder().is(net.minecraft.tags.EntityTypeTags.UNDEAD);
    }

    public double groundY(double x, double z, double fallback) {
        int y = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z));
        if (y <= this.level().getMinY() + 1 || y > fallback + 24.0) {
            return fallback;
        }
        return y;
    }

    public Vec3 head(int index) {
        return WpMath.head(index, this.wither.getX(), this.wither.getY(), this.wither.getZ(), this.wither.yBodyRot);
    }

    public Vec3 bodyCenter() {
        return this.wither.position().add(0.0, 2.0, 0.0);
    }

    public void sendVfx(int type, CompoundTag data) {
        WpNetwork.sendNear(this.level(), this.wither.getX(), this.wither.getY(), this.wither.getZ(), 160.0, new VfxPayload(type, this.wither.getId(), data));
    }

    public void playSound(Vec3 pos, SoundEvent sound, float volume, float pitch) {
        this.level().playSound(null, pos.x, pos.y, pos.z, sound, SoundSource.HOSTILE, volume, pitch);
    }

    public void addHazard(Hazard hazard) {
        this.pending.add(hazard);
    }

    public void save(net.minecraft.world.level.storage.ValueOutput tag) {
        tag.putBoolean("WpStageTwo", this.stageTwoReached);
        tag.putBoolean("WpStageThree", this.stageThreeReached);
    }

    public void load(net.minecraft.world.level.storage.ValueInput tag) {
        this.stageTwoReached = tag.getBooleanOr("WpStageTwo", false);
        this.stageThreeReached = tag.getBooleanOr("WpStageThree", false);
    }
}
