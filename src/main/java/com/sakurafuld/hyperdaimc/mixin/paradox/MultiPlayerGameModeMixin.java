package com.sakurafuld.hyperdaimc.mixin.paradox;

import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
@OnlyIn(Dist.CLIENT)
public abstract class MultiPlayerGameModeMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract boolean destroyBlock(BlockPos pPos);

    @Shadow
    private int destroyDelay;

    @Shadow
    private boolean isDestroying;

    @Shadow
    protected abstract boolean sameDestroyTarget(BlockPos pPos);

    @Shadow
    private float destroyProgress;

    @Shadow
    private BlockPos destroyBlockPos;

    @Shadow
    private ItemStack destroyingItem;

    @Shadow
    private float destroyTicks;

    @Shadow
    public abstract boolean startDestroyBlock(BlockPos pLoc, Direction pFace);

    @Shadow
    protected abstract void ensureHasSentCarriedItem();

    @Shadow
    @Final
    private ClientPacketListener connection;

    @Shadow
    protected abstract void startPrediction(ClientLevel pLevel, PredictiveAction pAction);

    @Unique
    private boolean playerDestroy = false;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void destroyBlockParadox$HEAD(BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        if (!HyperServerConfig.CHRONICLE_OWNER.get() && ChronicleHandler.isPaused(this.minecraft.level, pPos, null) && !ChronicleHandler.isPaused(this.minecraft.level, pPos, this.minecraft.player)) {
            this.playerDestroy = true;
            ParadoxHandler.gashaconPlayer = this.minecraft.player;
        }
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void destroyBlockParadox$RETURN(BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        if (!HyperServerConfig.CHRONICLE_OWNER.get() && ChronicleHandler.isPaused(this.minecraft.level, pPos, null) && !ChronicleHandler.isPaused(this.minecraft.level, pPos, this.minecraft.player)) {
            if (this.playerDestroy) {
                this.playerDestroy = false;
                ParadoxHandler.gashaconPlayer = null;
            }
        }
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void startDestroyBlockParadox(BlockPos pLoc, Direction pFace, CallbackInfoReturnable<Boolean> cir) {
        if (HyperServerConfig.CHRONICLE_PARADOX.get()) {
            return;
        }
        if (ParadoxHandler.hasParadox(this.minecraft.player) && ChronicleHandler.isPaused(this.minecraft.level, pLoc, this.minecraft.player)) {
            cir.setReturnValue(true);
            if (!this.isDestroying || !this.sameDestroyTarget(pLoc)) {
                if (this.isDestroying) {
                    this.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, pFace));
                }

                BlockState state = this.minecraft.level.getBlockState(pLoc);
                this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, pLoc, state, 0);
                this.startPrediction(this.minecraft.level, prediction -> {
                    boolean noAir = !state.isAir();
                    if (noAir && this.destroyProgress == 0) {
                        state.attack(this.minecraft.level, pLoc, this.minecraft.player);
                    }

                    ServerboundPlayerActionPacket packet = new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pLoc, pFace, prediction);
                    if (noAir && state.getDestroyProgress(this.minecraft.player, this.minecraft.player.level(), pLoc) >= 1) {
                        this.destroyBlock(pLoc);
                    } else {
                        this.isDestroying = true;
                        this.destroyBlockPos = pLoc;
                        this.destroyingItem = this.minecraft.player.getMainHandItem();
                        this.destroyProgress = 0;
                        this.destroyTicks = 0;
                        this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, (int) (this.destroyProgress * 10) - 1);
                    }

                    return packet;
                });
                boolean noAir = !state.isAir();
                if (noAir && this.destroyProgress == 0) {
                    state.attack(this.minecraft.level, pLoc, this.minecraft.player);
                }

                if (noAir && state.getDestroyProgress(this.minecraft.player, this.minecraft.player.level(), pLoc) >= 1) {
                    this.destroyBlock(pLoc);
                } else {
                    this.isDestroying = true;
                    this.destroyBlockPos = pLoc;
                    this.destroyingItem = this.minecraft.player.getMainHandItem();
                    this.destroyProgress = 0;
                    this.destroyTicks = 0;
                    this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, (int) (this.destroyProgress * 10) - 1);
                }
            }
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void continueDestroyBlockParadox(BlockPos pPosBlock, Direction pDirectionFacing, CallbackInfoReturnable<Boolean> cir) {
        if (HyperServerConfig.CHRONICLE_PARADOX.get()) {
            return;
        }
        if (ParadoxHandler.hasParadox(this.minecraft.player) && ChronicleHandler.isPaused(this.minecraft.level, pPosBlock, this.minecraft.player)) {
            this.ensureHasSentCarriedItem();
            if (this.destroyDelay > 0) {
                --this.destroyDelay;
                cir.setReturnValue(true);
            } else if (this.sameDestroyTarget(pPosBlock)) {
                BlockState blockstate = this.minecraft.level.getBlockState(pPosBlock);
                if (blockstate.isAir()) {
                    this.isDestroying = false;
                    cir.setReturnValue(false);
                } else {
                    this.destroyProgress += blockstate.getDestroyProgress(this.minecraft.player, this.minecraft.player.level(), pPosBlock);
                    if (this.destroyTicks % 4 == 0) {
                        SoundType soundtype = blockstate.getSoundType(this.minecraft.level, pPosBlock, this.minecraft.player);
                        this.minecraft.getSoundManager().play(new SimpleSoundInstance(soundtype.getHitSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, SoundInstance.createUnseededRandom(), pPosBlock));
                    }

                    ++this.destroyTicks;
                    this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, pPosBlock, blockstate, Mth.clamp(this.destroyProgress, 0.0F, 1.0F));

                    if (this.destroyProgress >= 1) {
                        this.isDestroying = false;
                        this.startPrediction(this.minecraft.level, prediction -> {
                            this.destroyBlock(pPosBlock);
                            return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pPosBlock, pDirectionFacing, prediction);
                        });
                        this.destroyProgress = 0.0F;
                        this.destroyTicks = 0.0F;
                        this.destroyDelay = 5;
                    }
                    this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, (int) (this.destroyProgress * 10.0F) - 1);
                    cir.setReturnValue(true);
                }
            } else {
                cir.setReturnValue(this.startDestroyBlock(pPosBlock, pDirectionFacing));
            }
        }
    }
}
