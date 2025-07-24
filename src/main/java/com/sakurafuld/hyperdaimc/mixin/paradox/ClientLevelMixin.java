package com.sakurafuld.hyperdaimc.mixin.paradox;

import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
@OnlyIn(Dist.CLIENT)
public abstract class ClientLevelMixin {
    @Inject(method = "setKnownState", at = @At("HEAD"))
    private void setKnownStateParadox$HEAD(BlockPos pPos, BlockState pState, CallbackInfo ci) {
        ChronicleHandler.clientForceNonPaused = true;
    }

    @Inject(method = "setKnownState", at = @At("RETURN"))
    private void setKnownStateParadox$RETURN(BlockPos pPos, BlockState pState, CallbackInfo ci) {
        ChronicleHandler.clientForceNonPaused = false;
    }
}
