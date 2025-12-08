package com.sakurafuld.hyperdaimc.content.hyper.fumetsu;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class FumetsuHandler {
    public static ThreadLocal<Long> logout = ThreadLocal.withInitial(() -> 0L);
    public static ThreadLocal<Boolean> spawn = ThreadLocal.withInitial(() -> false);
    public static ThreadLocal<Boolean> specialRemove = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!HyperCommonConfig.FUMETSU_LOGOUT.get()) return;
        logout.set(System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!HyperCommonConfig.FUMETSU_LOGOUT.get()) return;
        logout.set(System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!HyperCommonConfig.FUMETSU_LOGOUT.get()) return;
        logout.set(System.currentTimeMillis());
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void death(LivingDeathEvent event) {
        if (!HyperCommonConfig.FUMETSU_LOGOUT.get()) return;
        if (event.getEntity() instanceof ServerPlayer) logout.set(System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!HyperCommonConfig.FUMETSU_LOGOUT.get()) return;
        if (event.getEntity() instanceof ServerPlayer) logout.set(System.currentTimeMillis());
    }
}
