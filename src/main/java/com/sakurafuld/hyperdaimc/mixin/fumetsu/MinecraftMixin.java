package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.api.mixin.IClientLevelFumetsu;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.timings.TimeTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Mixin(Minecraft.class)
@OnlyIn(Dist.CLIENT)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public ClientLevel level;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;tickEntities()V"))
    private void tickFumetsu(CallbackInfo ci) {
        if (this.level instanceof IClientLevelFumetsu levelFumetsu) {
            ProfilerFiller profilerfiller = this.level.getProfiler();
            profilerfiller.push("entities");
            levelFumetsu.fumetsuTickList().forEach((p_194183_) -> {
                if (!p_194183_.isRemoved() && !p_194183_.isPassenger()) {
                    this.guardEntityTickFumetsu(this::tickNonPassengerFumetsu, p_194183_);
                }
            });
            profilerfiller.pop();
        }
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
            if (net.minecraftforge.common.ForgeConfig.SERVER.removeErroringEntities.get()) {
                com.mojang.logging.LogUtils.getLogger().error("{}", crashreport.getFriendlyReport());
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
            fumetsu.setMovable(true);
            entity.setOldPosAndRot();
            ++entity.tickCount;
            this.level.getProfiler().push(() -> ForgeRegistries.ENTITIES.getKey(entity.getType()).toString());
            fumetsu.fumetsuTick();
            this.level.getProfiler().pop();
            fumetsu.setMovable(false);

            for (Entity passenger : entity.getPassengers()) {
                this.tickPassengerFumetsu(entity, passenger);
            }
        }
    }


    @Unique
    private void tickPassengerFumetsu(Entity pMount, Entity pRider) {
        if (this.level instanceof IClientLevelFumetsu levelFumetsu && pRider instanceof IFumetsu fumetsu) {
            if (!pRider.isRemoved() && pRider.getVehicle() == pMount) {
                if (levelFumetsu.fumetsuTickList().contains(pRider)) {
                    fumetsu.setMovable(true);
                    pRider.setOldPosAndRot();
                    ++pRider.tickCount;
                    fumetsu.fumetsuTick();
                    fumetsu.setMovable(false);
                    for (Entity entity : pRider.getPassengers()) {
                        this.tickPassengerFumetsu(pRider, entity);
                    }
                }
            } else {
                pRider.stopRiding();
            }
        }
    }
}
