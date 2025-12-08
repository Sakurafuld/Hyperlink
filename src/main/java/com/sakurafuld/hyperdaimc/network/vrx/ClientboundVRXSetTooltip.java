package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXMenu;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXOne;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXType;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public record ClientboundVRXSetTooltip(int id, List<VRXOne> list) {
    public static void encode(ClientboundVRXSetTooltip msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.id);
        buf.writeCollection(msg.list, (buffer, vrxOne) -> buffer.writeNbt(vrxOne.serialize()));
    }

    public static ClientboundVRXSetTooltip decode(FriendlyByteBuf buf) {
        return new ClientboundVRXSetTooltip(buf.readVarInt(), buf.readCollection(ArrayList::new, buffer -> {
            CompoundTag tag = Objects.requireNonNull(buffer.readAnySizeNbt());
            return VRXType.deserializeStatic(tag);
        }));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        if (Objects.requireNonNull(Minecraft.getInstance().player).containerMenu instanceof VRXMenu menu && menu.containerId == this.id)
            menu.setTooltip(this.list);
    }
}
