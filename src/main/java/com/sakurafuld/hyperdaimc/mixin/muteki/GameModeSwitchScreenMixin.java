package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.network.PacketHandler;
import com.sakurafuld.hyperdaimc.network.muteki.ServerboundSpecialGameModeSwitch;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameModeSwitcherScreen.class)
public abstract class GameModeSwitchScreenMixin {
    @Redirect(method = "switchToHoveredGameMode(Lnet/minecraft/client/Minecraft;Ljava/util/Optional;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;chat(Ljava/lang/String;)V"))
    private static void switchToHoveredGameModeMuteki(LocalPlayer instance, String pMessage) {
        if (MutekiHandler.muteki(instance)) {
            PacketHandler.INSTANCE.sendToServer(new ServerboundSpecialGameModeSwitch(pMessage));
        } else {
            instance.chat(pMessage);
        }
    }
}
