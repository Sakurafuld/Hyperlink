package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundVRXSound {
    private final Vec3 position;
    private final boolean create;

    public ServerboundVRXSound(Vec3 position, boolean create) {
        this.position = position;
        this.create = create;
    }

    public static void encode(ServerboundVRXSound msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.position.x());
        buf.writeDouble(msg.position.y());
        buf.writeDouble(msg.position.z());
        buf.writeBoolean(msg.create);
    }

    public static ServerboundVRXSound decode(FriendlyByteBuf buf) {
        return new ServerboundVRXSound(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            VRXHandler.playSound(player.serverLevel(), this.position, this.create);
        });
        ctx.get().setPacketHandled(true);
    }
}
