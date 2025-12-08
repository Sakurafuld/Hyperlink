package com.sakurafuld.hyperdaimc.network.paradox;

import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxCapabilityPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public record ClientboundParadoxSyncCapability(UUID uuid, CompoundTag tag) {
    public static void encode(ClientboundParadoxSyncCapability msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.uuid());
        buf.writeNbt(msg.tag());
    }

    public static ClientboundParadoxSyncCapability decode(FriendlyByteBuf buf) {
        return new ClientboundParadoxSyncCapability(buf.readUUID(), buf.readAnySizeNbt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        ClientLevel level = Objects.requireNonNull(Minecraft.getInstance().level);
        Objects.requireNonNull(level.getPlayerByUUID(this.uuid())).getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradox ->
                paradox.deserializeNBT(this.tag()));
    }
}
