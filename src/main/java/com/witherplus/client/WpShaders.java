package com.witherplus.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.witherplus.WitherPlus;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.client.event.RegisterShadersEvent;

public final class WpShaders {
    @Nullable
    public static ShaderInstance flame;
    @Nullable
    public static ShaderInstance ground;
    @Nullable
    public static ShaderInstance sphere;
    @Nullable
    public static ShaderInstance screen;

    private static boolean additive;

    private WpShaders() {
    }

    public static void setAdditive(boolean value) {
        additive = value;
    }

    private static final class WorldShader extends ShaderInstance {
        private WorldShader(ResourceProvider provider, ResourceLocation id, VertexFormat format) throws IOException {
            super(provider, id, format);
        }

        @Override
        public void apply() {
            super.apply();
            RenderSystem.enableBlend();
            if (additive) {
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            } else {
                RenderSystem.defaultBlendFunc();
            }
        }
    }

    public static void register(RegisterShadersEvent event) {
        try {
            event.registerShader(new WorldShader(event.getResourceProvider(), WitherPlus.id("wp_flame"), DefaultVertexFormat.POSITION_TEX_COLOR), s -> flame = s);
            event.registerShader(new WorldShader(event.getResourceProvider(), WitherPlus.id("wp_ground"), DefaultVertexFormat.POSITION_TEX_COLOR), s -> ground = s);
            event.registerShader(new WorldShader(event.getResourceProvider(), WitherPlus.id("wp_sphere"), DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL), s -> sphere = s);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), WitherPlus.id("wp_screen"), DefaultVertexFormat.POSITION_TEX), s -> screen = s);
            WitherPlus.LOGGER.info("Wither Plus shaders registered");
        } catch (IOException e) {
            WitherPlus.LOGGER.error("Failed to load Wither Plus shaders", e);
        }
    }
}
