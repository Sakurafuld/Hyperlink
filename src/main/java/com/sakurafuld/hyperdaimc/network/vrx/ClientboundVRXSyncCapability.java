package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class ClientboundVRXSyncCapability {
    private final int entity;
    private final CompoundTag tag;

    public ClientboundVRXSyncCapability(int entity, CompoundTag tag) {
        this.entity = entity;
        this.tag = tag;
    }

    public static void encode(ClientboundVRXSyncCapability msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entity);
        buf.writeNbt(msg.tag);
    }

    public static ClientboundVRXSyncCapability decode(FriendlyByteBuf buf) {
        return new ClientboundVRXSyncCapability(buf.readVarInt(), buf.readNbt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> require(LogicalSide.CLIENT).run(this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        Entity entity = Minecraft.getInstance().level.getEntity(this.entity);
        if (entity != null) {
            entity.getCapability(VRXCapability.CAPABILITY).ifPresent(vrx -> vrx.deserializeNBT(this.tag));
        }
    }
}
