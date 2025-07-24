package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import flashfur.omnimobs.entities.metapotent_flashfur.MetapotentFlashfur;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(MetapotentFlashfur.class)
public abstract class MetapotentFlashfurMixin {
    @Inject(method = "instantKill", at = @At("HEAD"), cancellable = true, remap = false)
    private void instantKillMuteki(LivingEntity entity, float knockback, CallbackInfo ci) {
        if (MutekiHandler.muteki(entity)) {
            if (FumetsuEntity.class.equals(entity.getClass())) {
                entity.hurt(entity.damageSources().generic(), 1);
            }
            ci.cancel();
        }
    }

    @Inject(method = "eraseEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private void eraseEntityMuteki(Entity entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity living && MutekiHandler.muteki(living)) {
            ci.cancel();
        }
    }
}
