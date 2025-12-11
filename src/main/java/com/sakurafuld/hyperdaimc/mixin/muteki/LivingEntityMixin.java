package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.ILivingEntityMuteki;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ILivingEntityMuteki {
    @Unique
    private static EntityDataAccessor<Boolean> DATA_MUTEKI;
    @Unique
    private boolean initialized = false;
    @Unique
    private boolean forced = false;
    @Unique
    private float lastHealth = 1;
    @Unique
    private int mutekiNovelized = 0;

    @Shadow
    @Final
    private static EntityDataAccessor<Float> DATA_HEALTH_ID;

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract void setHealth(float pHealth);

    @SuppressWarnings("ConstantValue")
    @Override
    public boolean hyperdaimc$muteki() {
        return this.initialized && ((LivingEntity) (Object) this).getEntityData().get(DATA_MUTEKI);
    }

    @Override
    public void hyperdaimc$mutekiForce(boolean force) {
        this.forced = force;
    }

    @Override
    public boolean hyperdaimc$isMutekiForced() {
        return this.forced;
    }

    @Override
    public float hyperdaimc$mutekiLastHealth() {
        return this.lastHealth;
    }

    @Override
    public void hyperdaimc$mutekiNovelize() {
        this.mutekiNovelized++;
    }

    @Override
    public void hyperdaimc$mutekiSetLocal(float local) {
        this.lastHealth = local;
    }

    @Inject(method = "<clinit>", at = @At("HEAD"))
    private static void staticInitializerMuteki(CallbackInfo ci) {
        DATA_MUTEKI = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializerMuteki(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        self.getEntityData().define(DATA_MUTEKI, false);
        this.initialized = true;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickMuteki(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        boolean muteki = MutekiHandler.checkMuteki(self);
        self.getEntityData().set(DATA_MUTEKI, muteki);

        this.hyperdaimc$mutekiForce(true);
        if (muteki) {
            this.lastHealth = Math.max(this.getHealth(), this.lastHealth - this.mutekiNovelized);
            if (/*!self.level().isClientSide() && */this.getHealth() < this.lastHealth)
                this.setHealth(this.lastHealth);
        } else this.lastHealth = 1;
        this.mutekiNovelized = 0;

        this.hyperdaimc$mutekiForce(false);
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void dieMuteki$LivingEntity(DamageSource pDamageSource, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!NovelHandler.novelized(self) && MutekiHandler.muteki(self))
            ci.cancel();
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    private void dropAllDeathLootMuteki(DamageSource pDamageSource, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!NovelHandler.novelized(self) && MutekiHandler.muteki(self))
            ci.cancel();
    }

    @Inject(method = "shouldDropLoot", at = @At("HEAD"), cancellable = true)
    private void shouldDropLootMuteki(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!NovelHandler.novelized(self) && MutekiHandler.muteki(self))
            cir.setReturnValue(false);
    }

    @Inject(method = "shouldDropExperience", at = @At("HEAD"), cancellable = true)
    private void shouldDropExperienceLootMuteki(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!NovelHandler.novelized(self) && MutekiHandler.muteki(self))
            cir.setReturnValue(false);
    }

    @Inject(method = "dropFromLootTable", at = @At("HEAD"), cancellable = true)
    private void dropFromLootTableMuteki(DamageSource pDamageSource, boolean pHitByPlayer, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!NovelHandler.novelized(self) && MutekiHandler.muteki(self))
            ci.cancel();
    }

    @Inject(method = "dropCustomDeathLoot", at = @At("HEAD"), cancellable = true)
    private void dropCustomDeathLootMuteki(DamageSource pDamageSource, int pLooting, boolean pHitByPlayer, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!NovelHandler.novelized(self) && MutekiHandler.muteki(self))
            ci.cancel();
    }

    @Inject(method = "dropExperience", at = @At("HEAD"), cancellable = true)
    private void dropExperienceMuteki(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!NovelHandler.novelized(self) && MutekiHandler.muteki(self))
            ci.cancel();
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"))
    private void readAdditionalSaveDataMuteki(CompoundTag pCompound, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        this.hyperdaimc$mutekiForce(true);
        self.getEntityData().set(DATA_HEALTH_ID, pCompound.getFloat("Health"));
        this.hyperdaimc$mutekiForce(false);
    }
}
