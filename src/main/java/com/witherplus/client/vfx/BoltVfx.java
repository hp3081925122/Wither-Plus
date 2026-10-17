package com.witherplus.client.vfx;

import com.witherplus.client.Draw;
import com.witherplus.client.VfxInstance;
import com.witherplus.client.VfxManager;
import com.witherplus.wither.WpMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BoltVfx extends VfxInstance {
    private static final int TRAIL = 8;
    private final Vec3 start;
    private final Vec3 control;
    private final Vec3 end;
    private final int travel;
    private final int style;
    private final int rgb;
    private float stopAt = 1.0F;

    public BoltVfx(Vec3 start, Vec3 control, Vec3 end, int travel, int style, int rgb) {
        super(Math.max(1, travel) + 1);
        this.start = start;
        this.control = control;
        this.end = end;
        this.travel = Math.max(1, travel);
        this.style = style;
        this.rgb = rgb;
    }

    private Vec3 at(float k) {
        return WpMath.bezier(this.start, this.control, this.end, Math.min(k, this.stopAt));
    }

    @Override
    protected void onTick() {
        float k = Math.min(1.0F, this.age / (float) this.travel);
        Vec3 pos = this.at(k);
        boolean impact = this.age >= this.travel;
        ClientLevel level = Minecraft.getInstance().level;
        if (!impact && level != null && this.age > 2) {
            BlockPos block = BlockPos.containing(pos);
            impact = !level.getBlockState(block).getCollisionShape(level, block).isEmpty();
            if (!impact) {
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(1.5), e -> !(e instanceof WitherBoss) && e.isAlive())) {
                    if (entity.getBoundingBox().inflate(0.7).contains(pos)) {
                        impact = true;
                        break;
                    }
                }
            }
        }
        if (impact) {
            this.stopAt = k;
            float radius = 1.8F;
            if (this.style == 1) {
                radius = 2.2F;
            }
            VfxManager.add(new BurstVfx(pos, radius, this.rgb, false));
            this.lifetime = this.age;
        }
    }

    @Override
    public void render(float partialTick) {
        float k = Math.min(this.stopAt, this.time(partialTick) / this.travel);
        Vec3 pos = this.at(k);
        Vec3[] points = new Vec3[TRAIL];
        float[] widths = new float[TRAIL];
        float[] alphas = new float[TRAIL];
        for (int j = 0; j < TRAIL; j++) {
            float f = 1.0F - j / (float) (TRAIL - 1);
            points[j] = this.at(Math.max(0.0F, k - j * 0.035F));
            widths[j] = 0.15F + 0.85F * f;
            alphas[j] = 0.9F * f;
        }
        int hot = Draw.mix(this.rgb, 0xFFFFFF, 0.55F);
        if (Draw.begin(Draw.Material.FLAME)) {
            Draw.ribbon(points, widths, alphas, this.rgb, 0.0F, 1.5F);
            Draw.end(Draw.FLAME_STRIP, 2.6F, 1.0F, 0.6F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            if (this.style == 1) {
                Draw.billboard(pos, 1.8F, this.rgb, 0.9F);
                Draw.billboard(pos, 0.8F, hot, 1.0F);
            } else {
                Draw.billboard(pos, 2.0F, this.rgb, 0.5F);
            }
            Draw.end(Draw.FLAME_BALL, 1.4F, 2.0F, 1.0F);
        }
        if (this.style == 0 && Draw.begin(Draw.Material.FLAME)) {
            Draw.billboard(pos, 1.3F, this.rgb, 1.0F);
            Draw.end(Draw.FLAME_SKULL, 1.0F, 1.0F, 0.0F);
        }
    }
}
