package com.sakurafuld.hyperdaimc.network.novel;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record ServerboundNovelize(IntOpenHashSet entities) {
    public static void encode(ServerboundNovelize msg, FriendlyByteBuf buf) {
        buf.writeCollection(msg.entities(), FriendlyByteBuf::writeVarInt);
    }

    public static ServerboundNovelize decode(FriendlyByteBuf buf) {
        return new ServerboundNovelize(buf.readCollection(IntOpenHashSet::new, FriendlyByteBuf::readVarInt));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
            ServerLevel level = player.serverLevel();

            if (player.getMainHandItem().is(HyperItems.NOVEL.get())) {
                double reach = Mth.square(HyperCommonConfig.NOVEL_REACH.get());
                if (reach == 0)
                    return;
                if (player.isShiftKeyDown() == HyperCommonConfig.NOVEL_INVERT_SHIFT.get()) {
                    if (!this.entities().isEmpty()) {
                        NovelHandler.captureAndTransfer(player, () -> {
                            Entity e = null;
                            double d = Double.MAX_VALUE;
                            for (int id : this.entities) {
                                Entity entity = level.getEntity(id);
                                if (entity == null || NovelHandler.novelized(entity))
                                    continue;
                                double distance = player.distanceToSqr(entity);
                                if (reach < distance)
                                    continue;
                                NovelHandler.novelize(player, entity, true);
                                if (distance < d) {
                                    e = entity;
                                    d = distance;
                                }
                            }

                            if (e != null)
                                NovelHandler.playSound(level, e.position());
                        });
                    }
                } else if (this.entities().size() == 1) {
                    Entity entity = level.getEntity(this.entities().intIterator().nextInt());
                    if (entity != null && !NovelHandler.novelized(entity)) {
                        double distance = player.distanceToSqr(entity);
                        if (reach < distance)
                            return;
                        NovelHandler.captureAndTransfer(player, () -> {
                            NovelHandler.novelize(player, entity, true);
                            NovelHandler.playSound(level, entity.position());
                        });
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
