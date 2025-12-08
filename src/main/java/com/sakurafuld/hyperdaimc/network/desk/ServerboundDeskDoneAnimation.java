package com.sakurafuld.hyperdaimc.network.desk;

import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record ServerboundDeskDoneAnimation(int id, boolean done) {
    public static void encode(ServerboundDeskDoneAnimation msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.id);
        buf.writeBoolean(msg.done);
    }

    public static ServerboundDeskDoneAnimation decode(FriendlyByteBuf buf) {
        return new ServerboundDeskDoneAnimation(buf.readVarInt(), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Objects.requireNonNull(ctx.get().getSender()).containerMenu instanceof DeskMenu menu && menu.containerId == this.id)
                menu.canCraft = this.done;
        });
        ctx.get().setPacketHandled(true);
    }
}
