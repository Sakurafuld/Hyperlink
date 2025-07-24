package com.sakurafuld.hyperdaimc.network.desk;

import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskSavedData;
import com.sakurafuld.hyperdaimc.helper.Deets;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class ClientboundDeskSyncSave {
    private final CompoundTag tag;

    public ClientboundDeskSyncSave(CompoundTag tag) {
        this.tag = tag;
    }

    public static void encode(ClientboundDeskSyncSave msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.tag);
    }

    public static ClientboundDeskSyncSave decode(FriendlyByteBuf buf) {
        return new ClientboundDeskSyncSave(buf.readNbt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> require(LogicalSide.CLIENT).run(this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        Deets.LOG.info("handlerDeskSync2Client");
        DeskSavedData.get(Minecraft.getInstance().level).load(this.tag);
    }
}
