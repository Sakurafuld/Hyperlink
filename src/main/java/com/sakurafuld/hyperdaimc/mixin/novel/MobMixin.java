package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.network.PacketHandler;
import com.sakurafuld.hyperdaimc.network.novel.ClientboundNovelize;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(method = "doHurtTarget", at = @At("HEAD"))
    private void doHurtTargetNovel(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        Mob self = (Mob) ((Object) this);
        if (self.getMainHandItem().is(HyperItems.NOVEL.get()) && self.getLevel() instanceof ServerLevel level) {
            LOG.debug("MobNovelize");
            NovelHandler.novelize(self, pEntity, false);
            NovelHandler.playSound(level, pEntity.position());
            PacketHandler.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), new ClientboundNovelize(self.getId(), pEntity.getId(), 1));
        }
    }
}
