package com.witherplus.mixin;

import com.witherplus.wither.WitherBrain;
import com.witherplus.wither.WitherBrainHolder;
import com.witherplus.WpConfig;
import com.witherplus.WitherPlus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin extends Monster implements WitherBrainHolder {
    @Unique
    private WitherBrain wp$brain;

    protected WitherBossMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public WitherBrain wp$getBrain() {
        if (this.wp$brain == null) {
            this.wp$brain = new WitherBrain((WitherBoss) (Object) this);
        }
        return this.wp$brain;
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        if (!this.wp$getBrain().active()) {
            return super.getDefaultLootTable();
        }
        String configured = WpConfig.ENHANCED_LOOT_TABLE.get();
        ResourceLocation table = ResourceLocation.tryParse(configured);
        if (table == null) {
            WitherPlus.LOGGER.warn("Invalid enhanced wither loot table path '{}', using vanilla loot table", configured);
            return super.getDefaultLootTable();
        }
        return ResourceKey.create(Registries.LOOT_TABLE, table);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void wp$aiStep(CallbackInfo ci) {
        if (this.level().isClientSide || !this.isAlive()) {
            return;
        }
        WitherBrain brain = this.wp$getBrain();
        if (brain.active()) {
            brain.tick();
        } else if (brain.busy()) {
            brain.abort();
        }
    }

    @Inject(method = "performRangedAttack(IDDDZ)V", at = @At("HEAD"), cancellable = true)
    private void wp$performRangedAttack(int head, double x, double y, double z, boolean dangerous, CallbackInfo ci) {
        if (this.level().isClientSide) {
            return;
        }
        WitherBrain brain = this.wp$getBrain();
        if (brain.active() && brain.busy()) {
            ci.cancel();
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void wp$hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (this.level().isClientSide) {
            return;
        }
        WitherBrain brain = this.wp$getBrain();
        if (brain.active() && brain.invulnerable() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void wp$save(CompoundTag tag, CallbackInfo ci) {
        this.wp$getBrain().save(tag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void wp$load(CompoundTag tag, CallbackInfo ci) {
        this.wp$getBrain().load(tag);
    }
}
