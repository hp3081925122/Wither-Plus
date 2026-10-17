package com.witherplus.wither.hazard;

import com.witherplus.wither.WitherBrain;
import net.minecraft.server.level.ServerLevel;

public abstract class Hazard {
    protected int age;

    public abstract boolean tick(ServerLevel level, WitherBrain brain);
}
