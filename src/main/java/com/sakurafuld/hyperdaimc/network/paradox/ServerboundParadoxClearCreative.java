package com.sakurafuld.hyperdaimc.network.paradox;

import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxItem;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public record ServerboundParadoxClearCreative(int index) {
    public static void encode(ServerboundParadoxClearCreative msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.index());
    }

    public static ServerboundParadoxClearCreative decode(FriendlyByteBuf buf) {
        return new ServerboundParadoxClearCreative(buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
            if (player.isCreative()) {
                ItemStack stack = player.inventoryMenu.getSlot(this.index()).getItem();
                if (stack.is(HyperItems.PARADOX.get())) {
                    UUID uuid = ParadoxItem.getUUID(stack);
                    if (uuid != null) {
                        ParadoxSavedData data = ParadoxSavedData.getServer();
                        ParadoxItem.removeUUID(stack);
                        data.remove(uuid);
                        data.sync2Client(uuid);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
