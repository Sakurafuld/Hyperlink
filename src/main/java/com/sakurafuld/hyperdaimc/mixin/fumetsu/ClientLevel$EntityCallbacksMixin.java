package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.api.mixin.IClientLevelFumetsu;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.helper.Deets;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.multiplayer.ClientLevel$EntityCallbacks")
@OnlyIn(Dist.CLIENT)
public abstract class ClientLevel$EntityCallbacksMixin implements LevelCallback<Entity> {
    @Shadow
    @Final
    ClientLevel this$0;

    @Inject(method = "onTickingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void onTickingStartFumetsu(Entity p_143363_, CallbackInfo ci) {
        if (this.this$0 instanceof IClientLevelFumetsu levelFumetsu && p_143363_ instanceof IFumetsu) {
            Deets.LOG.debug("tickingStartFumetsu");
            levelFumetsu.fumetsuTickList().add(p_143363_);
            ci.cancel();
        }
    }

    @Inject(method = "onTickingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void onTickingEndFumetsu(Entity p_143363_, CallbackInfo ci) {
        if (this.this$0 instanceof IClientLevelFumetsu levelFumetsu && p_143363_ instanceof IFumetsu) {
            if (FumetsuHandler.specialRemove.get() || NovelHandler.novelized(p_143363_)) {
                Deets.LOG.debug("tickingEndFumetsu");
                levelFumetsu.fumetsuTickList().remove(p_143363_);
                ci.cancel();
            }
        }
    }
}
