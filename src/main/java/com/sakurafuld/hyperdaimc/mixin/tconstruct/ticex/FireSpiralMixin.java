package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import cn.mmf.slashblade_addon.specialattacks.FireSpiral;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntitySlashEffectTicEx;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(FireSpiral.class)
public abstract class FireSpiralMixin {
    @ModifyArg(method = "doCircleSlash", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", remap = true), index = 0, remap = false)
    private static Entity doCircleSlashTicEx(Entity par1) {
        ((IEntitySlashEffectTicEx) par1).hyperdaimc$setSpecial(true);
        return par1;
    }
}
