package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Shadow
    public abstract boolean isClientSide();

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"), cancellable = true)
    private void setBlockChronicle$HEAD(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft, CallbackInfoReturnable<Boolean> cir) {
        Level self = (Level) ((Object) this);
        if (self.isClientSide()) {
            ChronicleHandler.clientForceNonPaused = true;
        }
        if (!pState.is(self.getBlockState(pPos).getBlock()) &&
                ChronicleHandler.isPaused(self, pPos, ParadoxHandler.gashaconPlayer)) {
            LOG.debug("PreventSetL:{}->:{}", self.getBlockState(pPos), pState);
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("RETURN"))
    private void setBlockChronicle$RETURN(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (this.isClientSide()) {
            ChronicleHandler.clientForceNonPaused = false;
        }
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void destroyBlockChronicle(BlockPos pPos, boolean pDropBlock, Entity pEntity, int pRecursionLeft, CallbackInfoReturnable<Boolean> cir) {
        Level self = (Level) ((Object) this);
        if (ChronicleHandler.isPaused(self, pPos, ParadoxHandler.gashaconPlayer)) {
            cir.setReturnValue(false);
        }
    }
}
