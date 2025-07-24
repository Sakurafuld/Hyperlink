package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuItem;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "setPosRaw", at = @At("HEAD"), cancellable = true)
    private void setPosRawFumetsu(double pX, double pY, double pZ, CallbackInfo ci) {
        if ((Object) this instanceof IFumetsu fumetsu && !fumetsu.isMovable() && !FumetsuItem.spawn) {
            ci.cancel();
        }
    }
}
