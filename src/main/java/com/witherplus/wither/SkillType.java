package com.witherplus.wither;

import com.witherplus.WpConfig;
import com.witherplus.wither.skill.BlackSunSkill;
import com.witherplus.wither.skill.BoneCrownSkill;
import com.witherplus.wither.skill.BoneSpiresSkill;
import com.witherplus.wither.skill.DeathSentenceSkill;
import com.witherplus.wither.skill.HydraFurySkill;
import com.witherplus.wither.skill.MaelstromSkill;
import com.witherplus.wither.skill.RequiemSkill;
import com.witherplus.wither.skill.ShadeDashSkill;
import com.witherplus.wither.skill.SkullMeteorSkill;
import com.witherplus.wither.skill.SoulChainsSkill;
import com.witherplus.wither.skill.SoulPyreSkill;
import com.witherplus.wither.skill.StyxTideSkill;
import com.witherplus.wither.skill.WitherSkill;

public enum SkillType {
    SOUL_PYRE(1, true),
    SKULL_METEOR(1, true),
    SOUL_CHAINS(1, true),
    SHADE_DASH(1, true),
    BONE_SPIRES(2, true),
    BLACK_SUN(2, true),
    MAELSTROM(2, true),
    DEATH_SENTENCE(2, true),
    HYDRA_FURY(3, true),
    STYX_TIDE(3, true),
    BONE_CROWN(2, false),
    REQUIEM(3, false);

    public final int minStage;
    public final boolean random;

    SkillType(int minStage, boolean random) {
        this.minStage = minStage;
        this.random = random;
    }

    public WpConfig.Skill config() {
        return switch (this) {
            case SOUL_PYRE -> WpConfig.SOUL_PYRE;
            case SKULL_METEOR -> WpConfig.SKULL_METEOR;
            case SOUL_CHAINS -> WpConfig.SOUL_CHAINS;
            case SHADE_DASH -> WpConfig.SHADE_DASH;
            case BONE_SPIRES -> WpConfig.BONE_SPIRES;
            case BLACK_SUN -> WpConfig.BLACK_SUN;
            case MAELSTROM -> WpConfig.MAELSTROM;
            case DEATH_SENTENCE -> WpConfig.DEATH_SENTENCE;
            case HYDRA_FURY -> WpConfig.HYDRA_FURY;
            case STYX_TIDE -> WpConfig.STYX_TIDE;
            case BONE_CROWN -> WpConfig.BONE_CROWN;
            case REQUIEM -> WpConfig.REQUIEM;
        };
    }

    public WitherSkill create(WitherBrain brain) {
        return switch (this) {
            case SOUL_PYRE -> new SoulPyreSkill(brain);
            case SKULL_METEOR -> new SkullMeteorSkill(brain);
            case SOUL_CHAINS -> new SoulChainsSkill(brain);
            case SHADE_DASH -> new ShadeDashSkill(brain);
            case BONE_SPIRES -> new BoneSpiresSkill(brain);
            case BLACK_SUN -> new BlackSunSkill(brain);
            case MAELSTROM -> new MaelstromSkill(brain);
            case DEATH_SENTENCE -> new DeathSentenceSkill(brain);
            case HYDRA_FURY -> new HydraFurySkill(brain);
            case STYX_TIDE -> new StyxTideSkill(brain);
            case BONE_CROWN -> new BoneCrownSkill(brain);
            case REQUIEM -> new RequiemSkill(brain);
        };
    }
}
