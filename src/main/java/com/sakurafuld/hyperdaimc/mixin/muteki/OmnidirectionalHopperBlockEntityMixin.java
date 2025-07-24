package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import me.desht.pneumaticcraft.common.block.entity.OmnidirectionalHopperBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(OmnidirectionalHopperBlockEntity.class)
public abstract class OmnidirectionalHopperBlockEntityMixin {
    @Redirect(method = "tryEntityImport", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isAlive()Z"), remap = false)
    private boolean tryEntityImportMuteki(Entity instance) {
        if (instance instanceof LivingEntity living && MutekiHandler.muteki(living)) {
            return false;
        } else {
            return instance.isAlive();
        }
    }
}
