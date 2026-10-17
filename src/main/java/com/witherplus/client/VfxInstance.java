package com.witherplus.client;

import com.witherplus.wither.WpMath;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.phys.Vec3;

public abstract class VfxInstance {
    protected int age;
    protected int lifetime;

    protected VfxInstance(int lifetime) {
        this.lifetime = lifetime;
    }

    public boolean tick() {
        this.age++;
        this.onTick();
        return this.age < this.lifetime;
    }

    protected void onTick() {
    }

    public abstract void render(float partialTick);

    public boolean disposable() {
        return true;
    }

    protected float time(float partialTick) {
        return this.age + partialTick;
    }

    protected float fade(float partialTick, float in, float out) {
        float t = this.time(partialTick);
        float a = 1.0F;
        if (in > 0.0F) {
            a = Math.min(a, t / in);
        }
        if (out > 0.0F) {
            a = Math.min(a, (this.lifetime - t) / out);
        }
        return Mth.clamp(a, 0.0F, 1.0F);
    }

    @Nullable
    protected static Entity entity(int id) {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        return Minecraft.getInstance().level.getEntity(id);
    }

    @Nullable
    protected static WitherBoss wither(int id) {
        if (entity(id) instanceof WitherBoss wither) {
            return wither;
        }
        return null;
    }

    protected static Vec3 lerpPos(Entity entity, float partialTick) {
        return new Vec3(Mth.lerp(partialTick, entity.xo, entity.getX()), Mth.lerp(partialTick, entity.yo, entity.getY()), Mth.lerp(partialTick, entity.zo, entity.getZ()));
    }

    protected static Vec3 center(Entity entity, float partialTick) {
        return lerpPos(entity, partialTick).add(0.0, entity.getBbHeight() * 0.5, 0.0);
    }

    protected static Vec3 head(WitherBoss wither, int index, float partialTick) {
        Vec3 pos = lerpPos(wither, partialTick);
        float yaw = Mth.rotLerp(partialTick, wither.yBodyRotO, wither.yBodyRot);
        return WpMath.head(index, pos.x, pos.y, pos.z, yaw);
    }

    public static double distanceToPlayer(Vec3 pos) {
        if (Minecraft.getInstance().player == null) {
            return Double.MAX_VALUE;
        }
        return Minecraft.getInstance().player.position().distanceTo(pos);
    }

    protected static float hash(int seed) {
        int h = seed * 0x27D4EB2D;
        h ^= h >>> 15;
        h *= 0x2C1B3C6D;
        h ^= h >>> 12;
        return (h & 0xFFFFFF) / (float) 0x1000000;
    }
}
