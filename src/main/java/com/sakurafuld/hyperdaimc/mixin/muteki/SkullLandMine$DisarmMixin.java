package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import io.github.lounode.extrabotany.common.entity.SkullLandMineEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(SkullLandMineEntity.Disarm.class)
public abstract class SkullLandMine$DisarmMixin {
    @Redirect(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSpectator()Z"), remap = false)
    private boolean explodeMuteki(Player instance) {
        if (MutekiHandler.muteki(instance)) {
            return true;
        } else {
            return instance.isSpectator();
        }
    }
}
