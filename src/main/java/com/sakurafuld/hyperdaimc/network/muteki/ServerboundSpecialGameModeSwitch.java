package com.sakurafuld.hyperdaimc.network.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record ServerboundSpecialGameModeSwitch(GameType mode) {
    public static void encode(ServerboundSpecialGameModeSwitch msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.mode);
    }

    public static ServerboundSpecialGameModeSwitch decode(FriendlyByteBuf buf) {
        return new ServerboundSpecialGameModeSwitch(buf.readEnum(GameType.class));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
            if (MutekiHandler.muteki(player)) {
                MutekiHandler.specialGameModeSwitch = true;
                Objects.requireNonNull(player.getServer()).getCommands().performPrefixedCommand(player.createCommandSourceStack(), "gamemode " + this.mode.getName());
                MutekiHandler.specialGameModeSwitch = false;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
