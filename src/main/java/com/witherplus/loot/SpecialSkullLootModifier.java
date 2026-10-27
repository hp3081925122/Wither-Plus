package com.witherplus.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.witherplus.SpecialSummonTracker;
import com.witherplus.WitherPlus;
import com.witherplus.WpConfig;
import com.witherplus.item.WpItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class SpecialSkullLootModifier extends LootModifier {
    public static final MapCodec<SpecialSkullLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, SpecialSkullLootModifier::new));

    public SpecialSkullLootModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!(context.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof SkullBlockEntity skull) || !SpecialSummonTracker.isSpecial(skull)) return generatedLoot;
        int replaced = 0;
        for (int i = 0; i < generatedLoot.size(); i++) {
            ItemStack stack = generatedLoot.get(i);
            if (stack.is(Items.WITHER_SKELETON_SKULL)) {
                generatedLoot.set(i, stack.transmuteCopy(WpItems.SPECIAL_WITHER_SKELETON_SKULL.get()));
                replaced++;
            }
        }
        if (WpConfig.DEBUG_LOGGING.get()) WitherPlus.LOGGER.info("Special skull at {} dropped, replaced {} vanilla skull stack(s)", skull.getBlockPos(), replaced);
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
