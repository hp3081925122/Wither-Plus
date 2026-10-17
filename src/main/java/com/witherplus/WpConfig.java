package com.witherplus;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class WpConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

    public static final ModConfigSpec.BooleanValue ENHANCE_ALL;
    public static final ModConfigSpec.DoubleValue MAX_HEALTH;
    public static final ModConfigSpec.DoubleValue ATTACK_DAMAGE;
    public static final ModConfigSpec.DoubleValue KNOCKBACK_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue STAGE_TWO_HEALTH;
    public static final ModConfigSpec.DoubleValue STAGE_THREE_HEALTH;
    public static final ModConfigSpec.IntValue ACTION_INTERVAL;
    public static final ModConfigSpec.DoubleValue RAGE_COOLDOWN_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue COMBAT_RANGE;
    public static final ModConfigSpec.IntValue WITHER_SECONDS;
    public static final ModConfigSpec.IntValue WITHER_AMPLIFIER;
    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING;

    public static final Skill SOUL_PYRE;
    public static final ModConfigSpec.DoubleValue PYRE_SCORCH_MULTIPLIER;
    public static final Skill SKULL_METEOR;
    public static final ModConfigSpec.IntValue METEOR_COUNT;
    public static final ModConfigSpec.DoubleValue METEOR_RADIUS;
    public static final Skill SOUL_CHAINS;
    public static final ModConfigSpec.IntValue CHAIN_TARGETS;
    public static final ModConfigSpec.DoubleValue CHAIN_PULL;
    public static final Skill SHADE_DASH;
    public static final ModConfigSpec.IntValue DASH_COUNT;
    public static final Skill BONE_SPIRES;
    public static final ModConfigSpec.IntValue SPIRE_RAYS;
    public static final Skill BLACK_SUN;
    public static final ModConfigSpec.DoubleValue SUN_PULL;
    public static final ModConfigSpec.DoubleValue SUN_BOLT_MULTIPLIER;
    public static final Skill MAELSTROM;
    public static final ModConfigSpec.DoubleValue MAELSTROM_RADIUS;
    public static final Skill DEATH_SENTENCE;
    public static final ModConfigSpec.IntValue SENTENCE_ROUNDS;
    public static final Skill HYDRA_FURY;
    public static final ModConfigSpec.IntValue HYDRA_BOLTS;
    public static final Skill STYX_TIDE;
    public static final ModConfigSpec.IntValue TIDE_WAVES;
    public static final Skill BONE_CROWN;
    public static final ModConfigSpec.IntValue CROWN_DURATION;
    public static final ModConfigSpec.IntValue CROWN_SUMMONS;
    public static final Skill REQUIEM;
    public static final ModConfigSpec.DoubleValue REQUIEM_RADIUS;

    public static final ModConfigSpec.BooleanValue ENABLE_SCREEN_EFFECTS;
    public static final ModConfigSpec.BooleanValue ENABLE_GLOW_LAYER;
    public static final ModConfigSpec.DoubleValue CAMERA_SHAKE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("凋零基础属性").push("wither");
        ENHANCE_ALL = b.comment("是否增强所有凋零；false 时恢复原版凋零").define("enhanceAll", true);
        MAX_HEALTH = b.comment("凋零最大生命值").defineInRange("maxHealth", 600.0, 1.0, 1000000.0);
        ATTACK_DAMAGE = b.comment("凋零基础攻击力，所有技能伤害 = 基础攻击力 × 技能倍率").defineInRange("attackDamage", 12.0, 0.0, 10000.0);
        KNOCKBACK_MULTIPLIER = b.comment("击退整体倍率，实际击退还会乘以 (1 - 击退抗性)").defineInRange("knockbackMultiplier", 0.5, 0.0, 10.0);
        STAGE_TWO_HEALTH = b.comment("进入第二阶段的血量百分比").defineInRange("stageTwoHealth", 0.5, 0.0, 1.0);
        STAGE_THREE_HEALTH = b.comment("进入第三阶段的血量百分比").defineInRange("stageThreeHealth", 0.2, 0.0, 1.0);
        ACTION_INTERVAL = b.comment("两次技能之间的最短间隔（tick）").defineInRange("actionInterval", 100, 0, 10000);
        RAGE_COOLDOWN_MULTIPLIER = b.comment("第三阶段冷却倍率").defineInRange("rageCooldownMultiplier", 0.6, 0.05, 10.0);
        COMBAT_RANGE = b.comment("索敌范围").defineInRange("combatRange", 48.0, 8.0, 512.0);
        WITHER_SECONDS = b.comment("技能命中附加凋零效果的秒数，0 为不附加").defineInRange("witherSeconds", 4, 0, 600);
        WITHER_AMPLIFIER = b.comment("技能命中附加凋零效果的等级（0 为 I 级）").defineInRange("witherAmplifier", 1, 0, 9);
        DEBUG_LOGGING = b.comment("输出技能调试日志").define("debugLogging", false);
        b.pop();

        b.comment("技能配置").push("skills");
        SOUL_PYRE = new Skill(b, "soulPyre", "魂焰三重吐息", 300, 0.3, 10);
        PYRE_SCORCH_MULTIPLIER = b.comment("魂火焦痕每次伤害倍率").defineInRange("soulPyre_scorchMultiplier", 0.12, 0.0, 100.0);
        SKULL_METEOR = new Skill(b, "skullMeteor", "骷髅陨落", 340, 1.1, 10);
        METEOR_COUNT = b.comment("陨落骷髅数量").defineInRange("skullMeteor_count", 7, 1, 64);
        METEOR_RADIUS = b.comment("骷髅爆炸半径").defineInRange("skullMeteor_radius", 3.5, 0.5, 32.0);
        SOUL_CHAINS = new Skill(b, "soulChains", "冥魂锁链", 380, 1.3, 8);
        CHAIN_TARGETS = b.comment("最多锁定目标数").defineInRange("soulChains_targets", 3, 1, 16);
        CHAIN_PULL = b.comment("锁链拉扯强度").defineInRange("soulChains_pull", 0.07, 0.0, 2.0);
        SHADE_DASH = new Skill(b, "shadeDash", "暗影突袭", 260, 1.2, 9);
        DASH_COUNT = b.comment("冲刺次数").defineInRange("shadeDash_count", 3, 1, 10);
        BONE_SPIRES = new Skill(b, "boneSpires", "骨刺狱", 320, 1.0, 9);
        SPIRE_RAYS = b.comment("骨刺射线数量").defineInRange("boneSpires_rays", 6, 1, 32);
        BLACK_SUN = new Skill(b, "blackSun", "黑日", 560, 2.0, 7);
        SUN_PULL = b.comment("黑日牵引强度").defineInRange("blackSun_pull", 0.06, 0.0, 2.0);
        SUN_BOLT_MULTIPLIER = b.comment("黑日魂焰弹伤害倍率").defineInRange("blackSun_boltMultiplier", 0.5, 0.0, 100.0);
        MAELSTROM = new Skill(b, "maelstrom", "魂渊风暴", 420, 0.7, 8);
        MAELSTROM_RADIUS = b.comment("旋臂最大半径").defineInRange("maelstrom_radius", 16.0, 4.0, 64.0);
        DEATH_SENTENCE = new Skill(b, "deathSentence", "死亡宣判", 400, 1.6, 8);
        SENTENCE_ROUNDS = b.comment("宣判轮数").defineInRange("deathSentence_rounds", 3, 1, 10);
        HYDRA_FURY = new Skill(b, "hydraFury", "三首狂怒", 300, 0.6, 10);
        HYDRA_BOLTS = b.comment("骷髅弹数量").defineInRange("hydraFury_bolts", 30, 1, 200);
        STYX_TIDE = new Skill(b, "styxTide", "冥河潮汐", 380, 1.4, 9);
        TIDE_WAVES = b.comment("潮汐波数").defineInRange("styxTide_waves", 3, 1, 10);
        BONE_CROWN = new Skill(b, "boneCrown", "骸骨王冠（第二阶段转阶段）", 0, 1.0, 0);
        CROWN_DURATION = b.comment("骸骨王冠持续时间（tick）").defineInRange("boneCrown_duration", 120, 40, 2400);
        CROWN_SUMMONS = b.comment("骸骨王冠召唤凋零骷髅数量").defineInRange("boneCrown_summons", 4, 0, 32);
        REQUIEM = new Skill(b, "requiem", "终焉安魂曲（第三阶段开场）", 0, 2.4, 0);
        REQUIEM_RADIUS = b.comment("安魂曲法阵半径").defineInRange("requiem_radius", 22.0, 6.0, 64.0);
        b.pop();
        COMMON_SPEC = b.build();

        ModConfigSpec.Builder c = new ModConfigSpec.Builder();
        c.comment("客户端特效").push("vfx");
        ENABLE_SCREEN_EFFECTS = c.comment("启用全屏去饱和、闪屏与暗角").define("enableScreenEffects", true);
        ENABLE_GLOW_LAYER = c.comment("启用凋零模型发光层").define("enableGlowLayer", true);
        CAMERA_SHAKE = c.comment("镜头震动强度倍率").defineInRange("cameraShake", 1.0, 0.0, 5.0);
        c.pop();
        CLIENT_SPEC = c.build();
    }

    private WpConfig() {
    }

    public static final class Skill {
        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.IntValue cooldown;
        public final ModConfigSpec.DoubleValue damage;
        public final ModConfigSpec.IntValue weight;

        Skill(ModConfigSpec.Builder b, String key, String name, int cooldown, double damage, int weight) {
            this.enabled = b.comment("启用" + name).define(key + "_enabled", true);
            this.cooldown = b.comment(name + "冷却（tick）").defineInRange(key + "_cooldown", cooldown, 0, 100000);
            this.damage = b.comment(name + "伤害倍率").defineInRange(key + "_damageMultiplier", damage, 0.0, 1000.0);
            this.weight = b.comment(name + "选择权重").defineInRange(key + "_weight", weight, 0, 1000);
        }
    }
}
