package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "mayInteract", at = @At("HEAD"), cancellable = true)
    private void mayInteractChronicle(Player pPlayer, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        if (HyperServerConfig.CHRONICLE_INTERACT.get() && ChronicleHandler.isPaused((Level) ((Object) this), pPos, pPlayer)) {
            cir.setReturnValue(false);
        }
    }
}
