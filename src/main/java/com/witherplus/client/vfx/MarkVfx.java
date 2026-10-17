package com.witherplus.client.vfx;

import com.witherplus.client.Draw;
import com.witherplus.client.VfxInstance;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class MarkVfx extends VfxInstance {
    private final int[] targets;
    private final int lock;
    private final int rgb;

    public MarkVfx(int[] targets, int lock, int rgb) {
        super(lock + 6);
        this.targets = targets;
        this.lock = Math.max(1, lock);
        this.rgb = rgb;
    }

    @Override
    public void render(float partialTick) {
        float t = this.time(partialTick);
        float k = Mth.clamp(t / this.lock, 0.0F, 1.0F);
        float alpha = this.fade(partialTick, 4.0F, 6.0F);
        List<Entity> entities = new ArrayList<>();
        for (int id : this.targets) {
            Entity entity = entity(id);
            if (entity != null && entity.isAlive()) {
                entities.add(entity);
            }
        }
        if (entities.isEmpty()) {
            return;
        }
        float pulse = 0.75F + 0.25F * Mth.sin(t * (0.3F + 0.9F * k));
        if (Draw.begin(Draw.Material.GROUND)) {
            for (Entity entity : entities) {
                Draw.ground(lerpPos(entity, partialTick), 2.2F, this.rgb, alpha);
            }
            Draw.end(Draw.GROUND_WARNING, k, 0.9F, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (Entity entity : entities) {
                Vec3 feet = lerpPos(entity, partialTick);
                Draw.beam(feet.add(0.0, 16.0, 0.0), feet, 0.1F + 0.3F * k, this.rgb, alpha * k * pulse, 6.0F);
            }
            Draw.end(Draw.FLAME_STRIP, 3.0F, 1.0F, 0.6F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (Entity entity : entities) {
                Vec3 above = lerpPos(entity, partialTick).add(0.0, entity.getBbHeight() + 0.9 + Mth.sin(t * 0.2F) * 0.1, 0.0);
                Draw.billboard(above, (1.0F + 0.4F * k) * pulse, this.rgb, alpha);
            }
            Draw.end(Draw.FLAME_SKULL, 1.0F, 1.0F, 0.0F);
        }
    }
}
