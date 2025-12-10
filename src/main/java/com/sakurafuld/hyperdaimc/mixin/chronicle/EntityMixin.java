package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.system.ChronicleHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "mayInteract", at = @At("HEAD"), cancellable = true)
    private void mayInteractChronicle(Level pLevel, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        //noinspection ConstantValue
        if (HyperCommonConfig.CHRONICLE_INTERACT.get() && ChronicleHandler.isPaused(pLevel, pPos, (Entity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
