package com.witherplus.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class Draw {
    public enum Material {
        FLAME(true),
        SMOKE(false),
        BONE(false),
        GROUND(true),
        GROUND_ALPHA(false),
        SPHERE(true),
        SPHERE_ALPHA(false),
        BLOCK(false);

        final boolean additive;

        Material(boolean additive) {
            this.additive = additive;
        }
    }

    public static final int FLAME_STRIP = 0;
    public static final int FLAME_BALL = 1;
    public static final int FLAME_SMOKE = 2;
    public static final int FLAME_SKULL = 3;
    public static final int FLAME_COLUMN = 4;
    public static final int FLAME_CHAIN = 5;
    public static final int FLAME_BONE = 6;
    public static final int FLAME_TONGUE = 7;

    public static final int GROUND_ASH = 0;
    public static final int GROUND_SOULFIRE = 1;
    public static final int GROUND_SIGIL = 2;
    public static final int GROUND_WARNING = 3;
    public static final int GROUND_CHECKER = 4;

    public static final int SPHERE_BLACK_SUN = 0;
    public static final int SPHERE_SHELL = 1;

    public static final int SOUL = 0x5CF0FF;
    public static final int ABYSS = 0x2F8BFF;
    public static final int HELL = 0xFF5A1F;
    public static final int ASH = 0x15161C;
    public static final int BONE_COLOR = 0xE8E0CC;
    public static final Identifier BONE_SPIKE_BASE = Identifier.fromNamespaceAndPath("witherplus", "textures/vfx/bone_spike_base.png");
    public static final Identifier BONE_SPIKE_MIDDLE = Identifier.fromNamespaceAndPath("witherplus", "textures/vfx/bone_spike_middle.png");
    public static final Identifier BONE_SPIKE_TIP = Identifier.fromNamespaceAndPath("witherplus", "textures/vfx/bone_spike_tip.png");

    private static BufferBuilder buffer;
    private static Material current;
    private static boolean hasNormal;
    private static float nx;
    private static float ny = 1.0F;
    private static float nz;
    private static double cx;
    private static double cy;
    private static double cz;
    private static Vec3 camPos = Vec3.ZERO;
    private static Vec3 camRight = new Vec3(1.0, 0.0, 0.0);
    private static Vec3 camUp = new Vec3(0.0, 1.0, 0.0);
    private static Identifier blockTexture = BONE_SPIKE_TIP;

    private Draw() {
    }

    public static void setup(net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        camPos = camera.pos;
        cx = camPos.x;
        cy = camPos.y;
        cz = camPos.z;
        Vector3f left = new Vector3f(-1, 0, 0).rotate(camera.orientation);
        Vector3f up = new Vector3f(0, 1, 0).rotate(camera.orientation);
        camRight = new Vec3(-left.x(), -left.y(), -left.z());
        camUp = new Vec3(up.x(), up.y(), up.z());
    }

    public static Vec3 cameraPos() {
        return camPos;
    }

    public static Vec3 cameraRight() {
        return camRight;
    }

    public static Vec3 cameraUp() {
        return camUp;
    }

    public static boolean begin(Material material, Identifier texture) {
        blockTexture = texture;
        return begin(material);
    }

    public static boolean begin(Material material) {
        current = material;
        hasNormal = material == Material.SPHERE || material == Material.SPHERE_ALPHA;
        VertexFormat format = DefaultVertexFormat.POSITION_TEX_COLOR;
        if (hasNormal) {
            format = DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL;
        }
        buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, format);
        return true;
    }

    public static void end(float a, float b, float c, float d) {
        com.mojang.blaze3d.vertex.MeshData mesh = buffer.build();
        buffer = null;
        if (mesh == null) {
            return;
        }
        WpGpu.draw(mesh, current.ordinal(), new org.joml.Vector4f(a, b, c, d), new org.joml.Vector4f(1), new org.joml.Vector4f(1), blockTexture);
    }

    public static void normal(double x, double y, double z) {
        nx = (float) x;
        ny = (float) y;
        nz = (float) z;
    }

    public static void vertex(double x, double y, double z, float u, float v, int rgb, float alpha) {
        float r = ((rgb >> 16) & 255) / 255.0F;
        float g = ((rgb >> 8) & 255) / 255.0F;
        float b = (rgb & 255) / 255.0F;
        VertexConsumer c = buffer.addVertex((float) (x - cx), (float) (y - cy), (float) (z - cz)).setUv(u, v).setColor(r, g, b, Mth.clamp(alpha, 0.0F, 1.0F));
        if (hasNormal) {
            c.setNormal(nx, ny, nz);
        }
    }

    public static void vertex(Vec3 p, float u, float v, int rgb, float alpha) {
        vertex(p.x, p.y, p.z, u, v, rgb, alpha);
    }

    public static void quad(Vec3 a, Vec3 b, Vec3 c, Vec3 d, int rgb, float alpha) {
        vertex(a, 0.0F, 0.0F, rgb, alpha);
        vertex(b, 0.0F, 1.0F, rgb, alpha);
        vertex(c, 1.0F, 1.0F, rgb, alpha);
        vertex(d, 1.0F, 0.0F, rgb, alpha);
    }

    public static void plane(Vec3 center, Vec3 right, Vec3 up, int rgb, float alpha) {
        Vec3 n = right.cross(up);
        if (n.lengthSqr() > 1.0E-8) {
            n = n.normalize();
        }
        normal(n.x, n.y, n.z);
        quad(center.subtract(right).subtract(up), center.subtract(right).add(up), center.add(right).add(up), center.add(right).subtract(up), rgb, alpha);
    }

    public static void billboard(Vec3 center, float size, int rgb, float alpha) {
        float h = size * 0.5F;
        plane(center, camRight.scale(h), camUp.scale(h), rgb, alpha);
    }

    public static void billboard(Vec3 center, float size, float roll, int rgb, float alpha) {
        float h = size * 0.5F;
        float c = Mth.cos(roll);
        float s = Mth.sin(roll);
        Vec3 right = camRight.scale(c).add(camUp.scale(s)).scale(h);
        Vec3 up = camUp.scale(c).subtract(camRight.scale(s)).scale(h);
        plane(center, right, up, rgb, alpha);
    }

    public static void uprightBillboard(Vec3 base, float width, float height, int rgb, float alpha) {
        uprightBillboard(base, width, height, rgb, alpha, 0);
    }

    public static void uprightBillboard(Vec3 base, float width, float height, int rgb, float alpha, int seed) {
        Vec3 toCam = camPos.subtract(base);
        Vec3 side = new Vec3(toCam.z, 0.0, -toCam.x);
        if (side.lengthSqr() < 1.0E-6) {
            side = camRight;
        }
        side = side.normalize().scale(width * 0.5);
        Vec3 top = base.add(0.0, height, 0.0);
        normal(0.0, 1.0, 0.0);
        float v0 = Math.floorMod(seed, 64);
        vertex(base.subtract(side), 0.0F, v0, rgb, alpha);
        vertex(top.subtract(side), 1.0F, v0, rgb, alpha);
        vertex(top.add(side), 1.0F, v0 + 1.0F, rgb, alpha);
        vertex(base.add(side), 0.0F, v0 + 1.0F, rgb, alpha);
    }

    public static void ground(Vec3 center, float half, int rgb, float alpha) {
        double y = center.y + 0.04;
        normal(0.0, 1.0, 0.0);
        vertex(center.x - half, y, center.z - half, 0.0F, 0.0F, rgb, alpha);
        vertex(center.x - half, y, center.z + half, 0.0F, 1.0F, rgb, alpha);
        vertex(center.x + half, y, center.z + half, 1.0F, 1.0F, rgb, alpha);
        vertex(center.x + half, y, center.z - half, 1.0F, 0.0F, rgb, alpha);
    }

    public static double groundY(double x, double z, double fallback) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return fallback;
        }
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
        if (y <= level.getMinY() + 1 || y > fallback + 24.0) {
            return fallback;
        }
        return y;
    }

    public static void terrain(Vec3 center, float half, int cells, float lift, int rgb, float alpha) {
        int n = cells + 1;
        double[] heights = new double[n * n];
        double step = half * 2.0 / cells;
        double x0 = center.x - half;
        double z0 = center.z - half;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                heights[i * n + j] = groundY(x0 + i * step, z0 + j * step, center.y) + lift;
            }
        }
        double floor = center.y - 16.0;
        double ceiling = center.y + 16.0;
        normal(0.0, 1.0, 0.0);
        for (int i = 0; i < cells; i++) {
            for (int j = 0; j < cells; j++) {
                double h00 = heights[i * n + j];
                double h01 = heights[i * n + j + 1];
                double h11 = heights[(i + 1) * n + j + 1];
                double h10 = heights[(i + 1) * n + j];
                if (Math.min(Math.min(h00, h01), Math.min(h11, h10)) < floor || Math.max(Math.max(h00, h01), Math.max(h11, h10)) > ceiling) {
                    continue;
                }
                float u0 = i / (float) cells;
                float u1 = (i + 1) / (float) cells;
                float v0 = j / (float) cells;
                float v1 = (j + 1) / (float) cells;
                vertex(x0 + i * step, h00, z0 + j * step, u0, v0, rgb, alpha);
                vertex(x0 + i * step, h01, z0 + (j + 1) * step, u0, v1, rgb, alpha);
                vertex(x0 + (i + 1) * step, h11, z0 + (j + 1) * step, u1, v1, rgb, alpha);
                vertex(x0 + (i + 1) * step, h10, z0 + j * step, u1, v0, rgb, alpha);
            }
        }
    }

    public static void ribbon(Vec3[] points, float[] widths, float[] alphas, int rgb, float uStart, float uEnd) {
        int n = points.length;
        if (n < 2) {
            return;
        }
        Vec3[] sides = new Vec3[n];
        for (int i = 0; i < n; i++) {
            Vec3 prev = points[Math.max(0, i - 1)];
            Vec3 next = points[Math.min(n - 1, i + 1)];
            Vec3 tangent = next.subtract(prev);
            Vec3 toCam = camPos.subtract(points[i]);
            Vec3 side = tangent.cross(toCam);
            if (side.lengthSqr() < 1.0E-8) {
                side = camRight;
            }
            sides[i] = side.normalize().scale(widths[i] * 0.5);
        }
        normal(0.0, 1.0, 0.0);
        for (int i = 0; i < n - 1; i++) {
            float u0 = Mth.lerp(i / (float) (n - 1), uStart, uEnd);
            float u1 = Mth.lerp((i + 1) / (float) (n - 1), uStart, uEnd);
            vertex(points[i].subtract(sides[i]), u0, 0.0F, rgb, alphas[i]);
            vertex(points[i].add(sides[i]), u0, 1.0F, rgb, alphas[i]);
            vertex(points[i + 1].add(sides[i + 1]), u1, 1.0F, rgb, alphas[i + 1]);
            vertex(points[i + 1].subtract(sides[i + 1]), u1, 0.0F, rgb, alphas[i + 1]);
        }
    }

    public static void flatRibbon(Vec3[] points, float[] widths, float[] alphas, int rgb, float uStart, float uEnd) {
        int n = points.length;
        if (n < 2) {
            return;
        }
        Vec3[] sides = new Vec3[n];
        for (int i = 0; i < n; i++) {
            Vec3 prev = points[Math.max(0, i - 1)];
            Vec3 next = points[Math.min(n - 1, i + 1)];
            Vec3 side = new Vec3(-(next.z - prev.z), 0.0, next.x - prev.x);
            if (side.lengthSqr() < 1.0E-8) {
                side = new Vec3(1.0, 0.0, 0.0);
            }
            sides[i] = side.normalize().scale(widths[i] * 0.5);
        }
        normal(0.0, 1.0, 0.0);
        for (int i = 0; i < n - 1; i++) {
            float u0 = Mth.lerp(i / (float) (n - 1), uStart, uEnd);
            float u1 = Mth.lerp((i + 1) / (float) (n - 1), uStart, uEnd);
            vertex(points[i].subtract(sides[i]), u0, 0.0F, rgb, alphas[i]);
            vertex(points[i].add(sides[i]), u0, 1.0F, rgb, alphas[i]);
            vertex(points[i + 1].add(sides[i + 1]), u1, 1.0F, rgb, alphas[i + 1]);
            vertex(points[i + 1].subtract(sides[i + 1]), u1, 0.0F, rgb, alphas[i + 1]);
        }
    }

    public static void wall(Vec3[] bases, float[] heights, float[] alphas, int rgb, float vStart, float vEnd) {
        int n = bases.length;
        normal(0.0, 1.0, 0.0);
        for (int i = 0; i < n - 1; i++) {
            float v0 = Mth.lerp(i / (float) (n - 1), vStart, vEnd);
            float v1 = Mth.lerp((i + 1) / (float) (n - 1), vStart, vEnd);
            vertex(bases[i], 0.0F, v0, rgb, alphas[i]);
            vertex(bases[i].add(0.0, heights[i], 0.0), 1.0F, v0, rgb, alphas[i]);
            vertex(bases[i + 1].add(0.0, heights[i + 1], 0.0), 1.0F, v1, rgb, alphas[i + 1]);
            vertex(bases[i + 1], 0.0F, v1, rgb, alphas[i + 1]);
        }
    }

    public static void beam(Vec3 a, Vec3 b, float width, int rgb, float alpha, float uEnd) {
        ribbon(new Vec3[]{a, b}, new float[]{width, width}, new float[]{alpha, alpha}, rgb, 0.0F, uEnd);
    }

    public static Vec3[] basis(Vec3 axis) {
        Vec3 n = axis.normalize();
        Vec3 ref = new Vec3(0.0, 1.0, 0.0);
        if (Math.abs(n.y) > 0.9) {
            ref = new Vec3(1.0, 0.0, 0.0);
        }
        Vec3 u = n.cross(ref).normalize();
        Vec3 v = n.cross(u).normalize();
        return new Vec3[]{u, v};
    }

    public static void cylinder(Vec3 a, Vec3 b, float radiusA, float radiusB, int segments, int rgb, float alpha, float vRepeat) {
        Vec3 axis = b.subtract(a);
        if (axis.lengthSqr() < 1.0E-6) {
            return;
        }
        Vec3[] base = basis(axis);
        for (int i = 0; i < segments; i++) {
            float a0 = Mth.TWO_PI * i / segments;
            float a1 = Mth.TWO_PI * (i + 1) / segments;
            Vec3 d0 = base[0].scale(Mth.cos(a0)).add(base[1].scale(Mth.sin(a0)));
            Vec3 d1 = base[0].scale(Mth.cos(a1)).add(base[1].scale(Mth.sin(a1)));
            float v0 = vRepeat * i / segments;
            float v1 = vRepeat * (i + 1) / segments;
            normal(d0.x, d0.y, d0.z);
            vertex(a.add(d0.scale(radiusA)), 0.0F, v0, rgb, alpha);
            vertex(b.add(d0.scale(radiusB)), 1.0F, v0, rgb, alpha);
            normal(d1.x, d1.y, d1.z);
            vertex(b.add(d1.scale(radiusB)), 1.0F, v1, rgb, alpha);
            vertex(a.add(d1.scale(radiusA)), 0.0F, v1, rgb, alpha);
        }
    }

    public static float lightAt(Vec3 pos) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return 1.0F;
        }
        int packed = LevelRenderer.getLightCoords(level, BlockPos.containing(pos));
        int value = Math.max(LightCoordsUtil.block(packed), LightCoordsUtil.sky(packed) - level.getSkyDarken());
        return 0.25F + 0.75F * Mth.clamp(value / 15.0F, 0.0F, 1.0F);
    }

    public static void crossSprite(Vec3 base, float width, float height, double dirX, double dirZ, float light) {
        Vec3 top = base.add(0.0, height, 0.0);
        int c = Mth.clamp((int) (light * 255.0F), 0, 255);
        int rgb = (c << 16) | (c << 8) | c;
        int side = Mth.clamp((int) (light * 0.8F * 255.0F), 0, 255);
        int sideRgb = (side << 16) | (side << 8) | side;
        Vec3 a = new Vec3(dirX, 0.0, dirZ).scale(width * 0.5);
        Vec3 b = new Vec3(-dirZ, 0.0, dirX).scale(width * 0.5);
        vertex(base.subtract(a), 0.0F, 1.0F, rgb, 1.0F);
        vertex(top.subtract(a), 0.0F, 0.0F, rgb, 1.0F);
        vertex(top.add(a), 1.0F, 0.0F, rgb, 1.0F);
        vertex(base.add(a), 1.0F, 1.0F, rgb, 1.0F);
        vertex(base.subtract(b), 0.0F, 1.0F, sideRgb, 1.0F);
        vertex(top.subtract(b), 0.0F, 0.0F, sideRgb, 1.0F);
        vertex(top.add(b), 1.0F, 0.0F, sideRgb, 1.0F);
        vertex(base.add(b), 1.0F, 1.0F, sideRgb, 1.0F);
    }

    public static void cone(Vec3 base, Vec3 tip, float radius, int segments, int rgb, float alpha) {
        cylinder(base, tip, radius, 0.0F, segments, rgb, alpha, 1.0F);
    }

    public static void sphere(Vec3 c, float radius, int rings, int segments, int rgb, float alpha) {
        for (int i = 0; i < rings; i++) {
            float t0 = Mth.PI * i / rings;
            float t1 = Mth.PI * (i + 1) / rings;
            for (int j = 0; j < segments; j++) {
                float p0 = Mth.TWO_PI * j / segments;
                float p1 = Mth.TWO_PI * (j + 1) / segments;
                sphereVertex(c, radius, t0, p0, j / (float) segments, i / (float) rings, rgb, alpha);
                sphereVertex(c, radius, t1, p0, j / (float) segments, (i + 1) / (float) rings, rgb, alpha);
                sphereVertex(c, radius, t1, p1, (j + 1) / (float) segments, (i + 1) / (float) rings, rgb, alpha);
                sphereVertex(c, radius, t0, p1, (j + 1) / (float) segments, i / (float) rings, rgb, alpha);
            }
        }
    }

    private static void sphereVertex(Vec3 c, float radius, float theta, float phi, float u, float v, int rgb, float alpha) {
        float st = Mth.sin(theta);
        double x = st * Mth.cos(phi);
        double y = Mth.cos(theta);
        double z = st * Mth.sin(phi);
        normal(x, y, z);
        vertex(c.x + x * radius, c.y + y * radius, c.z + z * radius, u, v, rgb, alpha);
    }

    public static void ring(Vec3 c, Vec3 axis, float inner, float outer, int segments, int rgb, float alphaInner, float alphaOuter, float uRepeat) {
        Vec3[] base = basis(axis);
        Vec3 n = axis.normalize();
        normal(n.x, n.y, n.z);
        for (int i = 0; i < segments; i++) {
            float a0 = Mth.TWO_PI * i / segments;
            float a1 = Mth.TWO_PI * (i + 1) / segments;
            Vec3 d0 = base[0].scale(Mth.cos(a0)).add(base[1].scale(Mth.sin(a0)));
            Vec3 d1 = base[0].scale(Mth.cos(a1)).add(base[1].scale(Mth.sin(a1)));
            float u0 = uRepeat * i / segments;
            float u1 = uRepeat * (i + 1) / segments;
            vertex(c.add(d0.scale(inner)), u0, 0.0F, rgb, alphaInner);
            vertex(c.add(d0.scale(outer)), u0, 1.0F, rgb, alphaOuter);
            vertex(c.add(d1.scale(outer)), u1, 1.0F, rgb, alphaOuter);
            vertex(c.add(d1.scale(inner)), u1, 0.0F, rgb, alphaInner);
        }
    }

    public static int rgb(float r, float g, float b) {
        int ri = Mth.clamp((int) (r * 255.0F), 0, 255);
        int gi = Mth.clamp((int) (g * 255.0F), 0, 255);
        int bi = Mth.clamp((int) (b * 255.0F), 0, 255);
        return (ri << 16) | (gi << 8) | bi;
    }

    public static int mix(int a, int b, float k) {
        float ar = ((a >> 16) & 255) / 255.0F;
        float ag = ((a >> 8) & 255) / 255.0F;
        float ab = (a & 255) / 255.0F;
        float br = ((b >> 16) & 255) / 255.0F;
        float bg = ((b >> 8) & 255) / 255.0F;
        float bb = (b & 255) / 255.0F;
        return rgb(Mth.lerp(k, ar, br), Mth.lerp(k, ag, bg), Mth.lerp(k, ab, bb));
    }
}
