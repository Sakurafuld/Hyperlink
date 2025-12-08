package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXMenu;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXOneFluid;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record ServerboundVRXSetJeiFluid(int id, int index, FluidStack stack) {
    public static void encode(ServerboundVRXSetJeiFluid msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.id());
        buf.writeVarInt(msg.index());
        buf.writeFluidStack(msg.stack());
    }

    public static ServerboundVRXSetJeiFluid decode(FriendlyByteBuf buf) {
        return new ServerboundVRXSetJeiFluid(buf.readVarInt(), buf.readVarInt(), buf.readFluidStack());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
            if (player.containerMenu instanceof VRXMenu menu && menu.containerId == this.id()) {
                if (VRXRegistry.cast(this.stack(), menu.getAvailableTypes()) instanceof VRXOneFluid.Wrapper wrapper)
                    wrapper.accept(menu, this.index());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
