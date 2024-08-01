package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.api.ILivingEntityNovel;
import com.sakurafuld.hyperdaimc.content.ModItems;
import com.sakurafuld.hyperdaimc.content.muteki.MutekiHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;

@Mixin(ForgeHooks.class)
public abstract class ForgeHooksMixin {
    @Inject(method = "onLivingDeath", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onLivingDeathNovel(LivingEntity entity, DamageSource src, CallbackInfoReturnable<Boolean> cir){
        if(MutekiHandler.getNovelized() != null && MutekiHandler.getNovelized().equals(entity)){
            MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(entity, src));
            ((ILivingEntityNovel) entity).novelHeartAttack();
            cir.setReturnValue(false);
            return;
        }
        if(!CuriosApi.getCuriosHelper().findCurios(entity, ModItems.MUTEKI.get()).isEmpty()){
            cir.setReturnValue(true);
        }
    }
}
