package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.system.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.ILivingEntityMuteki;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SynchedEntityData.class)
public abstract class SynchedEntityDataMixin {
    @Shadow
    @Final
    private Entity entity;

    @Shadow
    public abstract <T> T get(EntityDataAccessor<T> pKey);

    @Shadow
    protected abstract <T> SynchedEntityData.DataItem<T> getItem(EntityDataAccessor<T> pKey);

    @Inject(method = "set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;Z)V", at = @At("HEAD"), cancellable = true)
    private <T> void setMuteki(EntityDataAccessor<T> pKey, T pValue, boolean pForce, CallbackInfo ci) {
        if (pKey == LivingEntityAccessor.getDATA_HEALTH_ID() && this.entity instanceof LivingEntity living && pValue instanceof Float health) {
            if (MutekiHandler.muteki(living)) {
                if (((ILivingEntityMuteki) living).hyperdaimc$isMutekiForced())
                    return;

                if (health <= this.getItem(LivingEntityAccessor.getDATA_HEALTH_ID()).getValue() || !Float.isFinite(health))
                    ci.cancel();
            }
        }
    }

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings({"unchecked", "CancellableInjectionUsage"})
    private <T> void getMuteki(EntityDataAccessor<T> pKey, CallbackInfoReturnable<T> cir) {
        if (pKey == LivingEntityAccessor.getDATA_HEALTH_ID() && this.entity instanceof LivingEntity living && !((ILivingEntityMuteki) living).hyperdaimc$isMutekiForced()) {
            CallbackInfoReturnable<Float> cirf = ((CallbackInfoReturnable<Float>) cir);
            if (NovelHandler.novelized(living))
                cirf.setReturnValue(0f);
            else if (MutekiHandler.muteki(living))
                cirf.setReturnValue(((ILivingEntityMuteki) living).hyperdaimc$mutekiLastHealth());
        }
    }
}
