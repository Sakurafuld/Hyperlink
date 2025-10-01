package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.api.mixin.IEntityNovel;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTickList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(EntityTickList.class)
public abstract class EntityTickListMixin {
    @Redirect(method = "forEach", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private <T> void forEachNovel(Consumer<T> instance, T t) {
        Entity entity = (Entity) t;
        if (!(entity instanceof Player) && NovelHandler.novelized(entity)) {
            if (entity instanceof LivingEntity living && !entity.isRemoved() && (!entity.getLevel().isClientSide() || entity.getLevel().shouldTickDeath(entity))) {
                if (!NovelHandler.special(living)) {
                    int time = ++living.deathTime;
                    if (!entity.getLevel().isClientSide() && ((IEntityNovel) living).novelDead() >= 20) {
                        living.getLevel().broadcastEntityEvent(living, EntityEvent.POOF);
                        ((IEntityNovel) living).novelRemove(Entity.RemovalReason.KILLED);
                        return;
                    }
                    if (!entity.getLevel().isClientSide()) {
                        entity.checkDespawn();
                    }

                    living.setOldPosAndRot();
                    living.yHeadRotO = living.getYHeadRot();
                    living.yBodyRotO = living.yBodyRot;
                    living.oAttackAnim = living.attackAnim;
                    living.animationSpeedOld = 0;
                    living.animationSpeed = 0;
                    living.animationPosition = 0;
                    living.setSwimming(false);
                    living.deathTime = time;
                }
            } else {
                if (!entity.getLevel().isClientSide()) {
                    entity.checkDespawn();
                }
                entity.setOldPosAndRot();
            }
        } else {
            instance.accept(t);
        }
    }
}
