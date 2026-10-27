package com.witherplus.loot;

import com.mojang.serialization.MapCodec;
import com.witherplus.WitherPlus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class WpLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, WitherPlus.MODID);

    static {
        LOOT_MODIFIERS.register("special_skull_drop", () -> SpecialSkullLootModifier.CODEC);
    }

    private WpLootModifiers() {}

    public static void register(IEventBus bus) {
        LOOT_MODIFIERS.register(bus);
    }
}
