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
    public static final ThreadLocal<Long> LOGOUT = ThreadLocal.withInitial(() -> 0L);
    private static final ThreadLocal<Integer> SPAWN = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Integer> SPECIAL_REMOVE = ThreadLocal.withInitial(() -> 0);

    public static boolean isSpecialRemoving() {
        return 0 < SPECIAL_REMOVE.get();
    }

    public static void increaseSpecialRemove() {
        SPECIAL_REMOVE.set(SPECIAL_REMOVE.get() + 1);
    }

    public static void decreaseSpecialRemove() {
        SPECIAL_REMOVE.set(SPECIAL_REMOVE.get() - 1);
        assert 0 <= SPECIAL_REMOVE.get() : "'SPECIAL_REMOVE' is less than zero";
    }

    public static boolean isSpawning() {
        return 0 < SPAWN.get();
    }

    public static void increaseSpawning() {
        SPAWN.set(SPAWN.get() + 1);
    }

    public static void decreaseSpawning() {
        SPAWN.set(SPAWN.get() - 1);
        assert 0 <= SPAWN.get() : "'SPAWN' is less than zero";
    }

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!HyperCommonConfig.FUMETSU_LOGOUT.get()) return;
        LOGOUT.set(System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!HyperCommonConfig.FUMETSU_LOGOUT.get()) return;
        LOGOUT.set(System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!HyperCommonConfig.FUMETSU_LOGOUT.get()) return;
        LOGOUT.set(System.currentTimeMillis());
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void death(LivingDeathEvent event) {
        if (!HyperCommonConfig.FUMETSU_LOGOUT.get()) return;
        if (event.getEntity() instanceof ServerPlayer) LOGOUT.set(System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!HyperCommonConfig.FUMETSU_LOGOUT.get()) return;
        if (event.getEntity() instanceof ServerPlayer) LOGOUT.set(System.currentTimeMillis());
    }
}
