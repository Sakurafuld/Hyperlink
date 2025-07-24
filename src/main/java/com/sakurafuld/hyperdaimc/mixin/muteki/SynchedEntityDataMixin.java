package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.api.mixin.ILivingEntityMuteki;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
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

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private <T> void setMuteki(EntityDataAccessor<T> pKey, T pValue, CallbackInfo ci) {
        if (pKey == LivingEntityAccessor.getDATA_HEALTH_ID() && this.entity instanceof LivingEntity living && pValue instanceof Float health) {
            if (MutekiHandler.muteki(living)) {
                if (((ILivingEntityMuteki) living).forced()) {
                    return;
                }
                if (health <= this.getItem(LivingEntityAccessor.getDATA_HEALTH_ID()).getValue() || !Float.isFinite(health)) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("unchecked")
    private <T> void getMuteki(EntityDataAccessor<T> pKey, CallbackInfoReturnable<T> cir) {
        if (pKey == LivingEntityAccessor.getDATA_HEALTH_ID() && this.entity instanceof LivingEntity living && !((ILivingEntityMuteki) living).forced()) {
            CallbackInfoReturnable<Float> cirf = ((CallbackInfoReturnable<Float>) cir);
            boolean muteki = MutekiHandler.muteki(living);
            if (NovelHandler.novelized(living)) {
                cirf.setReturnValue(0f);
            } else if (muteki) {
                cirf.setReturnValue(((ILivingEntityMuteki) living).lastHealth());
            }
        }
    }
}
