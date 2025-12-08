package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.google.common.base.MoreObjects;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.muteki.ServerboundSpecialGameModeSwitch;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "handleDebugKeys", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendUnsignedCommand(Ljava/lang/String;)Z", ordinal = 0), cancellable = true)
    private void handleDebugKeysMuteki$toSpectator(int pKey, CallbackInfoReturnable<Boolean> cir) {
        if (MutekiHandler.muteki(Minecraft.getInstance().player)) {
            HyperConnection.INSTANCE.sendToServer(new ServerboundSpecialGameModeSwitch(GameType.SPECTATOR));
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "handleDebugKeys", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendUnsignedCommand(Ljava/lang/String;)Z", ordinal = 1), cancellable = true)
    private void handleDebugKeysMuteki$fromSpectator(int pKey, CallbackInfoReturnable<Boolean> cir) {
        if (MutekiHandler.muteki(Minecraft.getInstance().player)) {
            HyperConnection.INSTANCE.sendToServer(new ServerboundSpecialGameModeSwitch(MoreObjects.firstNonNull(Minecraft.getInstance().gameMode.getPreviousPlayerMode(), GameType.CREATIVE)));
            cir.setReturnValue(true);
        }
    }
}
