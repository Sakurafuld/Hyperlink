package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow
    protected abstract void tickPassenger(Entity pRidingEntity, Entity pPassengerEntity);

    @Shadow
    @Final
    EntityTickList entityTickList;

    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    private void tickNonPassengerFumetsu(Entity entity, CallbackInfo ci) {
        if (entity instanceof IFumetsu fumetsu) {
            ci.cancel();
            ServerLevel self = (ServerLevel) ((Object) this);
            ProfilerFiller profilerfiller = self.getProfiler();
            ++entity.tickCount;
            self.getProfiler().push(() -> ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString());
            profilerfiller.incrementCounter("tickNonPassenger");
            fumetsu.fumetsuTick();
            self.getProfiler().pop();

            for (Entity passenger : entity.getPassengers()) {
                this.tickPassenger(passenger, passenger);
            }
        }
    }

    @Inject(method = "tickPassenger", at = @At("HEAD"), cancellable = true)
    private void tickPassengerFumetsu(Entity pRidingEntity, Entity pPassengerEntity, CallbackInfo ci) {
        if (pPassengerEntity instanceof IFumetsu fumetsu) {
            ci.cancel();
            ServerLevel self = (ServerLevel) ((Object) this);
            if (!pPassengerEntity.isRemoved() && pPassengerEntity.getVehicle() == pRidingEntity) {
                if (this.entityTickList.contains(pPassengerEntity)) {
                    ++pPassengerEntity.tickCount;
                    ProfilerFiller profiler = self.getProfiler();
                    profiler.push(() -> ForgeRegistries.ENTITY_TYPES.getKey(pPassengerEntity.getType()).toString());
                    profiler.incrementCounter("tickPassenger");
                    fumetsu.fumetsuTick();
                    profiler.pop();

                    for (Entity entity : pPassengerEntity.getPassengers()) {
                        this.tickPassenger(pPassengerEntity, entity);
                    }
                }
            } else {
                pPassengerEntity.stopRiding();
            }
        }
    }
}
