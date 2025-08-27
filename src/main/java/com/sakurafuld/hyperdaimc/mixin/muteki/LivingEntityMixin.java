package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.api.mixin.ILivingEntityMuteki;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ILivingEntityMuteki {
    @Unique
    private boolean muteki = false;
    @Unique
    private boolean forced = false;
    @Unique
    private float lastHealth = 1;

    @Shadow
    @Final
    private static EntityDataAccessor<Float> DATA_HEALTH_ID;

    @Shadow
    public abstract float getHealth();

    @Override
    public boolean muteki() {
        return this.muteki;
    }

    @Override
    public void mutekiForce(boolean force) {
        this.forced = force;
    }

    @Override
    public boolean mutekiForced() {
        return this.forced;
    }

    @Override
    public float mutekiLastHealth() {
        return this.lastHealth;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickMuteki(CallbackInfo ci) {
        this.muteki = MutekiHandler.checkMuteki((LivingEntity) ((Object) this));

        this.mutekiForce(true);
        float health = this.getHealth();
        if (health > 0) {
            this.lastHealth = health;
        }
        this.mutekiForce(false);
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void dieMuteki$LivingEntity(DamageSource pDamageSource, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) ((Object) this);

        if ((!Float.isFinite(self.getHealth()) || !NovelHandler.novelized(self)) && MutekiHandler.muteki(self)) {
            ci.cancel();
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"))
    private void readAdditionalSaveDataMuteki(CompoundTag pCompound, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) ((Object) this);
        this.mutekiForce(true);
        self.getEntityData().set(DATA_HEALTH_ID, pCompound.getFloat("Health"));
        this.mutekiForce(false);
    }
}
