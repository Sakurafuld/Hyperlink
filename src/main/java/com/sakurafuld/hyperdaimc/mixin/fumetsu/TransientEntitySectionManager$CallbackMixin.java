package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.helper.Deets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.entity.TransientEntitySectionManager$Callback")
public abstract class TransientEntitySectionManager$CallbackMixin {

    @Shadow(remap = false)
    @Final
    private Entity realEntity;

    @Inject(method = "onRemove", at = @At("HEAD"), cancellable = true)
    private void onRemoveFumetsu(Entity.RemovalReason pReason, CallbackInfo ci) {
        if (this.realEntity != null) {
            if (FumetsuHandler.specialRemove.get()) {
                return;
            }
            if (!NovelHandler.novelized(this.realEntity) && (this.realEntity instanceof IFumetsu || (this.realEntity instanceof LivingEntity living && MutekiHandler.muteki(living)))) {
                Deets.LOG.debug("onRemoveFumetsuCancel");
                ci.cancel();
            }
        }
    }

    @Inject(method = "onMove", at = @At("HEAD"))
    private void onMoveFumetsu$HEAD(CallbackInfo ci) {
        FumetsuHandler.specialRemove.set(true);
    }

    @Inject(method = "onMove", at = @At("RETURN"))
    private void onMoveFumetsu$RETURN(CallbackInfo ci) {
        FumetsuHandler.specialRemove.set(false);
    }
}
