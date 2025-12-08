package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Inject(method = "handleClientCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;", ordinal = 0))
    private void handleClientCommandMuteki$BEFORE(CallbackInfo ci) {
        FumetsuHandler.specialRemove.set(true);
    }

    @Inject(method = "handleClientCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;", ordinal = 0, shift = At.Shift.AFTER))
    private void handleClientCommandMuteki$AFTER(CallbackInfo ci) {
        FumetsuHandler.specialRemove.set(false);
    }
}
