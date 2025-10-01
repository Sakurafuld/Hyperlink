package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Mixin(EntityLookup.class)
public abstract class EntityLookupMixin<T extends EntityAccess> {
    @Shadow
    @Final
    private static Logger LOGGER;
    @Unique
    private Int2ObjectMap<T> byId2 = new Int2ObjectLinkedOpenHashMap<>();
    @Unique
    private final Map<UUID, T> byUuid2 = Maps.newHashMap();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructorFumetsu(CallbackInfo ci) {
        this.byId2 = new Int2ObjectLinkedOpenHashMap<>();
    }

    @Inject(method = "getEntities", at = @At("HEAD"))
    public <U extends T> void getEntitiesFumetsu(EntityTypeTest<T, U> pTest, Consumer<U> pConsumer, CallbackInfo ci) {
        for (T t : this.byId2.values()) {
            U u = pTest.tryCast(t);
            if (u != null) {
                pConsumer.accept(u);
            }
        }
    }

    @Inject(method = "getAllEntities", at = @At("RETURN"), cancellable = true)
    public void getAllEntitiesFumetsu(CallbackInfoReturnable<Iterable<T>> cir) {
        cir.setReturnValue(Iterables.concat(this.byId2.values(), cir.getReturnValue()));
    }

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    public void add(T pEntity, CallbackInfo ci) {
        if (pEntity instanceof IFumetsu) {
//            Deets.LOG.debug("lookupAddFumetsu");
            ci.cancel();
            UUID uuid = pEntity.getUUID();
            if (this.byUuid2.containsKey(uuid)) {
                LOGGER.warn("Duplicate entity UUID {}: {}", uuid, pEntity);
            } else {
                this.byUuid2.put(uuid, pEntity);
                this.byId2.put(pEntity.getId(), pEntity);
            }
        }
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    public void remove(T pEntity, CallbackInfo ci) {
        if (pEntity instanceof IFumetsu) {
            ci.cancel();
            if (FumetsuHandler.specialRemove.get() || NovelHandler.novelized((Entity) pEntity)) {
//                Deets.LOG.debug("lookupRemoveFumetsu");
                this.byUuid2.remove(pEntity.getUUID());
                this.byId2.remove(pEntity.getId());
            }
        }
    }

    @Inject(method = "getEntity(I)Lnet/minecraft/world/level/entity/EntityAccess;", at = @At("HEAD"), cancellable = true)
    public void getEntity(int pId, CallbackInfoReturnable<T> cir) {
        T e = this.byId2.get(pId);
        if (e != null) {
            cir.setReturnValue(e);
        }
    }

    @Inject(method = "getEntity(Ljava/util/UUID;)Lnet/minecraft/world/level/entity/EntityAccess;", at = @At("HEAD"), cancellable = true)
    public void getEntity(UUID pUuid, CallbackInfoReturnable<T> cir) {
        T e = this.byUuid2.get(pUuid);
        if (e != null) {
            cir.setReturnValue(e);
        }
    }

    @Inject(method = "count", at = @At("RETURN"), cancellable = true)
    public void count(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.byUuid2.size() + cir.getReturnValueI());
    }
}
