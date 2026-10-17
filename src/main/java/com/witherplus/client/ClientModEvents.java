package com.witherplus.client;

import com.witherplus.WitherPlus;
import com.witherplus.mixin.client.LivingRendererAccessor;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

@EventBusSubscriber(modid = WitherPlus.MODID, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}
    @SubscribeEvent
    public static void onRegisterShaders(RegisterRenderPipelinesEvent event) { WpShaders.register(event); }
    @SubscribeEvent
    public static void onStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(WitherBossRenderer.class, (WitherBoss entity, WitherRenderState state) -> state.setRenderData(WitherVisualState.KEY, WitherVisualState.of(entity)));
    }
    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        if (event.getRenderer(EntityType.WITHER) instanceof WitherBossRenderer renderer) {
            ((LivingRendererAccessor) renderer).wp$addLayer(new WitherGlowLayer(renderer));
        } else {
            WitherPlus.LOGGER.warn("Wither renderer not found, glow layer skipped");
        }
    }
}
