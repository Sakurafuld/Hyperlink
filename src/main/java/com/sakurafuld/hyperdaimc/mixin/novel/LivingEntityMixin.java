package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.api.mixin.IEntityNovel;
import com.sakurafuld.hyperdaimc.api.mixin.ILivingEntityMuteki;
import com.sakurafuld.hyperdaimc.content.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.novel.NovelDamageSource;
import com.sakurafuld.hyperdaimc.content.novel.NovelHandler;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;
import static com.sakurafuld.hyperdaimc.helper.Deets.require;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements IEntityNovel {
    @Shadow
    @Final
    private static EntityDataAccessor<Float> DATA_HEALTH_ID;

    @Override
    public void novelize(LivingEntity writer) {
        if (!HyperServerConfig.ENABLE_NOVEL.get()) {
            return;
        }

        LivingEntity self = (LivingEntity) ((Object) this);
        NovelDamageSource damage = new NovelDamageSource(writer);

        ((ILivingEntityMuteki) self).force(true);

        if (!MutekiHandler.muteki(self) || (!HyperServerConfig.MUTEKI_NOVEL.get() && self.getHealth() <= 1)) {
            LOG.debug("completeNovelized");
            this.setNovelized();
        }

        self.setLastHurtByMob(writer);
        if (writer instanceof Player player) {
            self.setLastHurtByPlayer(player);
        }

        self.getCombatTracker().recordDamage(damage, 0);
        this.novelSetHealth();
//        double dx = writer.getX() - self.getX();
//
//        double dz;
//        for(dz = writer.getZ() - self.getZ(); dx * dx + dz * dz < 1E-4d; dz = (Math.random() - Math.random()) * 0.01) {
//            dx = (Math.random() - Math.random()) * 0.01;
//        }
//
//        self.hurtDir = (float)(Math.toDegrees(Mth.atan2(dz, dx)) - self.getYRot());
//        self.knockback(0.4, dx, dz);
        if (NovelHandler.novelized(self)) {
            require(LogicalSide.SERVER).run(() ->
                    self.die(damage));

            if (!FumetsuEntity.class.equals(self.getClass())) {
                ((ILivingEntityMuteki) self).force(true);
                for (int count = 0; count < 2048 && (self.getHealth() > 0 || self.isAlive()); count++) {
                    self.setHealth(0);
                    self.getEntityData().set(DATA_HEALTH_ID, 0f);
                }
                ((ILivingEntityMuteki) self).force(false);
            }
        }

        ((ILivingEntityMuteki) self).force(false);
    }

    @Unique
    private void novelSetHealth() {
        LivingEntity self = (LivingEntity) ((Object) this);
        if (MutekiHandler.muteki(self)) {
            if (!HyperServerConfig.MUTEKI_NOVEL.get()) {
                float health = self.getHealth();
                self.setHealth(health - 1);
                self.getEntityData().set(DATA_HEALTH_ID, health - 1);
                self.hurtDuration = 10;
                self.hurtTime = self.hurtDuration;
            }
        } else {
            self.setHealth(0);
            self.getEntityData().set(DATA_HEALTH_ID, 0f);
        }
    }
}
