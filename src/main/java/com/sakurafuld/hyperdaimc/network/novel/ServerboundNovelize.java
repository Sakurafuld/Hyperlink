package com.sakurafuld.hyperdaimc.network.novel;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

public class ServerboundNovelize {
    public static void encode(ServerboundNovelize msg, FriendlyByteBuf buf) {
    }

    public static ServerboundNovelize decode(FriendlyByteBuf buf) {
        return new ServerboundNovelize();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LOG.debug("handleNovelize");
            ServerPlayer player = ctx.get().getSender();
            ServerLevel level = player.serverLevel();

            if (player.getMainHandItem().is(HyperItems.NOVEL.get())) {
                double reach = Math.max(player.getBlockReach(), player.getEntityReach());
                if (player.isShiftKeyDown() == HyperCommonConfig.NOVEL_INVERT_SHIFT.get()) {
                    List<Entity> entities = Lists.newArrayList();
                    NovelHandler.rayTraceEntities(player, reach).stream()
                            .peek(entities::add)
                            .filter(NovelHandler.PREDICATE_SINGLE)
                            .min(Comparator.comparingDouble(player::distanceTo))
                            .ifPresent(entity -> NovelHandler.playSound(level, entity.position()));
                    for (Entity entity : entities) {
                        NovelHandler.novelize(player, entity, true);
                    }
                } else {
                    Entity entity = NovelHandler.rayTraceEntity(player, reach);
                    if (entity != null) {
                        NovelHandler.novelize(player, entity, true);
                        NovelHandler.playSound(level, entity.position());
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
