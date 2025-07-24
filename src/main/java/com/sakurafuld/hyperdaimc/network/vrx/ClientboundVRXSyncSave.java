package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class ClientboundVRXSyncSave {
    private final CompoundTag tag;

    public ClientboundVRXSyncSave(CompoundTag tag) {
        this.tag = tag;
    }

    public static void encode(ClientboundVRXSyncSave msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.tag);
    }

    public static ClientboundVRXSyncSave decode(FriendlyByteBuf buf) {
        return new ClientboundVRXSyncSave(buf.readNbt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> require(LogicalSide.CLIENT).run(this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        VRXSavedData.get(Minecraft.getInstance().level).load(this.tag);
    }
}
