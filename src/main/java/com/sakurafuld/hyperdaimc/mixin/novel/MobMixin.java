package com.sakurafuld.hyperdaimc.mixin.novel;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Mob.class)
public abstract class MobMixin {
//    @Inject(method = "doHurtTarget", at = @At("HEAD"))
//    private void doHurtTargetNovel(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
//        Mob self = (Mob) ((Object) this);
//        if (self.getMainHandItem().is(HyperItems.NOVEL.get()) && self.level() instanceof ServerLevel level) {
//            LOG.debug("MobNovelize");
//            NovelHandler.novelize(self, pEntity, false);
//            NovelHandler.playSound(level, pEntity.position());
//            HyperConnection.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), new ClientboundNovelize(self.getId(), pEntity.getId(), 1));
//        }
//    }
}
