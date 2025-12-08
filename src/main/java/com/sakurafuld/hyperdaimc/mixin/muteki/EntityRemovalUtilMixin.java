package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.infrastructure.entity.IFumetsu;
import flashfur.omnimobs.util.EntityRemovalUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(EntityRemovalUtil.class)
public abstract class EntityRemovalUtilMixin {
    @Inject(method = "deleteEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private static void deleteEntityMuteki(Entity entity, Level level, boolean onRemoved, CallbackInfo ci) {
        if (entity instanceof IFumetsu || (entity instanceof LivingEntity living && MutekiHandler.muteki(living))) {
            entity.hurt(entity.damageSources().generic(), 1);
            ci.cancel();
        }
    }
}
