package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.infrastructure.mixin.EntitySectionWrapper;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntitySectionStorage.class)
public abstract class EntitySectionStorageMixin<T extends EntityAccess> {
    @Inject(method = "createSection", at = @At("RETURN"), cancellable = true)
    private void createSectionFumetsu(long p_156902_, CallbackInfoReturnable<EntitySection<T>> cir) {
        cir.setReturnValue(new EntitySectionWrapper<>(cir.getReturnValue()));
    }
}
