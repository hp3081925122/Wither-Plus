package com.witherplus.loot;

import com.mojang.serialization.Codec;
import com.witherplus.WitherPlus;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class WpLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, WitherPlus.MODID);

    static {
        LOOT_MODIFIERS.register("special_skull_drop", () -> SpecialSkullLootModifier.CODEC);
    }

    private WpLootModifiers() {
    }

    public static void register(IEventBus bus) {
        LOOT_MODIFIERS.register(bus);
    }
}
