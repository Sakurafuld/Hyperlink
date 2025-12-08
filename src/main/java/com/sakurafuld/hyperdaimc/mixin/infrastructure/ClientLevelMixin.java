package com.sakurafuld.hyperdaimc.mixin.infrastructure;

import com.sakurafuld.hyperdaimc.infrastructure.mixin.MixinLevelTickEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
@OnlyIn(Dist.CLIENT)
public abstract class ClientLevelMixin {
    @Inject(method = "tickEntities", at = @At("RETURN"))
    private void tickEntitiesAPI(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new MixinLevelTickEvent((ClientLevel) (Object) this));
    }
}
