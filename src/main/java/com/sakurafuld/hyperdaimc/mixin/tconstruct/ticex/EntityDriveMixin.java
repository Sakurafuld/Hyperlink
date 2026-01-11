package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.sakurafuld.hyperdaimc.content.hyper.novel.system.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityTicEx;
import mods.flammpfeil.slashblade.entity.EntityDrive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(EntityDrive.class)
public abstract class EntityDriveMixin implements IEntityTicEx {
    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void onHitEntityTicEx(EntityHitResult entityHitResult, CallbackInfo ci) {
        EntityDrive self = (EntityDrive) (Object) this;
        if (self.level() instanceof ServerLevel level && self.getOwner() instanceof LivingEntity owner && this.hyperdaimc$isTicExNovel()) {
            Entity entity = entityHitResult.getEntity();
            if (entity.isAlive() || NovelHandler.PREDICATE.test(entity)) {
                if (owner instanceof ServerPlayer player)
                    NovelHandler.captureAndTransfer(player, () -> NovelHandler.novelize(player, entity, true));
                else NovelHandler.novelize(owner, entity, true);
                NovelHandler.playSound(level, entity.position());
            }
        }
    }
}
