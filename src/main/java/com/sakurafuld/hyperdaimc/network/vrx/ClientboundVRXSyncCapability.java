package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record ClientboundVRXSyncCapability(int entity, CompoundTag tag) {
    public static void encode(ClientboundVRXSyncCapability msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entity);
        buf.writeNbt(msg.tag);
    }

    public static ClientboundVRXSyncCapability decode(FriendlyByteBuf buf) {
        return new ClientboundVRXSyncCapability(buf.readVarInt(), buf.readNbt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        Entity entity = Objects.requireNonNull(Minecraft.getInstance().level).getEntity(this.entity);
        if (entity != null) {
            entity.getCapability(VRXCapability.TOKEN).ifPresent(vrx ->
                    vrx.deserializeNBT(this.tag));
        }
    }
}
