package com.sakurafuld.hyperdaimc.content.hyper.fumetsu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class FumetsuHandler {
    public static ThreadLocal<Long> logout = ThreadLocal.withInitial(() -> 0L);
    public static ThreadLocal<Boolean> spawn = ThreadLocal.withInitial(() -> false);

    public static ThreadLocal<Boolean> specialRemove = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        logout.set(System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        logout.set(System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        logout.set(System.currentTimeMillis());
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void death(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof ServerPlayer) {
            logout.set(System.currentTimeMillis());
        }
    }
}
