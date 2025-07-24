package com.sakurafuld.hyperdaimc.network.novel;

import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundNovelSound {
    private final Vec3 position;

    public ServerboundNovelSound(Vec3 position) {
        this.position = position;
    }

    public static void encode(ServerboundNovelSound msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.position.x());
        buf.writeDouble(msg.position.y());
        buf.writeDouble(msg.position.z());
    }

    public static ServerboundNovelSound decode(FriendlyByteBuf buf) {
        return new ServerboundNovelSound(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            NovelHandler.playSound(player.getLevel(), this.position);
        });
        ctx.get().setPacketHandled(true);
    }
}
