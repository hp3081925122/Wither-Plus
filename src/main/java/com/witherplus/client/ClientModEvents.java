package com.witherplus.client;

import com.witherplus.WitherPlus;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = WitherPlus.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        WpShaders.register(event);
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiOverlaysEvent event) {
        event.registerBelowAll("screen_fx", (gui, graphics, partialTick, width, height) -> ScreenFx.render(graphics, partialTick));
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
