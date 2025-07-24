package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.api.mixin.ILevelChunkSectionChronicle;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleSavedData;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionMixin implements ILevelChunkSectionChronicle {
    @Shadow
    public abstract BlockState getBlockState(int pX, int pY, int pZ);

    @Unique
    private ChunkAccess chunk = null;

    @Inject(method = "setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("HEAD"), cancellable = true)
    private void setBlockStateChronicle(int pX, int pY, int pZ, BlockState pState, boolean pUseLocks, CallbackInfoReturnable<BlockState> cir) {
        if (this.chunk != null && this.chunk.getWorldForge() instanceof Level level) {
            BlockState current = this.getBlockState(pX, pY, pZ);
            if (pState.is(current.getBlock())) {
                return;
            }

            ChronicleSavedData.get(level).getPaused((LevelChunkSection) ((Object) this)).ifPresent(map -> {
                long pos = map.get(pX << 8 | pY << 4 | pZ);
                if (pos != Long.MIN_VALUE && ChronicleHandler.isPaused(level, BlockPos.of(pos), ParadoxHandler.gashaconPlayer)) {
                    cir.setReturnValue(current);
                }
            });
        }
    }

    @Override
    public void setChunk(ChunkAccess chunk) {
        this.chunk = chunk;
    }
}
