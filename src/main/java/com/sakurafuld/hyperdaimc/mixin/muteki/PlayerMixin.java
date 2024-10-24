package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.ModItems;
import com.sakurafuld.hyperdaimc.content.muteki.MutekiHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void dieMuteki$Player(DamageSource pDamageSource, CallbackInfo ci){
        Player self = (Player) ((Object) this);
        if(!CuriosApi.getCuriosHelper().findCurios(self, ModItems.MUTEKI.get()).isEmpty()){
            if(MutekiHandler.getNovelized() != null && MutekiHandler.getNovelized().equals(self) && self.getHealth() <= 0)
                return;
            ci.cancel();
        }
    }
}
