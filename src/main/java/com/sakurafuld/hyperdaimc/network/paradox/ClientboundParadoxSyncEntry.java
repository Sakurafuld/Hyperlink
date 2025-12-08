package com.sakurafuld.hyperdaimc.network.paradox;

import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public record ClientboundParadoxSyncEntry(UUID uuid, @Nullable CompoundTag entry) {
    public static void encode(ClientboundParadoxSyncEntry msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.uuid);
        if (msg.entry == null)
            buf.writeBoolean(false);
        else {
            buf.writeBoolean(true);
            buf.writeNbt(msg.entry);
        }
    }

    public static ClientboundParadoxSyncEntry decode(FriendlyByteBuf buf) {
        return new ClientboundParadoxSyncEntry(buf.readUUID(), buf.readBoolean() ? buf.readAnySizeNbt() : null);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        ParadoxSavedData data = ParadoxSavedData.getClient();
        if (this.entry == null) data.remove(this.uuid);
        else data.set(this.uuid, this.entry);
    }
}
