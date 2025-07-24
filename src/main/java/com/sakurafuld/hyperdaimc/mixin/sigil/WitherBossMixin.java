package com.sakurafuld.hyperdaimc.mixin.sigil;

import com.sakurafuld.hyperdaimc.content.HyperItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin {
    @Inject(method = "dropCustomDeathLoot", at = @At("RETURN"))
    private void dropCustomLootSigil(DamageSource pSource, int pLooting, boolean pRecentlyHit, CallbackInfo ci) {
        WitherBoss self = (WitherBoss) (Object) this;
        ItemEntity sigil = self.spawnAtLocation(HyperItems.GOD_SIGIL.get());
        if (sigil != null) {
            sigil.setExtendedLifetime();
        }
    }
}
