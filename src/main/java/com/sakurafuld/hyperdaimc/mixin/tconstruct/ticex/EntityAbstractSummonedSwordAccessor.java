package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(EntityAbstractSummonedSword.class)
public interface EntityAbstractSummonedSwordAccessor {
    @Accessor(remap = false)
    boolean isInGround();
}
