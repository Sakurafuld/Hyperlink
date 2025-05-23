package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.content.chronicle.ChronicleHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBaseBlockMixin {
    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private static void isPushableChronicle(BlockState pBlockState, Level pLevel, BlockPos pPos, Direction pFacing, boolean pDestroyBlocks, Direction pPistonDirection, CallbackInfoReturnable<Boolean> cir) {
        if (ChronicleHandler.isPaused(pLevel, pPos, null)) {
            cir.setReturnValue(false);
        } else if (!pLevel.getBlockState(pPos).isAir() && ChronicleHandler.isPaused(pLevel, pPos.relative(pFacing), null)) {
            cir.setReturnValue(false);
        }
    }
}
