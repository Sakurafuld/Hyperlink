package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

@Deprecated
public class VulnerableServerboundVRXSyncSave {
    private final CompoundTag tag;

    public VulnerableServerboundVRXSyncSave(CompoundTag tag) {
        this.tag = tag;
    }

    public static void encode(VulnerableServerboundVRXSyncSave msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.tag);
    }

    public static VulnerableServerboundVRXSyncSave decode(FriendlyByteBuf buf) {
        return new VulnerableServerboundVRXSyncSave(buf.readNbt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        if (!HyperCommonConfig.VRX_VULNERABILIZATION.get()) {
            return;
        }
        ctx.get().enqueueWork(() -> {
            ServerLevel level = ctx.get().getSender().serverLevel();
            VRXSavedData data = VRXSavedData.get(level);
            data.load(this.tag);
            data.setDirty();
            data.sync2Client(PacketDistributor.DIMENSION.with(level::dimension));
        });
        ctx.get().setPacketHandled(true);
    }
}
