package com.sakurafuld.hyperdaimc.network.chronicle;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

@Deprecated
public class VulnerableServerboundChronicleSyncSave {
    private final CompoundTag tag;

    public VulnerableServerboundChronicleSyncSave(CompoundTag tag) {
        this.tag = tag;
    }

    public static void encode(VulnerableServerboundChronicleSyncSave msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.tag);
    }

    public static VulnerableServerboundChronicleSyncSave decode(FriendlyByteBuf buf) {
        return new VulnerableServerboundChronicleSyncSave(buf.readNbt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        if (!HyperCommonConfig.CHRONICLE_VULNERABILIZATION.get()) {
            return;
        }
        ctx.get().enqueueWork(() -> {
            ServerLevel level = ctx.get().getSender().serverLevel();
            ChronicleSavedData data = ChronicleSavedData.get(level);
            data.load(this.tag);
            data.setDirty();
            data.sync2Client(PacketDistributor.DIMENSION.with(level::dimension));
        });
        ctx.get().setPacketHandled(true);
    }
}
