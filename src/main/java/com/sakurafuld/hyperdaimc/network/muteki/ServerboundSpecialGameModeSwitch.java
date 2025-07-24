package com.sakurafuld.hyperdaimc.network.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundSpecialGameModeSwitch {
    private final String command;

    public ServerboundSpecialGameModeSwitch(String command) {
        this.command = command;
    }

    public static void encode(ServerboundSpecialGameModeSwitch msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.command);
    }

    public static ServerboundSpecialGameModeSwitch decode(FriendlyByteBuf buf) {
        return new ServerboundSpecialGameModeSwitch(buf.readUtf());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            MutekiHandler.specialGameModeSwitch = true;
            player.getServer().getCommands().performCommand(player.createCommandSourceStack(), this.command);
            MutekiHandler.specialGameModeSwitch = false;
        });
        ctx.get().setPacketHandled(true);
    }
}
