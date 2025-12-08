package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.muteki.ServerboundSpecialGameModeSwitch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameModeSwitcherScreen.class)
@OnlyIn(Dist.CLIENT)
public abstract class GameModeSwitchScreenMixin {
    @Inject(method = "switchToHoveredGameMode(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/debug/GameModeSwitcherScreen$GameModeIcon;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendUnsignedCommand(Ljava/lang/String;)Z"), cancellable = true)
    private static void switchToHoveredGameModeMuteki(Minecraft pMinecraft, GameModeSwitcherScreen.GameModeIcon pGameModeIcon, CallbackInfo ci) {
        if (MutekiHandler.muteki(pMinecraft.player)) {
            ci.cancel();
            GameType mode = switch (pGameModeIcon) {
                case CREATIVE -> GameType.CREATIVE;
                case SURVIVAL -> GameType.SURVIVAL;
                case ADVENTURE -> GameType.ADVENTURE;
                default -> GameType.SPECTATOR;
            };
            HyperConnection.INSTANCE.sendToServer(new ServerboundSpecialGameModeSwitch(mode));
        }
    }
}
