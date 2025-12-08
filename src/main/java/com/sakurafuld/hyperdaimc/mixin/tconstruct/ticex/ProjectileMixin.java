package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.sakurafuld.hyperdaimc.addon.tconstruct.HyperModifiers;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityTicEx;
import mods.flammpfeil.slashblade.entity.Projectile;
import moffy.ticex.item.modifiable.ModifiableSlashBladeItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

@Pseudo
@Mixin(Projectile.class)
public abstract class ProjectileMixin implements IEntityTicEx {
    @Inject(method = "setOwner", at = @At("HEAD"))
    public void setOwnerTicEx(Entity p_37263_, CallbackInfo ci) {
        this.hyperdaimc$setTicExNovel(p_37263_ instanceof LivingEntity living && this.isTicExNovel(living.getMainHandItem()));
    }

    @Unique
    protected boolean isTicExNovel(ItemStack stack) {
        if (stack.getItem() instanceof ModifiableSlashBladeItem) {
            ToolStack tool = ToolStack.from(stack);
            return !tool.isBroken() && tool.getModifierLevel(HyperModifiers.NOVEL.getId()) > 0;
        } else return false;
    }
}
