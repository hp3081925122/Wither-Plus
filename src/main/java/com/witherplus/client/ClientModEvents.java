package com.witherplus.client;

import com.witherplus.WitherPlus;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = WitherPlus.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        WpShaders.register(event);
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerBelowAll(WitherPlus.id("screen_fx"), (graphics, delta) -> ScreenFx.render(graphics, delta.getGameTimeDeltaPartialTick(false)));
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        LivingEntityRenderer<WitherBoss, WitherBossModel<WitherBoss>> renderer = event.getRenderer(EntityType.WITHER);
        if (renderer == null) {
            WitherPlus.LOGGER.warn("Wither renderer not found, glow layer skipped");
            return;
        }
        renderer.addLayer(new WitherGlowLayer(renderer));
    }
}
