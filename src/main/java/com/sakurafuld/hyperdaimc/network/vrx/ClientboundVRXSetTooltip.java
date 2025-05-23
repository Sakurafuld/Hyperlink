package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.content.vrx.VRXMenu;
import com.sakurafuld.hyperdaimc.content.vrx.VRXOne;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class ClientboundVRXSetTooltip {
    private final int id;
    private final List<VRXOne> list;

    public ClientboundVRXSetTooltip(int id, List<VRXOne> list) {
        this.id = id;
        this.list = list;
    }

    public static void encode(ClientboundVRXSetTooltip msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.id);
        buf.writeCollection(msg.list, (buffer, vrxOne) -> buffer.writeNbt(vrxOne.serialize()));
    }

    public static ClientboundVRXSetTooltip decode(FriendlyByteBuf buf) {
        return new ClientboundVRXSetTooltip(buf.readVarInt(), buf.readCollection(ArrayList::new, buffer -> {
            CompoundTag tag = buffer.readNbt();
            VRXOne.Type type = VRXOne.Type.of(tag.getString("Type"));
            return type.load(tag.getCompound("Data"));
        }));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> require(LogicalSide.CLIENT).run(this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        if (Minecraft.getInstance().player.containerMenu instanceof VRXMenu menu && menu.containerId == this.id) {
            menu.setTooltip(this.list);
        }
    }
}
