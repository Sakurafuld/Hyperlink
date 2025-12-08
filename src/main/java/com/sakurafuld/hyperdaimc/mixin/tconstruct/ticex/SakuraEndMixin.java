package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntitySlashEffectTicEx;
import mods.flammpfeil.slashblade.entity.EntitySlashEffect;
import mods.flammpfeil.slashblade.slasharts.SakuraEnd;
import mods.flammpfeil.slashblade.util.KnockBacks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(SakuraEnd.class)
public abstract class SakuraEndMixin {
    @Inject(method = "doSlash(Lnet/minecraft/world/entity/LivingEntity;FILnet/minecraft/world/phys/Vec3;ZZDLmods/flammpfeil/slashblade/util/KnockBacks;)Lmods/flammpfeil/slashblade/entity/EntitySlashEffect;", at = @At(value = "RETURN", ordinal = 3), remap = false)
    private static void doSlashTicEX(LivingEntity playerIn, float roll, int colorCode, Vec3 centerOffset, boolean mute, boolean critical, double damage, KnockBacks knockback, CallbackInfoReturnable<EntitySlashEffect> cir) {
        EntitySlashEffect slash = cir.getReturnValue();
        ((IEntitySlashEffectTicEx) slash).hyperdaimc$setSpecial(true);
    }
}
