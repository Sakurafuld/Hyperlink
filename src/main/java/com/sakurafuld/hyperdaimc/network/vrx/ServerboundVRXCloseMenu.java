package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundVRXCloseMenu {
    private final int id;

    public ServerboundVRXCloseMenu(int id) {
        this.id = id;
    }

    public static void encode(ServerboundVRXCloseMenu msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.id);
    }

    public static ServerboundVRXCloseMenu decode(FriendlyByteBuf buf) {
        return new ServerboundVRXCloseMenu(buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player.containerMenu instanceof VRXMenu menu && menu.containerId == this.id) {
                menu.closedByKey(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
