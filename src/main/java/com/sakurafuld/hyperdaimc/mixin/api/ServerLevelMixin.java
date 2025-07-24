package com.sakurafuld.hyperdaimc.mixin.api;

import com.sakurafuld.hyperdaimc.api.mixin.MixinLevelTickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "tick", at = @At("RETURN"))
    private void tickAPI(BooleanSupplier pHasTimeLeft, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new MixinLevelTickEvent((ServerLevel) (Object) this));
    }
}
