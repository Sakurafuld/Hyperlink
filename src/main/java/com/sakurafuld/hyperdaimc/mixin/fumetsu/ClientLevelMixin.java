package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Shadow
    protected abstract void tickPassenger(Entity pMount, Entity pRider);

    @Shadow
    @Final
    EntityTickList tickingEntities;

    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    private void tickNonPassenger(Entity entity, CallbackInfo ci) {
        if (entity instanceof IFumetsu fumetsu) {
            ci.cancel();
            ClientLevel self = (ClientLevel) ((Object) this);
            ++entity.tickCount;
            self.getProfiler().push(() -> ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString());
            fumetsu.fumetsuTick();
            self.getProfiler().pop();

            for (Entity passenger : entity.getPassengers()) {
                this.tickPassenger(passenger, passenger);
            }
        }
    }

    @Inject(method = "tickPassenger", at = @At("HEAD"), cancellable = true)
    private void tickPassengerFumetsu(Entity pMount, Entity pRider, CallbackInfo ci) {
        if (pRider instanceof IFumetsu fumetsu) {
            ci.cancel();
            if (!pRider.isRemoved() && pRider.getVehicle() == pMount) {
                if (this.tickingEntities.contains(pRider)) {
                    ++pRider.tickCount;
                    fumetsu.fumetsuTick();

                    for (Entity entity : pRider.getPassengers()) {
                        this.tickPassenger(pRider, entity);
                    }
                }
            } else {
                pRider.stopRiding();
            }
        }
    }
}
