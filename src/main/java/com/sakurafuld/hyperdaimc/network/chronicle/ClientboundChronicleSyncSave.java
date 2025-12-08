package com.sakurafuld.hyperdaimc.network.chronicle;

import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundChronicleSyncSave(CompoundTag tag) {
    public static void encode(ClientboundChronicleSyncSave msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.tag);
    }

    public static ClientboundChronicleSyncSave decode(FriendlyByteBuf buf) {
        return new ClientboundChronicleSyncSave(buf.readNbt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        ChronicleSavedData.get(Minecraft.getInstance().level).load(this.tag());
    }
}
