package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.api.mixin.IPersistentEntityManagerFumetsu;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.helper.Deets;
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

@Mixin(targets = "net.minecraft.world.level.entity.PersistentEntitySectionManager$Callback")
public abstract class PersistentEntitySectionManager$CallbackMixin<T extends EntityAccess> {

    @Shadow(remap = false)
    @Final
    private Entity realEntity;

    @Shadow
    @Final
    PersistentEntitySectionManager<T> this$0;

    @Shadow
    @Final
    private T entity;

    @Inject(method = "onRemove", at = @At("HEAD"), cancellable = true)
    private void onRemoveFumetsu$0(Entity.RemovalReason pReason, CallbackInfo ci) {
        if (FumetsuHandler.specialRemove.get()) {
            return;
        }
        if (this.realEntity != null && !NovelHandler.novelized(this.realEntity) && (this.realEntity instanceof IFumetsu || (this.realEntity instanceof LivingEntity living && MutekiHandler.muteki(living)))) {
            Deets.LOG.debug("onRemoveFumetsuCancel");
            ci.cancel();
        }
    }

    @Inject(method = "onRemove", at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"))
    private void onRemoveFumetsu$1(Entity.RemovalReason pReason, CallbackInfo ci) {
        if (this.entity instanceof IFumetsu) {
            if (FumetsuHandler.specialRemove.get() || NovelHandler.novelized((Entity) this.entity)) {
//                Deets.LOG.debug("removeEntityUuidFumetsu");
                ((IPersistentEntityManagerFumetsu) this.this$0).known().remove(this.entity.getUUID());
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
