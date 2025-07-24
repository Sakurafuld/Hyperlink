package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaDisarm;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(GaiaDisarm.class)
public abstract class GaiaDisarmMixin {
    @Inject(method = "disArm", at = @At("HEAD"), cancellable = true, remap = false)
    private void disarmMuteki(Player player, CallbackInfo ci) {
        if (MutekiHandler.muteki(player)) {
            ci.cancel();
        }
    }
}
