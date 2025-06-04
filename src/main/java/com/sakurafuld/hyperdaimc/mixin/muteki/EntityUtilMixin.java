package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.content.muteki.MutekiHandler;
import flashfur.omnimobs.util.EntityUtil;
import flashfur.omnimobs.util.ForceDamageSource;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(EntityUtil.class)
public abstract class EntityUtilMixin {
    @Inject(method = "forceSetDeltaMovement(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void forceSetDeltaMovementMuteki(Entity entity, Vec3 vec3, CallbackInfo ci) {
        if (entity instanceof IFumetsu || (entity instanceof LivingEntity living && MutekiHandler.muteki(living))) {
            ci.cancel();
        }
    }

    @Inject(method = "forceHurt", at = @At("HEAD"), cancellable = true, remap = false)
    private static void forceHurtMuteki(LivingEntity attacker, LivingEntity entity, ForceDamageSource damageSource, CallbackInfo ci) {
        if (MutekiHandler.muteki(entity)) {
            entity.hurt(entity.damageSources().generic(), 1);
            ci.cancel();
        }
    }

    @Inject(method = "forceSetHealth", at = @At("HEAD"), cancellable = true, remap = false)
    private static void forceSetHealthMuteki(LivingEntity entity, float value, CallbackInfo ci) {
        if (MutekiHandler.muteki(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "forceSetRemoved", at = @At("HEAD"), cancellable = true, remap = false)
    private static void forceSetRemoveMuteki(Entity entity, Entity.RemovalReason reason, CallbackInfo ci) {
        if (entity instanceof IFumetsu || (entity instanceof LivingEntity living && MutekiHandler.muteki(living))) {
            ci.cancel();
        }
    }

    @Inject(method = "forceSetPos", at = @At("HEAD"), cancellable = true, remap = false)
    private static void forceSetPosMuteki(Entity entity, Vec3 position, CallbackInfo ci) {
        if (entity instanceof IFumetsu || (entity instanceof LivingEntity living && MutekiHandler.muteki(living))) {
            ci.cancel();
        }
    }

    @Inject(method = "forceSetEntityData", at = @At("HEAD"), cancellable = true, remap = false)
    private static <T> void forceSetEntityDataMuteki(SynchedEntityData entityData, EntityDataAccessor<T> entityDataAccessor, T t, CallbackInfo ci) {
        Entity entity = ((SynchedEntityDataAccessor) entityData).getEntity();
        if (entity instanceof IFumetsu || (entity instanceof LivingEntity living && MutekiHandler.muteki(living))) {
            ci.cancel();
        }
    }

    @Inject(method = "forceRemoveNoPacket", at = @At("HEAD"), cancellable = true, remap = false)
    private static void forceRemoveNoPacketMuteki(Entity entity, Entity.RemovalReason reason, boolean leaveLevelCalls, CallbackInfo ci) {
        if (entity instanceof IFumetsu || (entity instanceof LivingEntity living && MutekiHandler.muteki(living))) {
            ci.cancel();
        }
    }
}
