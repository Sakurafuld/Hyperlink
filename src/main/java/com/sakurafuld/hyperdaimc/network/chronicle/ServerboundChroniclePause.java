package com.sakurafuld.hyperdaimc.network.chronicle;

import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleSavedData;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.system.ChronicleHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;
import java.util.function.Supplier;

public record ServerboundChroniclePause(BlockPos from, BlockPos to) {
    public static void encode(ServerboundChroniclePause msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.from);
        buf.writeBlockPos(msg.to);
    }

    public static ServerboundChroniclePause decode(FriendlyByteBuf buf) {
        return new ServerboundChroniclePause(buf.readBlockPos(), buf.readBlockPos());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
            if (player.getMainHandItem().is(HyperItems.CHRONICLE.get())) {
                ChronicleSavedData data = ChronicleSavedData.get(player.level());
                data.pause(player.getUUID(), this.from, this.to);
                data.sync2Client(PacketDistributor.DIMENSION.with(player.level()::dimension));
                ChronicleHandler.playSound(player.serverLevel(), Vec3.atCenterOf(this.to), true);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
