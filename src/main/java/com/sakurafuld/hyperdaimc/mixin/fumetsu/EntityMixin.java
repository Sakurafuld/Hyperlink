package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.api.mixin.IEntityFumetsu;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.helper.Deets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityFumetsu {
    @Unique
    private EntityInLevelCallback levelCallback2 = EntityInLevelCallback.NULL;

    @Override
    public void fumetsuExtinction(Entity.RemovalReason reason) {
        Deets.LOG.info("callbackRemoveFumetsu:{}", Arrays.stream(Thread.currentThread().getStackTrace()).skip(2).limit(10).toList());
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
            if (FumetsuHandler.specialRemove.get() || NovelHandler.novelized((Entity) fumetsu)) {
                this.fumetsuExtinction(pRemovalReason);
            }
        }
    }

    @Inject(method = "setLevelCallback", at = @At("HEAD"), cancellable = true)
    private void setLevelCallbackFumetsu(EntityInLevelCallback pLevelCallback, CallbackInfo ci) {
        if (this instanceof IFumetsu) {
            ci.cancel();
            this.levelCallback2 = pLevelCallback;
            Deets.LOG.info("setLevelCallbackFumetsu");
        }
    }
}
