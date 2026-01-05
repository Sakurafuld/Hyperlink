package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.mojang.logging.LogUtils;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.entity.IFumetsu;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.FumetsuTickList;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IPersistentEntityManagerFumetsu;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IServerLevelFumetsu;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.timings.TimeTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements IServerLevelFumetsu {
    @Unique
    private final FumetsuTickList fumetsuTickList = new FumetsuTickList();
    @Unique
    private final Set<Mob> navigatingMobs2 = new ObjectOpenHashSet<>();
    @Shadow
    @Final
    public PersistentEntitySectionManager<Entity> entityManager;
    @Shadow
    volatile boolean isUpdatingNavigations;

    @Shadow
    protected abstract void tickPassenger(Entity pRidingEntity, Entity pPassengerEntity);

    @Shadow
    public abstract ServerChunkCache getChunkSource();

    @Override
    public void hyperdaimc$fumetsuSpawn(Entity entity) {
        ((IPersistentEntityManagerFumetsu) this.entityManager).hyperdaimc$fumetsuSpawn(entity);
        entity.onAddedToWorld();
    }

    @Override
    public FumetsuTickList hyperdaimc$fumetsuTickList() {
        return this.fumetsuTickList;
    }

    @Override
    public Set<Mob> hyperdaimc$fumetsuNavi() {
        return this.navigatingMobs2;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/EntityTickList;forEach(Ljava/util/function/Consumer;)V"))
    private void tickFumetsu(BooleanSupplier pHasTimeLeft, CallbackInfo ci) {
        this.fumetsuTickList.forEach(entity -> {
            if (!(entity.isRemoved() && NovelHandler.novelized(entity))) {
                entity.checkDespawn();
                if (this.getChunkSource().chunkMap.getDistanceManager().inEntityTickingRange(entity.chunkPosition().toLong()) && !(entity.isRemoved() && NovelHandler.novelized(entity)))
                    this.guardEntityTickFumetsu(this::tickNonPassengerFumetsu, entity);
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
            self.getProfiler().push(() -> ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString());
            profilerfiller.incrementCounter("tickNonPassenger");
            fumetsu.fumetsuTick();
            self.getProfiler().pop();
            fumetsu.setMovable(false);

//            for (Entity passenger : entity.getPassengers()) {
//                this.tickPassengerFumetsu(entity, passenger);
//            }
        }
    }

//    @Unique
//    private void tickPassengerFumetsu(Entity pRidingEntity, Entity pPassengerEntity) {
//        if (pPassengerEntity instanceof IFumetsu fumetsu) {
//            ServerLevel self = (ServerLevel) ((Object) this);
//            if (!pPassengerEntity.isRemoved() && pPassengerEntity.getVehicle() == pRidingEntity) {
//                if (this.fumetsuTickList.contains(pPassengerEntity)) {
//                    fumetsu.setMovable(true);
//                    pPassengerEntity.setOldPosAndRot();
//                    ++pPassengerEntity.tickCount;
//                    ProfilerFiller profiler = self.getProfiler();
//                    profiler.push(() -> ForgeRegistries.ENTITY_TYPES.getKey(pPassengerEntity.getType()).toString());
//                    profiler.incrementCounter("tickPassenger");
//                    fumetsu.fumetsuTick();
//                    profiler.pop();
//                    fumetsu.setMovable(false);
//
//                    for (Entity entity : pPassengerEntity.getPassengers()) {
//                        this.tickPassenger(pPassengerEntity, entity);
//                    }
//                }
//            } else {
//                pPassengerEntity.stopRiding();
//            }
//        }
//    }

    @Inject(method = "sendBlockUpdated", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/ObjectArrayList;<init>()V"))
    private void sendBlockUpdatedFumetsu(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags, CallbackInfo ci) {
        List<PathNavigation> list = new ObjectArrayList<>();

        for (Mob mob : this.navigatingMobs2) {
            PathNavigation pathnavigation = mob.getNavigation();
            if (pathnavigation.shouldRecomputePath(pPos)) {
                list.add(pathnavigation);
            }
        }

        try {
            this.isUpdatingNavigations = true;

            for (PathNavigation pathnavigation1 : list) {
                pathnavigation1.recomputePath();
            }
        } finally {
            this.isUpdatingNavigations = false;
        }
    }

    @Inject(method = "removePlayerImmediately", at = @At("HEAD"))
    private void removePlayerImmediatelyFumetsu$HEAD(ServerPlayer pPlayer, Entity.RemovalReason pReason, CallbackInfo ci) {
        FumetsuHandler.increaseSpecialRemove();
    }

    @Inject(method = "removePlayerImmediately", at = @At("RETURN"))
    private void removePlayerImmediatelyFumetsu$RETURN(ServerPlayer pPlayer, Entity.RemovalReason pReason, CallbackInfo ci) {
        FumetsuHandler.decreaseSpecialRemove();
    }
}
