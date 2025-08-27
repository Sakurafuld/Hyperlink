package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.api.mixin.IEntityFumetsu;
import com.sakurafuld.hyperdaimc.api.mixin.IEntityNovel;
import com.sakurafuld.hyperdaimc.api.mixin.ILivingEntityMuteki;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

@Mixin(Entity.class)
public abstract class EntityMixin extends CapabilityProvider<Entity> implements IEntityNovel {
    protected EntityMixin(Class<Entity> baseClass) {
        super(baseClass);
    }

    @Shadow
    @Nullable
    private Entity.RemovalReason removalReason;

    @Shadow
    public abstract void stopRiding();

    @Shadow
    public abstract List<Entity> getPassengers();

    @Shadow
    private EntityInLevelCallback levelCallback;

    @Shadow
    public abstract void gameEvent(GameEvent pEvent);

    @Shadow
    public abstract EntityType<?> getType();

    @Shadow
    public abstract SynchedEntityData getEntityData();

    @Unique
    private static EntityDataAccessor<Boolean> DATA_NOVELIZED;

    @Unique
    private boolean initialized = false;

    @Override
    public void novelRemove(Entity.RemovalReason reason) {
        this.setNovelized();
        if (this.removalReason == null) {
            this.removalReason = reason;
        }

        if (this.removalReason.shouldDestroy()) {
            this.stopRiding();
        }

        this.getPassengers().forEach(Entity::stopRiding);
        if ((Object) this instanceof IFumetsu fumetsu) {
            ((IEntityFumetsu) fumetsu).fumetsuExtinction(this.removalReason);
        }
        this.levelCallback.onRemove(this.removalReason);

        if (this.removalReason == Entity.RemovalReason.KILLED) {
            this.gameEvent(GameEvent.ENTITY_DIE);
        }

        this.invalidateCaps();
    }

    @Override
    public void novelize(LivingEntity writer) {
        this.novelRemove(Entity.RemovalReason.KILLED);
    }

    @Override
    public boolean isNovelized() {
        return this.initialized && this.getEntityData().get(DATA_NOVELIZED);
    }

    @Override
    public void setNovelized() {
        if (this.initialized) {
            this.getEntityData().set(DATA_NOVELIZED, true);
        }
    }

    @Inject(method = "<clinit>", at = @At("HEAD"))
    private static void staticInitializerNovel(CallbackInfo ci) {
        DATA_NOVELIZED = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializerNovel(CallbackInfo ci) {
        this.getEntityData().define(DATA_NOVELIZED, false);
        this.initialized = true;
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("all")
    private void removeNovel(Entity.RemovalReason pReason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (FumetsuHandler.specialRemove.get()) {
            return;
        }
        if (self instanceof Player) {
            return;
        }
        if (pReason.shouldDestroy() && (self instanceof IFumetsu || (self instanceof LivingEntity living && MutekiHandler.muteki(living))) && !NovelHandler.novelized(self)) {
            ci.cancel();
        }
    }

    @Inject(method = "setRemoved", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("all")
    private void setRemovedNovel(Entity.RemovalReason pReason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (FumetsuHandler.specialRemove.get()) {
            return;
        }
        if (self instanceof Player) {
            return;
        }
        if (pReason.shouldDestroy() && (self instanceof IFumetsu || (self instanceof LivingEntity living && MutekiHandler.muteki(living))) && !NovelHandler.novelized(self)) {
            ci.cancel();
        }
    }

    @Inject(method = "getRemovalReason", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("all")
    private void getRemovalReasonNovel(CallbackInfoReturnable<Entity.RemovalReason> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof LivingEntity living && ((ILivingEntityMuteki) living).mutekiForced()) {
            return;
        }
        if (self instanceof Player) {
            return;
        }
        if (this.removalReason != null && this.removalReason.shouldDestroy() && (self instanceof IFumetsu || (self instanceof LivingEntity living && MutekiHandler.muteki(living))) && !NovelHandler.novelized(self)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "isRemoved", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("all")
    private void isRemovedNovel(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Player) {
            return;
        }
        if (self instanceof LivingEntity living && ((ILivingEntityMuteki) living).mutekiForced()) {
            return;
        }
        if (this.removalReason != null && this.removalReason.shouldDestroy() && (self instanceof IFumetsu || (self instanceof LivingEntity living && MutekiHandler.muteki(living))) && !NovelHandler.novelized(self)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void saveWithoutIdNovel(CompoundTag pCompound, CallbackInfoReturnable<CompoundTag> cir) {
        if (!((Object) this instanceof Player)) {
            pCompound.putBoolean(HYPERDAIMC + ":Novelized", this.isNovelized());
        }
    }

    @Inject(method = "load", at = @At("HEAD"))
    private void loadNovel(CompoundTag pCompound, CallbackInfo ci) {
        if (!((Object) this instanceof Player)) {
            this.getEntityData().set(DATA_NOVELIZED, pCompound.getBoolean(HYPERDAIMC + ":Novelized"));
        }
    }
}