package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.api.mixin.IEntityNovel;
import com.sakurafuld.hyperdaimc.content.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.novel.NovelHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeHooks.class)
public abstract class ForgeHooksMixin {
    @Inject(method = "onLivingDeath", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onLivingDeathNovel(LivingEntity entity, DamageSource src, CallbackInfoReturnable<Boolean> cir) {

        if ((!Float.isFinite(entity.getHealth()) || HyperServerConfig.MUTEKI_NOVEL.get() || !NovelHandler.novelized(entity)) && MutekiHandler.muteki(entity)) {

            cir.setReturnValue(true);
        } else if (NovelHandler.novelized(entity)) {

            MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(entity, src));
            ((IEntityNovel) entity).killsOver();
            cir.setReturnValue(false);
        }
    }
}
