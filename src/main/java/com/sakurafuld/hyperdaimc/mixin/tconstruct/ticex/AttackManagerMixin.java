package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntitySlashEffectTicEx;
import mods.flammpfeil.slashblade.entity.EntitySlashEffect;
import mods.flammpfeil.slashblade.util.AttackManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(AttackManager.class)
public abstract class AttackManagerMixin {
    @Inject(method = "newVoidSlashEffect", at = @At("RETURN"), remap = false)
    private static void newVoidSlashEffectTicEx(LivingEntity living, Vec3 pos, CallbackInfoReturnable<EntitySlashEffect> cir) {
        EntitySlashEffect slash = cir.getReturnValue();
        ((IEntitySlashEffectTicEx) slash).hyperdaimc$setSpecial(true);
    }
}
