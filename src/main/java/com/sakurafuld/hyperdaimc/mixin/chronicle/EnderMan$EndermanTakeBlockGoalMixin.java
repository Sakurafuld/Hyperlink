package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanTakeBlockGoal")
public abstract class EnderMan$EndermanTakeBlockGoalMixin {
    @Shadow
    @Final
    private EnderMan enderman;

    @Inject(locals = LocalCapture.CAPTURE_FAILSOFT, method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"), cancellable = true)
    private void tickChronicle(CallbackInfo ci, RandomSource randomsource, Level level, int i, int j, int k, BlockPos blockpos, BlockState blockstate, Vec3 vec3, Vec3 vec31, BlockHitResult blockhitresult, boolean flag) {
        if (ChronicleHandler.isPaused(level, blockpos, this.enderman))
            ci.cancel();
    }
}
