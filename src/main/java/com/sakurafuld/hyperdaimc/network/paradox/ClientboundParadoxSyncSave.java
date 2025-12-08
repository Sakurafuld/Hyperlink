package com.sakurafuld.hyperdaimc.network.paradox;

import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundParadoxSyncSave(CompoundTag tag) {
    public static void encode(ClientboundParadoxSyncSave msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.tag());
    }

    public static ClientboundParadoxSyncSave decode(FriendlyByteBuf buf) {
        return new ClientboundParadoxSyncSave(buf.readAnySizeNbt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        ParadoxSavedData.getClient().load(this.tag());
    }
}
