package com.sakurafuld.hyperdaimc.network.chronicle;

import com.sakurafuld.hyperdaimc.content.chronicle.ChronicleSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundChronicleSyncSave {
    private final CompoundTag tag;

    public ServerboundChronicleSyncSave(CompoundTag tag) {
        this.tag = tag;
    }

    public static void encode(ServerboundChronicleSyncSave msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.tag);
    }

    public static ServerboundChronicleSyncSave decode(FriendlyByteBuf buf) {
        return new ServerboundChronicleSyncSave(buf.readNbt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerLevel level = ctx.get().getSender().serverLevel();
            ChronicleSavedData data = ChronicleSavedData.get(level);
            data.load(this.tag);
            data.setDirty();
            data.sync2Client(level::dimension);
        });
        ctx.get().setPacketHandled(true);
    }
}
