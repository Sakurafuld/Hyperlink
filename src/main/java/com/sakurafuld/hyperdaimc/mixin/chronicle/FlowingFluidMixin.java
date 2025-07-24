package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {
    @Inject(method = "canSpreadTo", at = @At("HEAD"), cancellable = true)
    private void canSpreadToChronicle(BlockGetter pLevel, BlockPos pFromPos, BlockState pFromBlockState, Direction pDirection, BlockPos pToPos, BlockState pToBlockState, FluidState pToFluidState, Fluid pFluid, CallbackInfoReturnable<Boolean> cir) {
        if (pLevel instanceof Level level && (ChronicleHandler.isPaused(level, pFromPos, null) || ChronicleHandler.isPaused(level, pToPos, null))) {
            cir.setReturnValue(false);
        }
    }
}
