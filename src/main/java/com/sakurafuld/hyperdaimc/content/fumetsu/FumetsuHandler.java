package com.sakurafuld.hyperdaimc.content.fumetsu;

import com.google.common.collect.Lists;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class FumetsuHandler {
    private static final List<ServerPlayer> PLAYERS = Lists.newArrayList();

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        FumetsuEntity.EXISTING.forEach(FumetsuEntity::logout);
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        FumetsuEntity.EXISTING.forEach(FumetsuEntity::logout);
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        List<ServerPlayer> current = server.getPlayerList().getPlayers()
                .stream()
                .filter(player -> !player.isRemoved() && player.getPose() != Pose.DYING)
                .toList();

        if (!current.equals(PLAYERS)) {
            FumetsuEntity.EXISTING.forEach(FumetsuEntity::logout);
            PLAYERS.clear();
            PLAYERS.addAll(current);
        }
    }
}
