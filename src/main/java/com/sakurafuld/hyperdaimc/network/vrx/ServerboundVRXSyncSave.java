package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.content.vrx.VRXSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundVRXSyncSave {
    private final CompoundTag tag;

    public ServerboundVRXSyncSave(CompoundTag tag) {
        this.tag = tag;
    }

    public static void encode(ServerboundVRXSyncSave msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.tag);
    }

    public static ServerboundVRXSyncSave decode(FriendlyByteBuf buf) {
        return new ServerboundVRXSyncSave(buf.readNbt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerLevel level = ctx.get().getSender().serverLevel();
            VRXSavedData data = VRXSavedData.get(level);
            data.load(this.tag);
            data.setDirty();
            data.sync2Client(level::dimension);
        });
        ctx.get().setPacketHandled(true);
    }
}
