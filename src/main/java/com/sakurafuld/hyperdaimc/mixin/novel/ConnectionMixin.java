package com.sakurafuld.hyperdaimc.mixin.novel;

import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityNovel;
import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/TickablePacketListener;tick()V"))
    private void tickNovel(TickablePacketListener instance) {
        if (instance instanceof ServerGamePacketListenerImpl listener) {
            ServerPlayer player = listener.player;
            if (NovelHandler.novelized(player)) {
                listener.tickCount++;

                int time = ++player.deathTime;
                if (!player.level().isClientSide() && ((IEntityNovel) player).hyperdaimc$novelDead(true) >= 20) {
                    player.level().broadcastEntityEvent(player, EntityEvent.POOF);
                    ((IEntityNovel) player).hyperdaimc$novelRemove(Entity.RemovalReason.KILLED);
                } else {
                    player.tickCount++;
                    if (player.containerMenu != player.inventoryMenu)
                        player.doCloseContainer();
                    player.getCooldowns().tick();
                }

                MinecraftServer server = listener.server;
                server.getProfiler().push("keepAlive");
                long i = Util.getMillis();
                if (i - listener.keepAliveTime >= 15000L) {
                    if (listener.keepAlivePending) {
                        listener.disconnect(Component.translatable("disconnect.timeout"));
                    } else {
                        listener.keepAlivePending = true;
                        listener.keepAliveTime = i;
                        listener.keepAliveChallenge = i;
                        listener.send(new ClientboundKeepAlivePacket(listener.keepAliveChallenge));
                    }
                }

                server.getProfiler().pop();

                if (player.getLastActionTime() > 0L && server.getPlayerIdleTimeout() > 0 && Util.getMillis() - player.getLastActionTime() > (long) server.getPlayerIdleTimeout() * 1000L * 60L) {
                    listener.disconnect(Component.translatable("multiplayer.disconnect.idling"));
                }

                player.deathTime = time;
                return;
            }
        }

        instance.tick();
    }
}
