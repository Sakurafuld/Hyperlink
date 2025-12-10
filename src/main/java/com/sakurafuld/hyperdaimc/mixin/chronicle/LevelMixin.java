package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.content.hyper.chronicle.system.ChronicleHandler;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.system.ParadoxHandler;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.chronicle.ClientboundChronicleHitEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Shadow
    public abstract boolean isClientSide();

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"), cancellable = true)
    private void setBlockChronicle$HEAD(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft, CallbackInfoReturnable<Boolean> cir) {
        Level self = (Level) (Object) this;
        if (!pState.is(self.getBlockState(pPos).getBlock()) && ChronicleHandler.isPaused(self, pPos, ParadoxHandler.gashaconPlayer)) {
//            LOG.debug("PreventSetL:{}->:{}", self.getBlockState(pPos), pState);
            if (!self.isClientSide()) {
                cir.setReturnValue(false);
                HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> self.getChunkAt(pPos)), new ClientboundChronicleHitEffect(pPos));
            } else {
                ChronicleHandler.clientForceNonPaused = true;
                ChronicleHandler.hitEffect(pPos);
            }
        }
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("RETURN"))
    private void setBlockChronicle$RETURN(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (this.isClientSide())
            ChronicleHandler.clientForceNonPaused = false;
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void destroyBlockChronicle(BlockPos pPos, boolean pDropBlock, Entity pEntity, int pRecursionLeft, CallbackInfoReturnable<Boolean> cir) {
        Level self = (Level) (Object) this;
        if (ChronicleHandler.isPaused(self, pPos, ParadoxHandler.gashaconPlayer)) {
            cir.setReturnValue(false);
            if (self.isClientSide())
                ChronicleHandler.hitEffect(pPos);
            else
                HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> self.getChunkAt(pPos)), new ClientboundChronicleHitEffect(pPos));
        }
    }
}
