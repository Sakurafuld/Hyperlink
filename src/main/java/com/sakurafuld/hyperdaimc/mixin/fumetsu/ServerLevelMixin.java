package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.mojang.logging.LogUtils;
import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.api.mixin.IPersistentEntityManagerFumetsu;
import com.sakurafuld.hyperdaimc.api.mixin.IServerLevelFumetsu;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.entity.EntityTickList;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.timings.TimeTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements IServerLevelFumetsu {
    @Shadow
    protected abstract void tickPassenger(Entity pRidingEntity, Entity pPassengerEntity);

    @Shadow
    @Final
    EntityTickList entityTickList;

    @Shadow
    @Final
    private PersistentEntitySectionManager<Entity> entityManager;

    @Shadow
    public abstract ServerChunkCache getChunkSource();

    @Unique
    private final EntityTickList entityTickList2 = new EntityTickList();
    @Unique
    private final Set<Mob> navigatingMobs2 = new ObjectOpenHashSet<>();

    @Override
    public void fumetsuSpawn(Entity entity) {
        ((IPersistentEntityManagerFumetsu) this.entityManager).spawn(entity);
        entity.onAddedToWorld();
    }

    @Override
    public EntityTickList fumetsuTickList() {
        return this.entityTickList2;
    }

    @Override
    public Set<Mob> fumetsuNavi() {
        return this.navigatingMobs2;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/EntityTickList;forEach(Ljava/util/function/Consumer;)V"))
    private void tickFumetsu(BooleanSupplier pHasTimeLeft, CallbackInfo ci) {
        this.entityTickList2.forEach(entity -> {
            if (!entity.isRemoved()) {
                entity.checkDespawn();
                if (this.getChunkSource().chunkMap.getDistanceManager().inEntityTickingRange(entity.chunkPosition().toLong())) {
                    Entity vehicle = entity.getVehicle();
                    if (vehicle != null) {
                        if (!vehicle.isRemoved() && vehicle.hasPassenger(entity)) {
                            return;
                        }

                        entity.stopRiding();
                    }

                    if (!entity.isRemoved() && !(entity instanceof PartEntity)) {
                        this.guardEntityTickFumetsu(this::tickNonPassengerFumetsu, entity);
                    }
                }
            }
        });
    }

    @Unique
    private <T extends Entity> void guardEntityTickFumetsu(Consumer<T> pConsumerEntity, T pEntity) {
        try {
            TimeTracker.ENTITY_UPDATE.trackStart(pEntity);
            pConsumerEntity.accept(pEntity);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking entity");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being ticked");
            pEntity.fillCrashReportCategory(crashreportcategory);
            if (ForgeConfig.SERVER.removeErroringEntities.get()) {
                LogUtils.getLogger().error("{}", crashreport.getFriendlyReport());
                pEntity.discard();
            } else
                throw new ReportedException(crashreport);
        } finally {
            TimeTracker.ENTITY_UPDATE.trackEnd(pEntity);
        }
    }

    @Unique
    private void tickNonPassengerFumetsu(Entity entity) {
        if (entity instanceof IFumetsu fumetsu) {
            ServerLevel self = (ServerLevel) ((Object) this);
            fumetsu.setMovable(true);
            entity.setOldPosAndRot();
            ProfilerFiller profilerfiller = self.getProfiler();
            ++entity.tickCount;
            self.getProfiler().push(() -> ForgeRegistries.ENTITIES.getKey(entity.getType()).toString());
            profilerfiller.incrementCounter("tickNonPassenger");
            fumetsu.fumetsuTick();
            self.getProfiler().pop();
            fumetsu.setMovable(false);

            for (Entity passenger : entity.getPassengers()) {
                this.tickPassengerFumetsu(entity, passenger);
            }
        }
    }

    @Unique
    private void tickPassengerFumetsu(Entity pRidingEntity, Entity pPassengerEntity) {
        if (pPassengerEntity instanceof IFumetsu fumetsu) {
            ServerLevel self = (ServerLevel) ((Object) this);
            if (!pPassengerEntity.isRemoved() && pPassengerEntity.getVehicle() == pRidingEntity) {
                if (this.entityTickList2.contains(pPassengerEntity)) {
                    fumetsu.setMovable(true);
                    pPassengerEntity.setOldPosAndRot();
                    ++pPassengerEntity.tickCount;
                    ProfilerFiller profiler = self.getProfiler();
                    profiler.push(() -> pPassengerEntity.getType().getRegistryName() == null ? pPassengerEntity.getType().toString() : pPassengerEntity.getType().getRegistryName().toString());
                    profiler.incrementCounter("tickPassenger");
                    fumetsu.fumetsuTick();
                    profiler.pop();
                    fumetsu.setMovable(false);

                    for (Entity entity : pPassengerEntity.getPassengers()) {
                        this.tickPassenger(pPassengerEntity, entity);
                    }
                }
            } else {
                pPassengerEntity.stopRiding();
            }
        }
    }

    @Inject(method = "tickNonPassenger", at = @At("HEAD"), cancellable = true)
    private void tickNonPassengerFumetsu(Entity entity, CallbackInfo ci) {
        if (entity instanceof IFumetsu fumetsu) {
            ci.cancel();
            ServerLevel self = (ServerLevel) ((Object) this);
            fumetsu.setMovable(true);
            entity.setOldPosAndRot();
            ProfilerFiller profilerfiller = self.getProfiler();
            ++entity.tickCount;
            self.getProfiler().push(() -> ForgeRegistries.ENTITIES.getKey(entity.getType()).toString());
            profilerfiller.incrementCounter("tickNonPassenger");
            fumetsu.fumetsuTick();
            self.getProfiler().pop();
            fumetsu.setMovable(false);

            for (Entity passenger : entity.getPassengers()) {
                this.tickPassenger(entity, passenger);
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
                    fumetsu.setMovable(true);
                    pPassengerEntity.setOldPosAndRot();
                    ++pPassengerEntity.tickCount;
                    ProfilerFiller profiler = self.getProfiler();
                    profiler.push(() -> pPassengerEntity.getType().getRegistryName() == null ? pPassengerEntity.getType().toString() : pPassengerEntity.getType().getRegistryName().toString());
                    profiler.incrementCounter("tickPassenger");
                    fumetsu.fumetsuTick();
                    profiler.pop();
                    fumetsu.setMovable(false);

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
