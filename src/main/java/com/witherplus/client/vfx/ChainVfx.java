package com.witherplus.client.vfx;

import com.witherplus.client.Draw;
import com.witherplus.client.VfxInstance;
import com.witherplus.wither.WpMath;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.phys.Vec3;

public final class ChainVfx extends VfxInstance {
    private static final List<ChainVfx> ACTIVE = new ArrayList<>();
    private static final int SAMPLES = 18;
    private final int witherId;
    private final int[] targets;
    private final IntOpenHashSet released = new IntOpenHashSet();
    private final int extend;
    private final int rgb;

    public ChainVfx(int witherId, int[] targets, int extend, int hold, int rgb) {
        super(extend + hold + 6);
        this.witherId = witherId;
        this.targets = targets;
        this.extend = Math.max(1, extend);
        this.rgb = rgb;
        ACTIVE.add(this);
    }

    public static void release(int witherId, int targetId) {
        for (ChainVfx chain : ACTIVE) {
            if (chain.witherId == witherId) {
                chain.released.add(targetId);
            }
        }
    }

    public static void clearAll() {
        ACTIVE.clear();
    }

    @Override
    public boolean tick() {
        boolean alive = super.tick();
        if (!alive) {
            ACTIVE.remove(this);
        }
        return alive;
    }

    @Override
    public void render(float partialTick) {
        WitherBoss wither = wither(this.witherId);
        if (wither == null) {
            return;
        }
        float t = this.time(partialTick);
        float ext = WpMath.easeOut(t / this.extend);
        float alpha = this.fade(partialTick, 2.0F, 4.0F);
        List<Vec3[]> chains = new ArrayList<>();
        List<Vec3> feet = new ArrayList<>();
        List<Float> lengths = new ArrayList<>();
        for (int i = 0; i < this.targets.length; i++) {
            if (this.released.contains(this.targets[i])) {
                continue;
            }
            Entity target = entity(this.targets[i]);
            if (target == null) {
                continue;
            }
            Vec3 from = head(wither, i % 3, partialTick);
            Vec3 to = center(target, partialTick);
            Vec3 end = from.lerp(to, ext);
            double length = from.distanceTo(end);
            Vec3 axis = end.subtract(from);
            Vec3 side = Vec3.ZERO;
            if (axis.lengthSqr() > 1.0E-4) {
                side = Draw.basis(axis)[0];
            }
            Vec3[] points = new Vec3[SAMPLES];
            for (int j = 0; j < SAMPLES; j++) {
                float s = j / (float) (SAMPLES - 1);
                double sag = Math.sin(Math.PI * s) * Math.min(2.0, length * 0.08) * (1.0 - 0.6 * Mth.clamp((t - this.extend) / 20.0F, 0.0F, 1.0F));
                double wobble = Math.sin(s * 9.0 + t * 0.6 + i) * 0.15 * Math.sin(Math.PI * s);
                points[j] = from.lerp(end, s).add(0.0, -sag, 0.0).add(side.scale(wobble));
            }
            chains.add(points);
            lengths.add((float) length);
            feet.add(lerpPos(target, partialTick));
        }
        if (chains.isEmpty()) {
            return;
        }
        float[] widths = new float[SAMPLES];
        float[] glowWidths = new float[SAMPLES];
        float[] alphas = new float[SAMPLES];
        for (int j = 0; j < SAMPLES; j++) {
            widths[j] = 0.5F;
            glowWidths[j] = 1.4F;
            alphas[j] = alpha;
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            for (int i = 0; i < chains.size(); i++) {
                Draw.ribbon(chains.get(i), widths, alphas, this.rgb, 0.0F, lengths.get(i) * 1.6F);
            }
            Draw.end(Draw.FLAME_CHAIN, 0.6F, 1.0F, 0.0F);
        }
        if (Draw.begin(Draw.Material.FLAME)) {
            float[] glowAlphas = new float[SAMPLES];
            for (int j = 0; j < SAMPLES; j++) {
                glowAlphas[j] = alpha * 0.35F;
            }
            for (Vec3[] points : chains) {
                Draw.ribbon(points, glowWidths, glowAlphas, this.rgb, 0.0F, 3.0F);
            }
            Draw.end(Draw.FLAME_STRIP, 1.5F, 1.0F, 0.0F);
        }
        if (t >= this.extend && Draw.begin(Draw.Material.GROUND)) {
            for (Vec3 foot : feet) {
                Draw.ground(foot, 1.4F, this.rgb, alpha);
            }
            Draw.end(Draw.GROUND_SIGIL, 1.0F, 0.5F, 0.0F);
        }
    }
}
