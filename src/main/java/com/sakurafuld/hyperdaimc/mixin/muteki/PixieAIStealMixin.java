package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.github.alexthe666.iceandfire.entity.ai.PixieAISteal;
import com.sakurafuld.hyperdaimc.content.muteki.MutekiHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;

@Pseudo
@Mixin(PixieAISteal.class)
public abstract class PixieAIStealMixin {
    @Shadow(remap = false)
    private Player temptingPlayer;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;isEmpty()Z"), remap = false)
    private boolean tickMuteki(ArrayList<Integer> instance) {
        if (MutekiHandler.muteki(this.temptingPlayer)) {
            return true;
        } else {
            return instance.isEmpty();
        }
    }
}
