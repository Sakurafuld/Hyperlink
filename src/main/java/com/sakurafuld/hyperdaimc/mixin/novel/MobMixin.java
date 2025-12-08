package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.entity.IFumetsu;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityFumetsu;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(method = "removeAfterChangingDimensions", at = @At("HEAD"), cancellable = true)
    private void removeAfterChangingDimensionsNovel(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (FumetsuHandler.specialRemove.get()) {
            if (self instanceof IEntityFumetsu fumetsu) {
                fumetsu.hyperdaimc$extinction(Entity.RemovalReason.CHANGED_DIMENSION);
            }
            return;
        }
        if (self instanceof Player) {
            return;
        }
        if ((self instanceof IFumetsu || (self instanceof LivingEntity living && MutekiHandler.muteki(living))) && !NovelHandler.novelized(self)) {
            ci.cancel();
        }
    }
}
