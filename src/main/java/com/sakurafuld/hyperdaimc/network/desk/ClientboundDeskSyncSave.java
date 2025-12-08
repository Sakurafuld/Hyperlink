package com.sakurafuld.hyperdaimc.network.desk;

import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundDeskSyncSave(CompoundTag tag) {
    public static void encode(ClientboundDeskSyncSave msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.tag);
    }

    public static ClientboundDeskSyncSave decode(FriendlyByteBuf buf) {
        return new ClientboundDeskSyncSave(buf.readNbt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        DeskSavedData.get(Minecraft.getInstance().level).load(this.tag);
    }
}
