package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void dieMuteki$Player(DamageSource pDamageSource, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (!NovelHandler.novelized(self) && MutekiHandler.muteki(self))
            ci.cancel();
    }

    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    private void dropEquipmentMuteki(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!NovelHandler.novelized(self) && MutekiHandler.muteki(self))
            ci.cancel();
    }

    @Inject(method = "destroyVanishingCursedItems", at = @At("HEAD"), cancellable = true)
    private void destroyVanishingCursedItemsMuteki(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!NovelHandler.novelized(self) && MutekiHandler.muteki(self))
            ci.cancel();
    }
}
