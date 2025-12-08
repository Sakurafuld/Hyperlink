package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXMenu;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXOneItem;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record ServerboundVRXSetJeiItem(int id, int index, ItemStack stack) {
    public static void encode(ServerboundVRXSetJeiItem msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.id());
        buf.writeVarInt(msg.index());
        buf.writeItemStack(msg.stack(), false);
    }

    public static ServerboundVRXSetJeiItem decode(FriendlyByteBuf buf) {
        return new ServerboundVRXSetJeiItem(buf.readVarInt(), buf.readVarInt(), buf.readItem());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
            if (player.containerMenu instanceof VRXMenu menu && menu.containerId == this.id()) {
                if (VRXRegistry.cast(this.stack(), menu.getAvailableTypes()) instanceof VRXOneItem.Wrapper wrapper)
                    wrapper.accept(menu, this.index());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
