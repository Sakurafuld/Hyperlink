package com.sakurafuld.hyperdaimc.mixin.paradox;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.handler.ParadoxHandler;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.handler.ParadoxTrigger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Objects;

@Mixin(Minecraft.class)
@OnlyIn(Dist.CLIENT)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public HitResult hitResult;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;

    @Shadow
    public abstract float getPartialTick();

    @Shadow
    @Nullable
    public ClientLevel level;

    @Shadow
    protected int missTime;

    @Inject(method = "continueAttack", at = @At("HEAD"))
    private void continueAttackParadox(boolean pLeftClick, CallbackInfo ci) {
        LocalPlayer player = Objects.requireNonNull(this.player);
        if (HyperCommonConfig.PARADOX_HIT_FLUID.get() && pLeftClick && this.missTime <= 0 && !player.isUsingItem() && ParadoxHandler.hasParadox(player)) {
            if (this.hitResult == null || this.hitResult.getType() == HitResult.Type.MISS) {
                HitResult hit = player.pick(Math.max(this.gameMode.getPickRange(), player.getEntityReach()), this.getPartialTick(), true);
                if (hit instanceof BlockHitResult result && result.getType() != HitResult.Type.MISS && !this.level.getFluidState(result.getBlockPos()).isEmpty()) {
                    this.hitResult = result;
                }
            }
        }
    }

    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    private void pickBlocKParadox(CallbackInfo ci) {
        if (ParadoxTrigger.pick())
            ci.cancel();
    }
}
