package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.entity.IFumetsu;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityFumetsu;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityNovel;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.ILivingEntityMuteki;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
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

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@Mixin(Entity.class)
public abstract class EntityMixin extends CapabilityProvider<Entity> implements IEntityNovel {
    @Shadow
    @Nullable
    private Entity.RemovalReason removalReason;
    @Shadow
    private EntityInLevelCallback levelCallback;
    @Unique
    private static EntityDataAccessor<Boolean> DATA_NOVELIZED;
    @Unique
    private static final String KEY_NOVELIZED = HYPERDAIMC + ":Novelized";
    @Unique
    private boolean initialized = false;
    @Unique
    private Entity.RemovalReason lastReason = null;

    protected EntityMixin(Class<Entity> baseClass) {
        super(baseClass);
    }

    @Inject(method = "<clinit>", at = @At("HEAD"))
    private static void staticInitializerNovel(CallbackInfo ci) {
        DATA_NOVELIZED = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
    }

    @Shadow
    public abstract void stopRiding();

    @Shadow
    public abstract List<Entity> getPassengers();

    @Shadow
    public abstract void gameEvent(GameEvent pEvent);

    @Shadow
    public abstract EntityType<?> getType();

    @Shadow
    public abstract SynchedEntityData getEntityData();

    @Override
    public void hyperdaimc$novelRemove(Entity.RemovalReason reason) {
        Entity self = (Entity) (Object) this;
        this.hyperdaimc$setNovelized();
        if (this.removalReason == null) {
            this.removalReason = reason;
            this.lastReason = reason;
        }

        if (this.removalReason.shouldDestroy())
            this.stopRiding();

        this.getPassengers().forEach(Entity::stopRiding);
        if ((Object) this instanceof IFumetsu fumetsu)
            ((IEntityFumetsu) fumetsu).hyperdaimc$extinction(this.removalReason);

        this.levelCallback.onRemove(this.removalReason);

        if (this.removalReason == Entity.RemovalReason.KILLED)
            this.gameEvent(GameEvent.ENTITY_DIE);

        this.invalidateCaps();

        if (self instanceof LivingEntity living) {
            living.getBrain().clearMemories();
            if (living instanceof Player player) {
                player.inventoryMenu.removed(player);
                if (player instanceof ServerPlayer serverPlayer && player.containerMenu != null && player.hasContainerOpen()) {
                    serverPlayer.doCloseContainer();
                }
            }
        }
    }

    @Override
    public void hyperdaimc$novelize(LivingEntity writer) {
        this.hyperdaimc$novelRemove(Entity.RemovalReason.KILLED);
    }

    @Override
    public boolean hyperdaimc$isNovelized() {
        return this.initialized && this.getEntityData().get(DATA_NOVELIZED);
    }

    @Override
    public void hyperdaimc$setNovelized() {
        if (this.initialized)
            this.getEntityData().set(DATA_NOVELIZED, true);
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
        if (FumetsuHandler.isSpecialRemoving()) {
            if (self instanceof IEntityFumetsu fumetsu)
                fumetsu.hyperdaimc$extinction(pReason);
            return;
        }

        if ((self instanceof IFumetsu || (self instanceof LivingEntity living && MutekiHandler.muteki(living))) && !NovelHandler.novelized(self)) {
//            if (self instanceof Player) {
//                LOG.debug("RemoveMutekiPlayer");
//                return;
//            }
            this.removalReason = this.lastReason;
            ci.cancel();
        }
    }

    @Inject(method = "setRemoved", at = @At("HEAD"), cancellable = true)
    private void setRemovedNovel(Entity.RemovalReason pReason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (FumetsuHandler.isSpecialRemoving()) {
            if (self instanceof IEntityFumetsu fumetsu)
                fumetsu.hyperdaimc$extinction(pReason);

            return;
        }

        if ((self instanceof IFumetsu || (self instanceof LivingEntity living && MutekiHandler.muteki(living))) && !NovelHandler.novelized(self)) {
//            if (self instanceof Player) {
//                LOG.debug("RemoveMutekiPlayer");
//                return;
//            }
            this.removalReason = this.lastReason;
            ci.cancel();
        }
    }

    @Inject(method = "getRemovalReason", at = @At("HEAD"), cancellable = true)
    private void getRemovalReasonNovel(CallbackInfoReturnable<Entity.RemovalReason> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof LivingEntity living && ((ILivingEntityMuteki) living).hyperdaimc$isMutekiForced())
            return;

        if (self instanceof Player)
            return;

        if ((self instanceof IFumetsu || (self instanceof LivingEntity living && MutekiHandler.muteki(living))) && !NovelHandler.novelized(self)) {
            this.removalReason = this.lastReason;
            if (this.removalReason != null) cir.setReturnValue(null);
        }
    }

    @Inject(method = "isRemoved", at = @At("HEAD"), cancellable = true)
    private void isRemovedNovel(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Player)
            return;

        if (self instanceof LivingEntity living && ((ILivingEntityMuteki) living).hyperdaimc$isMutekiForced())
            return;

        if ((self instanceof IFumetsu || (self instanceof LivingEntity living && MutekiHandler.muteki(living))) && !NovelHandler.novelized(self)) {
            this.removalReason = this.lastReason;
            if (this.removalReason != null && this.removalReason.shouldDestroy()) cir.setReturnValue(false);
        }
    }

    @Inject(method = "unsetRemoved", at = @At("HEAD"))
    private void unsetRemovedNovel(CallbackInfo ci) {
        this.lastReason = null;
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void saveWithoutIdNovel(CompoundTag pCompound, CallbackInfoReturnable<CompoundTag> cir) {
        if (!((Object) this instanceof Player) && this.hyperdaimc$isNovelized())
            pCompound.putBoolean(KEY_NOVELIZED, true);
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "load", at = @At("HEAD"))
    private void loadNovel(CompoundTag pCompound, CallbackInfo ci) {
        if (!((Object) this instanceof Player) && pCompound.contains(KEY_NOVELIZED))
            this.getEntityData().set(DATA_NOVELIZED, pCompound.getBoolean(KEY_NOVELIZED));
        else this.getEntityData().set(DATA_NOVELIZED, false);
    }
}