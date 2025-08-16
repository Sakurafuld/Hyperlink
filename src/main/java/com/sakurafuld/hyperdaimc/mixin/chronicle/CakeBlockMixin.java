package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CakeBlock.class)
public abstract class CakeBlockMixin {
    @Shadow
    @Final
    public static IntegerProperty BITES;

    @Inject(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/resources/ResourceLocation;)V"), cancellable = true)
    private static void eatChronicle$BEFORE(LevelAccessor pLevel, BlockPos pPos, BlockState pState, Player pPlayer, CallbackInfoReturnable<InteractionResult> cir) {
        if (pLevel instanceof Level level && pState.getValue(BITES) >= 6) {
            if (ChronicleHandler.isPaused(level, pPos, pPlayer)) {
                cir.setReturnValue(InteractionResult.PASS);
            } else if (!HyperCommonConfig.CHRONICLE_OWNER.get() && ChronicleHandler.isPaused(level, pPos, null)) {
                ParadoxHandler.gashaconPlayer = pPlayer;
            }
        }
    }

    @Inject(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z", shift = At.Shift.AFTER))
    private static void eatChronicle$AFTER(LevelAccessor pLevel, BlockPos pPos, BlockState pState, Player pPlayer, CallbackInfoReturnable<InteractionResult> cir) {
        if (pLevel instanceof Level level && pState.getValue(BITES) >= 6 && !HyperCommonConfig.CHRONICLE_OWNER.get() && ChronicleHandler.isPaused(level, pPos, null) && !ChronicleHandler.isPaused(level, pPos, pPlayer)) {
            ParadoxHandler.gashaconPlayer = null;
        }
    }
}
