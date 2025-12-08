package com.sakurafuld.hyperdaimc.network.chronicle;

import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleHandler;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;
import java.util.function.Supplier;

public record ServerboundChronicleRestart(BlockPos pos) {
    public static void encode(ServerboundChronicleRestart msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static ServerboundChronicleRestart decode(FriendlyByteBuf buf) {
        return new ServerboundChronicleRestart(buf.readBlockPos());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
            if (player.getMainHandItem().is(HyperItems.CHRONICLE.get())) {
                ChronicleSavedData data = ChronicleSavedData.get(player.level());
                data.restart(player.getUUID(), this.pos);
                data.sync2Client(PacketDistributor.DIMENSION.with(player.level()::dimension));
                ChronicleHandler.playSound(player.serverLevel(), Vec3.atCenterOf(this.pos), false);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
