package com.witherplus.mixin;

import com.witherplus.WitherPlus;
import com.witherplus.WpConfig;
import com.witherplus.wither.WitherBrainHolder;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobLootTableMixin {
    @Shadow
    private Optional<ResourceKey<LootTable>> lootTable;

    @Inject(method = "getLootTable", at = @At("RETURN"), cancellable = true)
    private void wp$getLootTable(CallbackInfoReturnable<Optional<ResourceKey<LootTable>>> cir) {
        if (!this.lootTable.isEmpty() || !((Object) this instanceof WitherBoss wither)
                || !((WitherBrainHolder) wither).wp$getBrain().active()) {
            return;
        }
        String configured = WpConfig.ENHANCED_LOOT_TABLE.get();
        Identifier table = Identifier.tryParse(configured);
        if (table == null) {
            WitherPlus.LOGGER.warn("Invalid enhanced wither loot table path '{}', using vanilla loot table", configured);
            return;
        }
        cir.setReturnValue(Optional.of(ResourceKey.create(Registries.LOOT_TABLE, table)));
    }
}
