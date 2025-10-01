package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.wzz.witherzilla.util.WitherzillaUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(WitherzillaUtil.class)
public abstract class WitherillaUtilMixin {
    @Inject(method = "forceRemoveEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private static void forceRemoveEntityMuteki(Entity entity, CallbackInfo ci) {
        if (entity instanceof IFumetsu || (entity instanceof LivingEntity living && MutekiHandler.muteki(living))) {
            entity.hurt(entity.damageSources().generic(), 1);
            ci.cancel();
        }
    }
}
