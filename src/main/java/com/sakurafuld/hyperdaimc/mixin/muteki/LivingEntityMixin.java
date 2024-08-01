package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.ModItems;
import com.sakurafuld.hyperdaimc.content.muteki.MutekiHandler;
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
import top.theillusivec4.curios.api.CuriosApi;

import static com.sakurafuld.hyperdaimc.Deets.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow @Final private static EntityDataAccessor<Float> DATA_HEALTH_ID;

    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void setHealthMuteki(float pHealth, CallbackInfo ci){
        LivingEntity self = (LivingEntity) ((Object) this);
        if(!CuriosApi.getCuriosHelper().findCurios(self, ModItems.MUTEKI.get()).isEmpty()){
            if(MutekiHandler.getNovelized() != null && MutekiHandler.getNovelized().equals(self)){
                self.getEntityData().set(DATA_HEALTH_ID, self.getHealth() - 1);
                self.hurtDuration = 10;
                self.hurtTime = self.hurtDuration;
                ci.cancel();
                return;
            }
            if(pHealth <= self.getHealth())
                ci.cancel();
        }
    }
    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void dieMuteki$LivingEntity(DamageSource pDamageSource, CallbackInfo ci){
        LivingEntity self = (LivingEntity) ((Object) this);
        if(!CuriosApi.getCuriosHelper().findCurios(self, ModItems.MUTEKI.get()).isEmpty()){
            if(MutekiHandler.getNovelized() != null && MutekiHandler.getNovelized().equals(self) && self.getHealth() <= 0)
                return;
            ci.cancel();
        }
    }
    @Inject(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"))
    private void readAdditionalSaveDataMuteki(CompoundTag pCompound, CallbackInfo ci){
        LivingEntity self = (LivingEntity) ((Object) this);
        self.getEntityData().set(DATA_HEALTH_ID, pCompound.getFloat("Health"));
    }
}
