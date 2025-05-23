package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.content.chronicle.ChronicleHandler;
import com.sakurafuld.hyperdaimc.content.paradox.ParadoxHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    private void setBlockStateChronicle(BlockPos pPos, BlockState pState, boolean pIsMoving, CallbackInfoReturnable<BlockState> cir) {
        LevelChunk self = (LevelChunk) ((Object) this);
        if (!pState.is(self.getBlockState(pPos).getBlock()) &&
                ChronicleHandler.isPaused(self.getLevel(), pPos, ParadoxHandler.gashaconPlayer)) {
            LOG.debug("PreventSetLC:{}->:{}", self.getBlockState(pPos), pState);
            cir.setReturnValue(null);
        }
    }
}
