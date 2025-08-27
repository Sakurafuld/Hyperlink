package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.google.common.collect.Sets;
import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.api.mixin.IPersistentEntityManagerFumetsu;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.helper.Deets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.UUID;

@Mixin(PersistentEntitySectionManager.class)
public abstract class PersistentEntitySectionManagerMixin<T extends EntityAccess> implements IPersistentEntityManagerFumetsu {
    @Shadow
    @Final
    static Logger LOGGER;

    @Shadow
    protected abstract boolean addEntityWithoutEvent(T pEntity, boolean pWorldGenSpawned);

    @Unique
    private final Set<UUID> knownUuids2 = Sets.newHashSet();


    @Override
    @SuppressWarnings("unchecked")
    public void spawn(Entity entity) {
        this.addEntityWithoutEvent((T) entity, false);
    }

    @Override
    public Set<UUID> known() {
        return this.knownUuids2;
    }

    @Inject(method = "processUnloads", at = @At("HEAD"))
    private void processUnloadsFumetsu$HEAD(CallbackInfo ci) {
        FumetsuHandler.specialRemove.set(true);
    }

    @Inject(method = "processUnloads", at = @At("RETURN"))
    private void processUnloadsFumetsu$RETURN(CallbackInfo ci) {
        FumetsuHandler.specialRemove.set(false);
    }

    @Inject(method = "updateChunkStatus(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/entity/Visibility;)V", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;forEach(Ljava/util/function/Consumer;)V"))
    private void updateChunkStatusFumetsu$BEFORE(CallbackInfo ci) {
        FumetsuHandler.specialRemove.set(true);
    }

    @Inject(method = "updateChunkStatus(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/entity/Visibility;)V", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
    private void updateChunkStatusFumetsu$AFTER(CallbackInfo ci) {
        FumetsuHandler.specialRemove.set(false);
    }

    @Inject(method = "addEntityUuid", at = @At("HEAD"), cancellable = true)
    private void addEntityUuidFumetsu(T pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (pEntity instanceof IFumetsu) {
            Deets.LOG.debug("addEntityUuidFumetsu");
            if (!this.knownUuids2.add(pEntity.getUUID())) {
                LOGGER.warn("UUID of added entity already exists: {}", pEntity);
                cir.setReturnValue(false);
            } else {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "isLoaded", at = @At("HEAD"), cancellable = true)
    private void isLoadedFumetsu(UUID pUuid, CallbackInfoReturnable<Boolean> cir) {
        if (this.knownUuids2.contains(pUuid)) {
            cir.setReturnValue(true);
        }
    }
}
