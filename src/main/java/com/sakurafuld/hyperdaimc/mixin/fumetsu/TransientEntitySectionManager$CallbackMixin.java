package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.sakurafuld.hyperdaimc.helper.Deets.side;

@Mixin(targets = "net.minecraft.world.level.entity.TransientEntitySectionManager$Callback")
public abstract class TransientEntitySectionManager$CallbackMixin {

    @Shadow(remap = false)
    @Final
    private Entity realEntity;

    @Inject(method = "onRemove", at = @At("HEAD"), cancellable = true)
    private void onRemoveFumetsu(Entity.RemovalReason pReason, CallbackInfo ci) {
        if (side().isClient() && FumetsuHandler.clientSpecialRemove) {
            return;
        }
        if (this.realEntity instanceof Player) {
            return;
        }
        if (this.realEntity != null && !NovelHandler.novelized(this.realEntity) && (this.realEntity instanceof IFumetsu || (this.realEntity instanceof LivingEntity living && MutekiHandler.muteki(living)))) {
            ci.cancel();
        }
    }
}
