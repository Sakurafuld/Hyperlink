package com.sakurafuld.hyperdaimc.network.paradox;

import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxCapabilityPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class ClientboundParadoxDelete {
    public static void encode(ClientboundParadoxDelete msg, FriendlyByteBuf buf) {
    }

    public static ClientboundParadoxDelete decode(FriendlyByteBuf buf) {
        return new ClientboundParadoxDelete();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        Objects.requireNonNull(Minecraft.getInstance().player).getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(ParadoxCapabilityPlayer::deleteSelection);
    }
}
