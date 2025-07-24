package com.sakurafuld.hyperdaimc.mixin.paradox;

import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
@OnlyIn(Dist.CLIENT)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public HitResult hitResult;

    @Redirect(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"))
    private HitResult.Type continueAttackMixin(HitResult instance) {
        LocalPlayer player = ((Minecraft) (Object) this).player;
        if (ParadoxHandler.hasParadox(player)) {
            this.hitResult = player.pick(player.getReachDistance(), 1, HyperServerConfig.PARADOX_HIT_FLUID.get());
            return this.hitResult.getType();
        } else {
            return instance.getType();
        }
    }
}
