package com.sakurafuld.hyperdaimc.network.desk;

import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class ClientboundDeskMinecraft {
    private final DeskSavedData.Entry entry;

    public ClientboundDeskMinecraft(DeskSavedData.Entry entry) {
        this.entry = entry;
    }

    public static void encode(ClientboundDeskMinecraft msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.entry.save());
    }

    public static ClientboundDeskMinecraft decode(FriendlyByteBuf buf) {
        return new ClientboundDeskMinecraft(DeskSavedData.Entry.load(buf.readNbt()));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> require(LogicalSide.CLIENT).run(this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        DeskSavedData.get(Minecraft.getInstance().level).getEntries().add(this.entry);
    }
}
