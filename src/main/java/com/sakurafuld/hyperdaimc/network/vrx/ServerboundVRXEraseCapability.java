package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.content.vrx.VRXCapability;
import com.sakurafuld.hyperdaimc.content.vrx.VRXHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundVRXEraseCapability {
    private final int id;

    public ServerboundVRXEraseCapability(int id) {
        this.id = id;
    }

    public static void encode(ServerboundVRXEraseCapability msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.id);
    }

    public static ServerboundVRXEraseCapability decode(FriendlyByteBuf buf) {
        return new ServerboundVRXEraseCapability(buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            Entity entity = player.level().getEntity(this.id);
            if (entity != null) {
                entity.getCapability(VRXCapability.CAPABILITY).ifPresent(vrx -> {
                    vrx.erase(player.getUUID());
                    VRXHandler.playSound(player.serverLevel(), entity.position(), false);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
