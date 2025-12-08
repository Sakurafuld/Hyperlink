package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntitySlashEffectTicEx;
import mods.flammpfeil.slashblade.slasharts.CircleSlash;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(CircleSlash.class)
public abstract class CircleSlashMixin {
    @ModifyArg(method = "doCircleSlashAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", remap = true), index = 0, remap = false)
    private static Entity doCircleSlashAttackTicEx(Entity par1) {
        ((IEntitySlashEffectTicEx) par1).hyperdaimc$setSpecial(true);
        return par1;
    }
}
