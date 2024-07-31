package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.content.ModItems;
import com.sakurafuld.hyperdaimc.content.novel.NovelHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.sakurafuld.hyperdaimc.Deets.LOG;
import static com.sakurafuld.hyperdaimc.Deets.side;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(method = "doHurtTarget", at = @At("HEAD"))
    private void doHurtTargetNovel(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        LOG.debug("{}-doHurtTarget", side());
        Mob self = (Mob) ((Object) this);
        if (!self.getMainHandItem().is(ModItems.NOVEL.get())) return;
        NovelHandler.novelKill(self, pEntity);
    }
}
