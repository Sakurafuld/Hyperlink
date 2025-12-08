package com.sakurafuld.hyperdaimc.network.paradox;

import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxCapabilityPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("ClassCanBeRecord")
public class ClientboundParadoxCursor {
    private final Action action;
    private final BlockPos pos;

    private ClientboundParadoxCursor(Action action, BlockPos pos) {
        this.action = action;
        this.pos = pos;
    }

    public static ClientboundParadoxCursor select(BlockPos pos) {
        return new ClientboundParadoxCursor(Action.SELECT, pos);
    }

    public static ClientboundParadoxCursor unselect() {
        return new ClientboundParadoxCursor(Action.UNSELECT, BlockPos.ZERO);
    }

    public static ClientboundParadoxCursor unchain(BlockPos pos) {
        return new ClientboundParadoxCursor(Action.UNCHAIN, pos);
    }

    public static ClientboundParadoxCursor unselectAndChain(BlockPos pos) {
        return new ClientboundParadoxCursor(Action.UNSELECT_AND_CHAIN, pos);
    }

    public static void encode(ClientboundParadoxCursor msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.action);
        buf.writeBlockPos(msg.pos);
    }

    public static ClientboundParadoxCursor decode(FriendlyByteBuf buf) {
        return new ClientboundParadoxCursor(buf.readEnum(Action.class), buf.readBlockPos());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        player.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradox -> {
            switch (this.action) {
                case SELECT -> paradox.select(this.pos);
                case UNSELECT -> paradox.unselect();
                case UNCHAIN -> paradox.unchain(this.pos);
                case UNSELECT_AND_CHAIN -> paradox.unselectAndChain(this.pos);
            }
        });
    }

    enum Action {
        SELECT, UNSELECT, UNCHAIN, UNSELECT_AND_CHAIN
    }
}
