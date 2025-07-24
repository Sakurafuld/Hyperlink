package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "blockActionRestricted", at = @At("HEAD"), cancellable = true)
    private void blockActionRestrictedChronicle(Level pLevel, BlockPos pPos, GameType pGameMode, CallbackInfoReturnable<Boolean> cir) {
        if (HyperServerConfig.CHRONICLE_INTERACT.get() && ChronicleHandler.isPaused(pLevel, pPos, (Player) ((Object) this))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mayUseItemAt", at = @At("HEAD"), cancellable = true)
    private void mayUseItemAt(BlockPos pPos, Direction pFacing, ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
        Player self = (Player) ((Object) this);
        if (ChronicleHandler.isPaused(self.getLevel(), pPos, self)) {
            cir.setReturnValue(false);
        }
    }
}
