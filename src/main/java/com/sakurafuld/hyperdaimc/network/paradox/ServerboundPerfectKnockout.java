package com.sakurafuld.hyperdaimc.network.paradox;

import com.sakurafuld.hyperdaimc.content.paradox.ParadoxHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

public class ServerboundPerfectKnockout {

    public ServerboundPerfectKnockout() {
    }

    public static void encode(ServerboundPerfectKnockout msg, FriendlyByteBuf buf) {
    }

    public static ServerboundPerfectKnockout decode(FriendlyByteBuf buf) {
        return new ServerboundPerfectKnockout();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LOG.debug("handlePerfectKnockout");
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ParadoxHandler.perfectKnockout(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
