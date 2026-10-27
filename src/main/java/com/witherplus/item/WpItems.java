package com.witherplus.item;

import com.witherplus.WitherPlus;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class WpItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WitherPlus.MODID);
    public static final RegistryObject<Item> SPECIAL_WITHER_SKELETON_SKULL = ITEMS.register(
            "special_wither_skeleton_skull",
            () -> new SpecialSkullItem(new Item.Properties().rarity(Rarity.EPIC)));

    private WpItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
