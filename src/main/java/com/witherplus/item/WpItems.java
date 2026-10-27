package com.witherplus.item;

import com.witherplus.WitherPlus;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WpItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WitherPlus.MODID);
    public static final DeferredItem<SpecialSkullItem> SPECIAL_WITHER_SKELETON_SKULL = ITEMS.register(
            "special_wither_skeleton_skull",
            () -> new SpecialSkullItem(new Item.Properties().rarity(Rarity.EPIC)));

    private WpItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
