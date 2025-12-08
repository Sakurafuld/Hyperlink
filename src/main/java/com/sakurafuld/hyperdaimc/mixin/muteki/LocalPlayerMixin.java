package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.infrastructure.mixin.ILivingEntityMuteki;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
@OnlyIn(Dist.CLIENT)
public abstract class LocalPlayerMixin implements ILivingEntityMuteki {
    @Inject(method = "hurtTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setHealth(F)V"))
    private void hurtToMuteki$BEFORE(float pHealth, CallbackInfo ci) {
        this.hyperdaimc$mutekiForce(true);
    }

    @Inject(method = "hurtTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setHealth(F)V", shift = At.Shift.AFTER))
    private void hurtToMuteki$AFTER(float pHealth, CallbackInfo ci) {
        this.hyperdaimc$mutekiForce(false);
        this.hyperdaimc$mutekiSetLocal(pHealth);
    }
}
