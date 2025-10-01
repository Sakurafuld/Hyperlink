package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.api.mixin.IEntityFumetsu;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
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
                fumetsu.fumetsuExtinction(Entity.RemovalReason.CHANGED_DIMENSION);
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
