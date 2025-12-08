package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityTicEx;
import mods.flammpfeil.slashblade.ability.SlayerStyleArts;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "mods.flammpfeil.slashblade.ability.SlayerStyleArts$1")
public abstract class SlayerStyleArtsMixin implements IEntityTicEx {
    @Shadow
    @Final
    ServerPlayer val$sender;

    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void onHitEntityTicEx(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (entityHitResult.getEntity() instanceof LivingEntity target && this.hyperdaimc$isTicExNovel())
            SlayerStyleArts.doTeleport(this.val$sender, target);
    }
}
