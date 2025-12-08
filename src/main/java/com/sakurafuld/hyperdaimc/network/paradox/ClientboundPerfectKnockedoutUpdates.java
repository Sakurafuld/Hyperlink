package com.sakurafuld.hyperdaimc.network.paradox;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record ClientboundPerfectKnockedoutUpdates(long[] longs) {

    public static void encode(ClientboundPerfectKnockedoutUpdates msg, FriendlyByteBuf buf) {
        buf.writeLongArray(msg.longs());
    }

    public static ClientboundPerfectKnockedoutUpdates decode(FriendlyByteBuf buf) {
        return new ClientboundPerfectKnockedoutUpdates(buf.readLongArray());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        ClientLevel level = Objects.requireNonNull(Minecraft.getInstance().level);
        BlockState state = Blocks.AIR.defaultBlockState();
        for (long pos : this.longs())
            level.setServerVerifiedBlockState(BlockPos.of(pos), state, Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_ALL);
    }
}
