package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Unique
    private Player lastDestroyingPlayer = null;

    @Shadow
    protected abstract BlockState asState();

    @Inject(method = "getDestroyProgress", at = @At("HEAD"), cancellable = true)
    private void getDestroyProgressChronicle(Player pPlayer, BlockGetter pLevel, BlockPos pPos, CallbackInfoReturnable<Float> cir) {
        this.lastDestroyingPlayer = pPlayer;
        if (pLevel instanceof Level level && ChronicleHandler.isPaused(level, pPos, pPlayer)) {
            if (!HyperCommonConfig.CHRONICLE_PARADOX.get() && ParadoxHandler.hasParadox(pPlayer)) {
                cir.setReturnValue(0.005f);
            } else {
                cir.setReturnValue(0f);
            }
        }
    }

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void getDestroySpeedChronicle(BlockGetter pLevel, BlockPos pPos, CallbackInfoReturnable<Float> cir) {
        Player player = this.lastDestroyingPlayer;
        if (pLevel instanceof Level level && level.isClientSide() && player == null) {
            player = jade();
        }
        if (pLevel instanceof Level level && ChronicleHandler.isPaused(level, pPos, player)) {
            cir.setReturnValue(-1f);
        }
    }

    @Inject(method = "canBeReplaced(Lnet/minecraft/world/item/context/BlockPlaceContext;)Z", at = @At("HEAD"), cancellable = true)
    private void canBeReplacedChronicle(BlockPlaceContext pUseContext, CallbackInfoReturnable<Boolean> cir) {
        if (ChronicleHandler.isPaused(pUseContext.getLevel(), pUseContext.getClickedPos(), pUseContext.getPlayer())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "spawnAfterBreak", at = @At("HEAD"), cancellable = true)
    private void spawnAfterBreakChronicle(ServerLevel pLevel, BlockPos pPos, ItemStack pStack, boolean pDropExperience, CallbackInfo ci) {
        if (ChronicleHandler.isPaused(pLevel, pPos, ParadoxHandler.gashaconPlayer)) {
            ci.cancel();
        }
    }

    @Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
    public void onPlaceChronicle(Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving, CallbackInfo ci) {
        if (!this.asState().is(pOldState.getBlock()) && ChronicleHandler.isPaused(pLevel, pPos, ParadoxHandler.gashaconPlayer)) {
            ci.cancel();
        }
    }

    @Inject(method = "onRemove", at = @At("HEAD"), cancellable = true)
    public void onRemoveChronicle(Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving, CallbackInfo ci) {
        if (!this.asState().is(pNewState.getBlock()) && ChronicleHandler.isPaused(pLevel, pPos, ParadoxHandler.gashaconPlayer)) {
            ci.cancel();
        }
    }

    @Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
    public void updateShapeChronicle(Direction pDirection, BlockState pQueried, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pOffsetPos, CallbackInfoReturnable<BlockState> cir) {
        if (pLevel instanceof Level level && !cir.getReturnValue().is(this.asState().getBlock()) && ChronicleHandler.isPaused(level, pCurrentPos, null)) {
            cir.setReturnValue(this.asState());
        }
    }

    @Unique
    @OnlyIn(Dist.CLIENT)
    private static Player jade() {
        return Minecraft.getInstance().player;
    }
}
