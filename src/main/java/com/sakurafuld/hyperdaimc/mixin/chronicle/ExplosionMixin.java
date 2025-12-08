package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.chronicle.ClientboundChronicleHitEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Set;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow
    @Final
    private Level level;

    @Shadow
    public abstract boolean interactsWithBlocks();

    @Inject(locals = LocalCapture.CAPTURE_FAILSOFT, method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ExplosionDamageCalculator;getBlockExplosionResistance(Lnet/minecraft/world/level/Explosion;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Ljava/util/Optional;"))
    private void explodeChronicle(CallbackInfo ci, Set<BlockPos> set, int i, int j, int k, int l, double d0, double d1, double d2, double d3, float f, double d4, double d6, double d8, float f1, BlockPos blockpos, BlockState blockstate, FluidState fluidstate) {
        if (this.interactsWithBlocks() && !(blockstate.isAir() && fluidstate.isEmpty()) && ChronicleHandler.isPaused(this.level, blockpos, null)) {
            if (!this.level.isClientSide())
                HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> this.level.getChunkAt(blockpos)), new ClientboundChronicleHitEffect(blockpos));
            set.remove(blockpos);
        }
    }
}
