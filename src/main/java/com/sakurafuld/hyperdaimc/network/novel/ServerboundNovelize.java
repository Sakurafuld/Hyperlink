package com.sakurafuld.hyperdaimc.network.novel;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.Comparator;
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
                Vec3 view = player.getViewVector(1);
                Vec3 vector = view.scale(reach);
                Vec3 eye = player.getEyePosition();
                if (player.isShiftKeyDown() != HyperCommonConfig.NOVEL_INVERT_SHIFT.get()) {
                    NovelHandler.rayTraceEntities(player, eye, eye.add(vector), player.getBoundingBox().expandTowards(vector).inflate(1), 0).stream()
                            .min(Comparator.comparingDouble(entity -> entity.position().distanceToSqr(eye)))
                            .ifPresent(entity -> {
                                NovelHandler.novelize(player, entity, true);
                                NovelHandler.playSound(level, entity.position());
                            });
                } else {
                    NovelHandler.rayTraceEntities(player, eye, eye.add(vector), player.getBoundingBox().expandTowards(vector).inflate(1), 0.75f).stream()
                            .peek(entity -> NovelHandler.novelize(player, entity, true))
                            .min(Comparator.comparingDouble(player::distanceTo))
                            .ifPresent(entity -> NovelHandler.playSound(level, entity.position()));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
