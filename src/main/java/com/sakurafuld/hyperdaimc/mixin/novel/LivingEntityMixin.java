package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.api.mixin.IEntityNovel;
import com.sakurafuld.hyperdaimc.api.mixin.ILivingEntityMuteki;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelDamageSource;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements IEntityNovel {
    @Unique
    private int dead = 0;

    @Shadow
    @Final
    private static EntityDataAccessor<Float> DATA_HEALTH_ID;

    @Shadow
    private long lastDamageStamp;

    @Shadow
    @Nullable
    private DamageSource lastDamageSource;

    @Override
    public void novelize(LivingEntity writer) {
        if (!HyperCommonConfig.ENABLE_NOVEL.get()) {
            return;
        }

        LivingEntity self = (LivingEntity) ((Object) this);
        NovelDamageSource damage = new NovelDamageSource(writer);

        ((ILivingEntityMuteki) self).mutekiForce(true);

        if (!MutekiHandler.muteki(self) || (!HyperCommonConfig.MUTEKI_NOVEL.get() && self.getHealth() <= 1)) {
            this.setNovelized();
        }

        self.setLastHurtByMob(writer);
        if (writer instanceof Player player) {
            self.setLastHurtByPlayer(player);
        }

        self.getCombatTracker().recordDamage(damage, 0, 0);
        this.lastDamageSource = damage;
        this.lastDamageStamp = self.getLevel().getGameTime();
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
            if (!self.getLevel().isClientSide()) {
                LOG.debug("DieNovelized");
                self.die(damage);
            }

            if (!FumetsuEntity.class.equals(self.getClass())) {
                ((ILivingEntityMuteki) self).mutekiForce(true);
                for (int count = 0; count < 2048 && (self.getHealth() > 0 || !self.isDeadOrDying() || self.isAlive()); count++) {
                    self.setHealth(0);
                    self.getEntityData().set(DATA_HEALTH_ID, 0f);
                }
                ((ILivingEntityMuteki) self).mutekiForce(false);
            }
        }

        ((ILivingEntityMuteki) self).mutekiForce(false);
    }

    @Unique
    private void novelSetHealth() {
        LivingEntity self = (LivingEntity) ((Object) this);
        if (MutekiHandler.muteki(self)) {
            if (!HyperCommonConfig.MUTEKI_NOVEL.get()) {
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

    @Inject(method = "getHealth", at = @At("HEAD"), cancellable = true)
    private void getHealthNovel(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) ((Object) this);
        if (((ILivingEntityMuteki) self).mutekiForced()) {
            return;
        }
        if (NovelHandler.novelized(self)) {
            cir.setReturnValue(0f);
        } else if (MutekiHandler.muteki(self)) {
            cir.setReturnValue(((ILivingEntityMuteki) self).mutekiLastHealth());
        }
    }

    @Inject(method = "isDeadOrDying", at = @At("HEAD"), cancellable = true)
    private void isDeadOrDyingNovel(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) ((Object) this);
        if (((ILivingEntityMuteki) self).mutekiForced()) {
            return;
        }
        if (NovelHandler.novelized(self)) {
            cir.setReturnValue(true);
        } else if (MutekiHandler.muteki(self)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isAlive", at = @At("HEAD"), cancellable = true)
    private void isAliveNovel(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) ((Object) this);
        if (((ILivingEntityMuteki) self).mutekiForced()) {
            return;
        }
        if (NovelHandler.novelized(self)) {
            cir.setReturnValue(false);
        } else if (MutekiHandler.muteki(self)) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public int novelDead() {
        return ++this.dead;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void addAdditionalSaveDataNovel(CompoundTag pCompound, CallbackInfo ci) {
        pCompound.putInt(HYPERDAIMC + ":NovelDead", this.dead);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readAdditionalSaveDataNovel(CompoundTag pCompound, CallbackInfo ci) {
        this.dead = pCompound.getInt(HYPERDAIMC + ":NovelDead");
    }
}
