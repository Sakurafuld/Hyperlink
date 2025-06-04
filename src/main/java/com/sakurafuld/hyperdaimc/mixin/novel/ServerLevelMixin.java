package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.api.mixin.IEntityNovel;
import com.sakurafuld.hyperdaimc.content.novel.NovelHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    private void tickNonPassengerFumetsu(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof IFumetsu || entity instanceof Player) && NovelHandler.novelized(entity)) {
            if (entity instanceof LivingEntity living) {
                if (!NovelHandler.special(living) && !living.isRemoved()) {
                    ci.cancel();
                    living.setOldPosAndRot();
                    living.yHeadRotO = living.getYHeadRot();
                    living.yBodyRotO = living.yBodyRot;
                    living.oAttackAnim = living.attackAnim;
                    living.walkAnimation.update(0, 1);
                    living.setSwimming(false);
                    if (++living.deathTime >= 20) {
                        living.level().broadcastEntityEvent(living, EntityEvent.POOF);
                        ((IEntityNovel) living).novelRemove(Entity.RemovalReason.KILLED);
                    }
                }
            } else {
                ci.cancel();
                entity.setOldPosAndRot();
            }
        }
    }

    @Inject(method = "tickPassenger", at = @At("HEAD"), cancellable = true)
    private void tickPassengerFumetsu(Entity pRidingEntity, Entity pPassengerEntity, CallbackInfo ci) {
        if (!(pPassengerEntity instanceof IFumetsu || pPassengerEntity instanceof Player) && NovelHandler.novelized(pPassengerEntity)) {
            if (pPassengerEntity instanceof LivingEntity living) {
                if (!NovelHandler.special(living) && !living.isRemoved()) {
                    ci.cancel();
                    pPassengerEntity.stopRiding();
                    pPassengerEntity.setOldPosAndRot();
                    living.yHeadRotO = living.getYHeadRot();
                    living.yBodyRotO = living.yBodyRot;
                    living.oAttackAnim = living.attackAnim;
                    living.walkAnimation.update(0, 1);
                    living.setSwimming(false);
                    if (++living.deathTime >= 20) {
                        living.level().broadcastEntityEvent(living, EntityEvent.POOF);
                        ((IEntityNovel) living).novelRemove(Entity.RemovalReason.KILLED);
                    }
                }
            } else {
                ci.cancel();
                pPassengerEntity.stopRiding();
                pPassengerEntity.setOldPosAndRot();
            }
        }
    }
}
