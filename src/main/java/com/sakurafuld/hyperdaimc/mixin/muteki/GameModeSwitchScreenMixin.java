package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.muteki.ServerboundSpecialGameModeSwitch;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameModeSwitcherScreen.class)
@OnlyIn(Dist.CLIENT)
public abstract class GameModeSwitchScreenMixin {
    @Redirect(method = "switchToHoveredGameMode(Lnet/minecraft/client/Minecraft;Ljava/util/Optional;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;chat(Ljava/lang/String;)V"))
    private static void switchToHoveredGameModeMuteki(LocalPlayer instance, String pMessage) {
        if (MutekiHandler.muteki(instance)) {
            HyperConnection.INSTANCE.sendToServer(new ServerboundSpecialGameModeSwitch(pMessage));
        } else {
            instance.chat(pMessage);
        }
    }
}
