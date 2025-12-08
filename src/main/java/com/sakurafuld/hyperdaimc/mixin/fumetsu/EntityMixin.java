package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.infrastructure.entity.IFumetsu;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityFumetsu;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityNovel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraftforge.common.util.ITeleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityFumetsu {
    @Unique
    private EntityInLevelCallback levelCallback2 = EntityInLevelCallback.NULL;

    @Override
    public void hyperdaimc$extinction(Entity.RemovalReason reason) {
//        Deets.LOG.debug("callbackRemoveFumetsu");
        this.levelCallback2.onRemove(reason);
    }

    @Inject(method = "setPosRaw", at = @At("HEAD"), cancellable = true)
    private void setPosRawFumetsu$0(double pX, double pY, double pZ, CallbackInfo ci) {
        if ((Object) this instanceof IFumetsu fumetsu && !fumetsu.isMovable() && !FumetsuHandler.spawn.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "setPosRaw", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/EntityInLevelCallback;onMove()V"))
    private void setPosRawFumetsu$1(double pX, double pY, double pZ, CallbackInfo ci) {
        if (this instanceof IFumetsu) {
            this.levelCallback2.onMove();
        }
    }

    @Inject(method = "setRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/EntityInLevelCallback;onRemove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"))
    private void setRemovedFumetsu(Entity.RemovalReason pRemovalReason, CallbackInfo ci) {
        if ((Object) this instanceof IFumetsu fumetsu) {
            if (FumetsuHandler.specialRemove.get() || ((IEntityNovel) fumetsu).hyperdaimc$isNovelized()) {
                this.hyperdaimc$extinction(pRemovalReason);
            }
        }
    }

    @Inject(method = "setLevelCallback", at = @At("HEAD"), cancellable = true)
    private void setLevelCallbackFumetsu(EntityInLevelCallback pLevelCallback, CallbackInfo ci) {
        if (this instanceof IFumetsu) {
            ci.cancel();
            this.levelCallback2 = pLevelCallback;
//            Deets.LOG.debug("setLevelCallbackFumetsu");
        }
    }

    @Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;", at = @At("HEAD"))
    private void changeDimension$HEAD(ServerLevel pDestination, CallbackInfoReturnable<Entity> cir) {
        FumetsuHandler.specialRemove.set(true);
    }

    @Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;", at = @At("RETURN"))
    private void changeDimension$RETURN(ServerLevel pDestination, CallbackInfoReturnable<Entity> cir) {
        FumetsuHandler.specialRemove.set(false);
    }

    @Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;removeAfterChangingDimensions()V", remap = true), remap = false)
    private void changeDimension$BEFORE(ServerLevel p_20118_, ITeleporter teleporter, CallbackInfoReturnable<Entity> cir) {
        FumetsuHandler.specialRemove.set(true);
    }

    @Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/world/entity/Entity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;removeAfterChangingDimensions()V", shift = At.Shift.AFTER, remap = true), remap = false)
    private void changeDimension$AFTER(ServerLevel p_20118_, ITeleporter teleporter, CallbackInfoReturnable<Entity> cir) {
        FumetsuHandler.specialRemove.set(false);
    }
}
