package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityTicEx;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatParticleOptions;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(EntityAbstractSummonedSword.class)
public abstract class EntityAbstractSummonedSwordMixin extends EntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void tickTicEx(CallbackInfo ci) {
        this.tickParticles();
    }

    @Override
    protected void rideTickTicEx(CallbackInfo ci) {
        this.tickParticles();
    }

    @Unique
    private void tickParticles() {
        if (!NovelHandler.ModifierRenderingLevel.ALL.check())
            return;
        EntityAbstractSummonedSword self = (EntityAbstractSummonedSword) (Object) this;
        if (((IEntityTicEx) self).hyperdaimc$isTicExNovel() && self.tickCount % 2 == 0 && self.level() instanceof ServerLevel level) {
            GashatParticleOptions options = GashatParticleOptions.drop(self.level().getRandom()::nextFloat, 1);
            level.sendParticles(options, self.getX(), self.getY(), self.getZ(), 4, 0, 0, 0, 1);
        }
    }
}
