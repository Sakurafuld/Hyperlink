package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.api.mixin.IServerLevelFumetsu;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.helper.Deets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.entity.LevelCallback;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public abstract class ServerLevel$EntityCallbacksMixin implements LevelCallback<Entity> {
    @Shadow
    @Final
    ServerLevel this$0;

    @Inject(method = "onTickingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void onTickingStartFumetsu(Entity p_143363_, CallbackInfo ci) {
        if (this.this$0 instanceof IServerLevelFumetsu levelFumetsu && p_143363_ instanceof IFumetsu) {
            Deets.LOG.debug("tickingStartFumetsu");
            levelFumetsu.fumetsuTickList().add(p_143363_);
            ci.cancel();
        }
    }

    @Inject(method = "onTickingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void onTickingEndFumetsu(Entity p_143363_, CallbackInfo ci) {
        if (this.this$0 instanceof IServerLevelFumetsu levelFumetsu && p_143363_ instanceof IFumetsu) {
            if (FumetsuHandler.specialRemove.get() || NovelHandler.novelized(p_143363_)) {
                Deets.LOG.debug("tickingEndFumetsu");
                levelFumetsu.fumetsuTickList().remove(p_143363_);
                ci.cancel();
            }
        }
    }

    @Redirect(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    private boolean onTrackingStartFumetsu$navi(Set<Mob> instance, Object e) {
        if (this.this$0 instanceof IServerLevelFumetsu levelFumetsu && e instanceof IFumetsu) {
            Deets.LOG.debug("trackingStartFumetsu$Navi");
            return levelFumetsu.fumetsuNavi().add((Mob) e);
        } else {
            return instance.add((Mob) e);
        }
    }

    @Redirect(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"))
    private boolean onTrackingEndFumetsu$navi(Set<Mob> instance, Object o) {
        if (this.this$0 instanceof IServerLevelFumetsu levelFumetsu && o instanceof IFumetsu) {
            if (FumetsuHandler.specialRemove.get() || NovelHandler.novelized((Entity) o)) {
                Deets.LOG.debug("trackingEndFumetsu$Navi");
                return levelFumetsu.fumetsuNavi().remove(o);
            } else {
                return false;
            }
        } else {
            return instance.remove(o);
        }
    }

    @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void onTrackingEndFumetsu(Entity p_143375_, CallbackInfo ci) {
        if (p_143375_ instanceof IFumetsu) {
            if (FumetsuHandler.specialRemove.get() || NovelHandler.novelized(p_143375_)) {
                Deets.LOG.debug("trackingEndFumetsu");
                return;
            }
            Deets.LOG.debug("trackingEndFumetsuCancel");
            ci.cancel();
        }
    }
}
