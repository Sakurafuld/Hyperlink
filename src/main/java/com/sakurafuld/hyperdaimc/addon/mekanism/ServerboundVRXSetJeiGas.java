package com.sakurafuld.hyperdaimc.addon.mekanism;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXMenu;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXRegistry;
import mekanism.api.chemical.gas.GasStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record ServerboundVRXSetJeiGas(int id, int index, GasStack stack) {
    public static void encode(ServerboundVRXSetJeiGas msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.id());
        buf.writeVarInt(msg.index());
        msg.stack().writeToPacket(buf);
    }

    public static ServerboundVRXSetJeiGas decode(FriendlyByteBuf buf) {
        return new ServerboundVRXSetJeiGas(buf.readVarInt(), buf.readVarInt(), GasStack.readFromPacket(buf));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
            if (player.containerMenu instanceof VRXMenu menu && menu.containerId == this.id()) {
                if (VRXRegistry.cast(this.stack(), menu.getAvailableTypes()) instanceof VRXOneGas.Wrapper wrapper)
                    wrapper.accept(menu, this.index());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
