package com.sakurafuld.hyperdaimc.network.chronicle;

import com.sakurafuld.hyperdaimc.content.hyper.chronicle.system.ChronicleHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundChronicleHitEffect(BlockPos pos) {
    public static void encode(ClientboundChronicleHitEffect msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos());
    }

    public static ClientboundChronicleHitEffect decode(FriendlyByteBuf buf) {
        return new ClientboundChronicleHitEffect(buf.readBlockPos());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        ChronicleHandler.hitEffect(this.pos());
    }
}
