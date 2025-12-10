package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.system.ChronicleHandler;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.chronicle.ClientboundChronicleHitEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "mayInteract", at = @At("HEAD"), cancellable = true)
    private void mayInteractChronicle(Player pPlayer, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        ServerLevel self = (ServerLevel) (Object) this;
        if (HyperCommonConfig.CHRONICLE_INTERACT.get() && ChronicleHandler.isPaused(self, pPos, pPlayer)) {
            cir.setReturnValue(false);
            HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> self.getChunkAt(pPos)), new ClientboundChronicleHitEffect(pPos));
        }
    }
}
