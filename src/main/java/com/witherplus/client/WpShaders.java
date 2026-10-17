package com.witherplus.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.witherplus.WitherPlus;
import java.util.Optional;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class WpShaders {
    public static final int SCREEN = 8;
    private static final RenderPipeline[] PIPELINES = new RenderPipeline[9];
    static {
        String[] fragments = {"flame", "flame", "flame", "ground", "ground", "sphere", "sphere", "block", "screen"};
        for (int i = 0; i < PIPELINES.length; i++) {
            boolean screen = i == SCREEN;
            boolean normal = i == 5 || i == 6;
            boolean additive = i < Draw.Material.values().length && Draw.Material.values()[i].additive;
            var builder = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(WitherPlus.id("pipeline/wp_" + i))
                .withVertexShader(WitherPlus.id("core/wp_" + (screen ? "screen" : normal ? "normal" : "tex")))
                .withFragmentShader(WitherPlus.id("core/wp_" + fragments[i]))
                .withVertexFormat(screen ? DefaultVertexFormat.POSITION_TEX : normal ? DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL : DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withUniform("WpUniforms", UniformType.UNIFORM_BUFFER)
                .withSampler("Sampler0")
                .withDepthStencilState(screen ? Optional.empty() : Optional.of(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, i == 7)))
                .withColorTargetState(new ColorTargetState(additive ? BlendFunction.LIGHTNING : BlendFunction.TRANSLUCENT))
                .withCull(false);
            PIPELINES[i] = builder.build();
        }
    }
    private WpShaders() {}
    public static void register(RegisterRenderPipelinesEvent event) {
        for (var pipeline : PIPELINES) event.registerPipeline(pipeline);
        WitherPlus.LOGGER.info("Registered {} Wither Plus render pipelines", PIPELINES.length);
    }
    public static RenderPipeline pipeline(int index) { return PIPELINES[index]; }
}
