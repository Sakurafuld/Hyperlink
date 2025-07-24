package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "getMaxHealth", at = @At("HEAD"), cancellable = true)
    private void getMaxHealthFumetsu(CallbackInfoReturnable<Float> cir) {
        if (FumetsuEntity.class.equals(this.getClass())) {
            cir.setReturnValue(Float.valueOf(HyperServerConfig.FUMETSU_HEALTH.get()));
        }
    }
}
