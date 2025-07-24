package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
@OnlyIn(Dist.CLIENT)
public abstract class ClientLevelMixin {
    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    private void tickNonPassenger(Entity entity, CallbackInfo ci) {
        if (!(entity instanceof IFumetsu || entity instanceof Player) && NovelHandler.novelized(entity)) {
            if (entity instanceof LivingEntity living) {
                if (!NovelHandler.special(living) && !living.isRemoved() && living.getLevel().shouldTickDeath(living)) {
                    ci.cancel();
                    ++living.deathTime;
                    living.setOldPosAndRot();
                    living.yHeadRotO = living.getYHeadRot();
                    living.yBodyRotO = living.yBodyRot;
                    living.oAttackAnim = living.attackAnim;
                    living.animationSpeedOld = 0;
                    living.animationSpeed = 0;
                    living.animationPosition = 0;
                    living.setSwimming(false);
                }
            } else {
                ci.cancel();
                entity.setOldPosAndRot();
            }
        }
    }

    @Inject(method = "tickPassenger", at = @At("HEAD"), cancellable = true)
    private void tickPassengerFumetsu(Entity pMount, Entity pRider, CallbackInfo ci) {
        if (!(pRider instanceof IFumetsu || pRider instanceof Player) && NovelHandler.novelized(pRider)) {
            if (pRider instanceof LivingEntity living) {
                if (!NovelHandler.special(living) && !living.isRemoved() && living.getLevel().shouldTickDeath(living)) {
                    ci.cancel();
                    pRider.stopRiding();
                    ++living.deathTime;
                    living.setOldPosAndRot();
                    living.yHeadRotO = living.getYHeadRot();
                    living.yBodyRotO = living.yBodyRot;
                    living.oAttackAnim = living.attackAnim;
                    living.animationSpeedOld = 0;
                    living.animationSpeed = 0;
                    living.animationPosition = 0;
                    living.setSwimming(false);
                }
            } else {
                ci.cancel();
                pRider.stopRiding();
                pRider.setOldPosAndRot();
            }
        }
    }
}
