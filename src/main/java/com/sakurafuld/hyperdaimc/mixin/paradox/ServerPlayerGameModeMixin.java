package com.sakurafuld.hyperdaimc.mixin.paradox;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockBreakAckPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow
    protected ServerLevel level;

    @Shadow
    public abstract void destroyAndAck(BlockPos pPos, ServerboundPlayerActionPacket.Action pAction, String pReason);

    @Shadow
    private int gameTicks;

    @Shadow
    private int destroyProgressStart;

    @Shadow
    private boolean isDestroyingBlock;

    @Shadow
    private BlockPos destroyPos;

    @Shadow
    private int lastSentState;

    @Shadow
    private boolean hasDelayedDestroy;

    @Shadow
    private BlockPos delayedDestroyPos;

    @Shadow
    private int delayedTickStart;

    @Unique
    private boolean playerDestroy = false;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void destroyBlockParadox(BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        if (!HyperCommonConfig.CHRONICLE_OWNER.get() && ChronicleHandler.isPaused(this.level, pPos, null) && !ChronicleHandler.isPaused(this.level, pPos, this.player)) {
            this.playerDestroy = true;
            ParadoxHandler.gashaconPlayer = this.player;
            LOG.debug("ServerGameModeDestroyStart");
        }
        if (HyperCommonConfig.CHRONICLE_PARADOX.get()) {
            return;
        }
        if (ParadoxHandler.hasParadox(this.player) && ChronicleHandler.isPaused(this.level, pPos, this.player)) {
            ParadoxHandler.perfectKnockout(this.player);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void destroyBlockParadox$RETURN(BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        if (!HyperCommonConfig.CHRONICLE_OWNER.get() && ChronicleHandler.isPaused(this.level, pPos, null) && !ChronicleHandler.isPaused(this.level, pPos, this.player)) {
            if (this.playerDestroy) {
                this.playerDestroy = false;
                ParadoxHandler.gashaconPlayer = null;
//                PacketHandler.INSTANCE.send(PacketDistributor.DIMENSION.with(this.level::dimension), new ClientboundUpdateParadox(pPos, this.level.getBlockState(pPos)));
                LOG.debug("ServerGameModeDestroyEnd");
            }
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"), cancellable = true)
    private void handleBlockBreakActionParadox(BlockPos pPos, ServerboundPlayerActionPacket.Action pAction, Direction pDirection, int maxBuildingHeight, CallbackInfo ci) {
        if (HyperCommonConfig.CHRONICLE_PARADOX.get()) {
            return;
        }
        if (ParadoxHandler.hasParadox(this.player) && ChronicleHandler.isPaused(this.level, pPos, this.player)) {
            if (pAction == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                ci.cancel();

                this.destroyProgressStart = this.gameTicks;
                float progress = 1;
                BlockState state = this.level.getBlockState(pPos);
                if (!state.isAir()) {
                    state.attack(this.level, pPos, this.player);
                    progress = state.getDestroyProgress(this.player, this.player.level, pPos);
                }

                if (!state.isAir() && progress >= 1) {
                    this.destroyAndAck(pPos, pAction, "insta mine");
                } else {
                    if (this.isDestroyingBlock) {
                        this.player.connection.send(new ClientboundBlockBreakAckPacket(this.destroyPos, this.level.getBlockState(this.destroyPos), ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, false, "abort destroying since another started (client insta mine, server disagreed)"));
                    }

                    this.isDestroyingBlock = true;
                    this.destroyPos = pPos.immutable();
                    int actualProgress = (int) (progress * 10);
                    this.level.destroyBlockProgress(this.player.getId(), pPos, actualProgress);
                    this.player.connection.send(new ClientboundBlockBreakAckPacket(pPos, this.level.getBlockState(pPos), pAction, true, "actual start of destroying"));
                    this.lastSentState = actualProgress;
                }
            } else if (pAction == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
                ci.cancel();

                if (pPos.equals(this.destroyPos)) {
                    int j = this.gameTicks - this.destroyProgressStart;
                    BlockState state = this.level.getBlockState(pPos);
                    if (!state.isAir()) {

                        float f1 = state.getDestroyProgress(this.player, this.level, pPos) * (j + 1);
                        if (f1 >= 0.7) {
                            this.isDestroyingBlock = false;
                            this.level.destroyBlockProgress(this.player.getId(), pPos, -1);
                            this.destroyAndAck(pPos, pAction, "destroyed");
                            return;
                        }

                        if (!this.hasDelayedDestroy) {
                            this.isDestroyingBlock = false;
                            this.hasDelayedDestroy = true;
                            this.delayedDestroyPos = pPos;
                            this.delayedTickStart = this.destroyProgressStart;
                        }
                    }
                }

                this.player.connection.send(new ClientboundBlockBreakAckPacket(pPos, this.level.getBlockState(pPos), pAction, true, "stopped destroying"));
            }
        }
    }
}
