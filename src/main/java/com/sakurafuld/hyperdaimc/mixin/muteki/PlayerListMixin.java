package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.system.NovelHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "respawn", at = @At("HEAD"), cancellable = true)
    private void respawnMuteki(ServerPlayer pPlayer, boolean pKeepEverything, CallbackInfoReturnable<ServerPlayer> cir) {
        if (!FumetsuHandler.isSpecialRemoving() && !NovelHandler.novelized(pPlayer) && MutekiHandler.muteki(pPlayer)) {
            cir.setReturnValue(pPlayer);
            for (float i = 1; pPlayer.getHealth() <= 0 && i <= 512; i++)
                pPlayer.setHealth(i);
        }
    }
}
