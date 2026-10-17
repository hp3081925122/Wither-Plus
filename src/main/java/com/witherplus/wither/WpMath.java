package com.witherplus.wither;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class WpMath {
    public static final float PYRE_REACH_MIN = 7.0F;
    public static final float PYRE_REACH_SWING = 6.0F;
    public static final float MAELSTROM_CURL = 0.16F;
    public static final float MAELSTROM_SPIN = 0.085F;
    public static final float MAELSTROM_INNER = 2.5F;

    private WpMath() {
    }

    public static Vec3 head(int index, double x, double y, double z, float bodyYaw) {
        if (index <= 0) {
            return new Vec3(x, y + 3.0, z);
        }
        float f = (bodyYaw + 180.0F * (index - 1)) * Mth.DEG_TO_RAD;
        return new Vec3(x + Mth.cos(f) * 1.3, y + 2.2, z + Mth.sin(f) * 1.3);
    }

    public static Vec3 dir(float angle) {
        return new Vec3(Mth.cos(angle), 0.0, Mth.sin(angle));
    }

    public static float yawOf(double dx, double dz) {
        return (float) Math.atan2(dz, dx);
    }

    public static float easeOut(float t) {
        float k = 1.0F - Mth.clamp(t, 0.0F, 1.0F);
        return 1.0F - k * k * k;
    }

    public static float easeIn(float t) {
        float k = Mth.clamp(t, 0.0F, 1.0F);
        return k * k * k;
    }

    public static Vec3 bezier(Vec3 a, Vec3 c, Vec3 b, double t) {
        double u = 1.0 - t;
        return a.scale(u * u).add(c.scale(2.0 * u * t)).add(b.scale(t * t));
    }

    public static Vec3 pyreOffset(float centerAngle, float halfArc, int head, float s) {
        float angle = centerAngle + halfArc * Mth.sin(s * Mth.TWO_PI + head * 2.0944F);
        float reach = PYRE_REACH_MIN + PYRE_REACH_SWING * (0.5F - 0.5F * Mth.cos(s * Mth.TWO_PI * 2.0F + head * 1.3F));
        return dir(angle).scale(reach);
    }

    public static float maelstromRadius(float t, int duration, float maxRadius) {
        return MAELSTROM_INNER + (maxRadius - MAELSTROM_INNER) * Mth.sin(Mth.PI * Mth.clamp(t / duration, 0.0F, 1.0F));
    }

    public static float maelstromArm(float base, int arm, int arms, float t, float distance) {
        return base + MAELSTROM_SPIN * t + arm * Mth.TWO_PI / arms + MAELSTROM_CURL * distance;
    }

    public static double segmentDistance(Vec3 point, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        double len = ab.lengthSqr();
        if (len < 1.0E-6) {
            return point.distanceTo(a);
        }
        double t = Mth.clamp(point.subtract(a).dot(ab) / len, 0.0, 1.0);
        return point.distanceTo(a.add(ab.scale(t)));
    }

    public static boolean inGap(float angle, float gapAngle, float gapHalf) {
        return Math.abs(Mth.wrapDegrees((angle - gapAngle) * Mth.RAD_TO_DEG)) * Mth.DEG_TO_RAD <= gapHalf;
    }
}
