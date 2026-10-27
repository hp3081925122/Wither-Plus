package com.witherplus;

import com.mojang.logging.LogUtils;
import com.witherplus.network.WpNetwork;
import com.witherplus.item.WpItems;
import com.witherplus.loot.WpLootModifiers;
import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.WitherBrainHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(WitherPlus.MODID)
public final class WitherPlus {
    public static final String MODID = "witherplus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WitherPlus(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, WpConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, WpConfig.CLIENT_SPEC);
        modBus.addListener(WpNetwork::register);
        WpItems.register(modBus);
        WpLootModifiers.register(modBus);
        modBus.addListener(this::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(this::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(this::onStartTracking);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        if (WpConfig.DEBUG_LOGGING.get()) {
            LOGGER.info("Wither skeleton skull block maps to item {}", BuiltInRegistries.ITEM.getKey(Blocks.WITHER_SKELETON_SKULL.asItem()));
        }
    }

    private void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof WitherBoss wither) || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        WitherBrain brain = ((WitherBrainHolder) wither).wp$getBrain();
        if (brain.active()) {
            LOGGER.info("Sending wither {} stage {} to {} on start tracking", wither.getId(), brain.stage(), player.getGameProfile().getName());
            WpNetwork.sendToPlayer(player, brain.stagePayload());
        }
    }

    private void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof WitherBoss wither)) {
            return;
        }
        SpecialSummonTracker.applyIfPending(event.getLevel(), wither);
        if (!((WitherBrainHolder) wither).wp$getBrain().active()) {
            return;
        }
        AttributeInstance attack = wither.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(WpConfig.ATTACK_DAMAGE.get());
        }
        AttributeInstance health = wither.getAttribute(Attributes.MAX_HEALTH);
        double configured = WpConfig.MAX_HEALTH.get();
        if (health != null && health.getBaseValue() != configured) {
            float ratio = wither.getHealth() / wither.getMaxHealth();
            health.setBaseValue(configured);
            wither.setHealth(ratio * wither.getMaxHealth());
        }
        LOGGER.debug("Wither {} joined with attack {} and max health {}", wither.getId(), wither.getAttributeValue(Attributes.ATTACK_DAMAGE), wither.getMaxHealth());
    }
}
