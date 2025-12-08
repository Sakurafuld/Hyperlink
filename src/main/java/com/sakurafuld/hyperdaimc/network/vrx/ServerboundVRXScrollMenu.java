package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXMenu;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record ServerboundVRXScrollMenu(int id, int index, double delta, boolean shiftDown) {
    public static void encode(ServerboundVRXScrollMenu msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.id);
        buf.writeVarInt(msg.index);
        buf.writeDouble(msg.delta);
        buf.writeBoolean(msg.shiftDown);
    }

    public static ServerboundVRXScrollMenu decode(FriendlyByteBuf buf) {
        return new ServerboundVRXScrollMenu(buf.readVarInt(), buf.readVarInt(), buf.readDouble(), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Objects.requireNonNull(ctx.get().getSender()).containerMenu instanceof VRXMenu menu && menu.containerId == this.id && menu.getSlot(this.index) instanceof VRXSlot slot)
                slot.scrolled(this.delta, this.shiftDown);
        });
        ctx.get().setPacketHandled(true);
    }
}
