package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.infrastructure.entity.IFumetsu;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityNovel;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IPersistentEntityManagerFumetsu;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.LOG;

@Mixin(targets = "net.minecraft.world.level.entity.PersistentEntitySectionManager$Callback")
public abstract class PersistentEntitySectionManager$CallbackMixin<T extends EntityAccess> {

    @Shadow
    @Final
    PersistentEntitySectionManager<T> this$0;
    @Shadow(remap = false)
    @Final
    private Entity realEntity;
    @Shadow
    @Final
    private T entity;

    @Inject(method = "onRemove", at = @At("HEAD"), cancellable = true)
    private void onRemoveFumetsu$0(Entity.RemovalReason pReason, CallbackInfo ci) {
        if (FumetsuHandler.specialRemove.get()) {
            return;
        }
        if (this.realEntity != null && !((IEntityNovel) this.realEntity).hyperdaimc$isNovelized() && (this.realEntity instanceof IFumetsu || (this.realEntity instanceof LivingEntity living && MutekiHandler.muteki(living)))) {
//            if (this.realEntity instanceof Player) {
//                LOG.debug("RemoveMutekiPlayer");
//                return;
//            }
            LOG.debug("onRemoveFumetsuCancel");
            ci.cancel();
        }
    }

    @Inject(method = "onRemove", at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"))
    private void onRemoveFumetsu$1(Entity.RemovalReason pReason, CallbackInfo ci) {
        if (this.entity instanceof IFumetsu) {
            if (FumetsuHandler.specialRemove.get() || ((IEntityNovel) this.entity).hyperdaimc$isNovelized()) {
//                Deets.LOG.debug("removeEntityUuidFumetsu");
                ((IPersistentEntityManagerFumetsu) this.this$0).hyperdaimc$fumetsuKnown().remove(this.entity.getUUID());
            }
        }
    }

    @Inject(method = "onMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/EntitySection;remove(Lnet/minecraft/world/level/entity/EntityAccess;)Z"))
    private void onMoveFumetsu$BEFORE(CallbackInfo ci) {
        FumetsuHandler.specialRemove.set(true);
    }

    @Inject(method = "onMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/EntitySection;remove(Lnet/minecraft/world/level/entity/EntityAccess;)Z", shift = At.Shift.AFTER))
    private void onMoveFumetsu$AFTER(CallbackInfo ci) {
        FumetsuHandler.specialRemove.set(false);
    }

    @Inject(method = "updateStatus", at = @At("HEAD"))
    private void processUnloadsFumetsu$HEAD(CallbackInfo ci) {
        FumetsuHandler.specialRemove.set(true);
    }

    @Inject(method = "updateStatus", at = @At("RETURN"))
    private void processUnloadsFumetsu$RETURN(CallbackInfo ci) {
        FumetsuHandler.specialRemove.set(false);
    }
}
