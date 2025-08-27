package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.google.common.collect.Streams;
import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.stream.Stream;

@Mixin(EntitySection.class)
public abstract class EntitySectionMixin<T extends EntityAccess> {
    @Unique
    private ClassInstanceMultiMap<T> storage2;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initFumetsu(Class<T> pEntityClazz, Visibility pChunkStatus, CallbackInfo ci) {
        this.storage2 = new ClassInstanceMultiMap<>(pEntityClazz);
    }

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void addFumetsu(T pEntity, CallbackInfo ci) {
        if (pEntity instanceof IFumetsu) {
            this.storage2.add(pEntity);
            ci.cancel();
        }
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void removeFumetsu(T pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (pEntity instanceof IFumetsu) {
            if (FumetsuHandler.specialRemove.get()) {
                cir.setReturnValue(this.storage2.remove(pEntity));
                return;
            }
            if (NovelHandler.novelized((Entity) pEntity)) {
                cir.setReturnValue(this.storage2.remove(pEntity));
            } else {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "getEntities(Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/AbortableIterationConsumer$Continuation;", at = @At("HEAD"), cancellable = true)
    private void getEntitiesFumetsu(AABB pBounds, AbortableIterationConsumer<T> pConsumer, CallbackInfoReturnable<AbortableIterationConsumer.Continuation> cir) {
        for (T t : this.storage2) {
            if (t.getBoundingBox().intersects(pBounds) && pConsumer.accept(t).shouldAbort()) {
                cir.setReturnValue(AbortableIterationConsumer.Continuation.ABORT);
                return;
            }
        }
    }

    @Inject(method = "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)Lnet/minecraft/util/AbortableIterationConsumer$Continuation;", at = @At("RETURN"), cancellable = true)
    private <U extends T> void getEntitiesFumetsu(EntityTypeTest<T, U> pTest, AABB pBounds, AbortableIterationConsumer<? super U> pConsumer, CallbackInfoReturnable<AbortableIterationConsumer.Continuation> cir) {
        if (cir.getReturnValue().shouldAbort()) {
            return;
        }
        Collection<? extends T> collection = this.storage2.find(pTest.getBaseClass());
        if (!collection.isEmpty()) {
            for (T t : collection) {
                U u = pTest.tryCast(t);
                if (u != null && t.getBoundingBox().intersects(pBounds) && pConsumer.accept(u).shouldAbort()) {
                    cir.setReturnValue(AbortableIterationConsumer.Continuation.ABORT);
                    break;
                }
            }
        }
    }

    @Inject(method = "isEmpty", at = @At("HEAD"), cancellable = true)
    private void isEmptyFumetsu(CallbackInfoReturnable<Boolean> cir) {
        if (!this.storage2.isEmpty()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getEntities()Ljava/util/stream/Stream;", at = @At("RETURN"), cancellable = true)
    private void getEntitiesFumetsu(CallbackInfoReturnable<Stream<T>> cir) {
        cir.setReturnValue(Streams.concat(cir.getReturnValue(), this.storage2.stream()));
    }

    @Inject(method = "size", at = @At("RETURN"), cancellable = true)
    private void sizeFumetsu(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValueI() + this.storage2.size());
    }
}
