package com.sakurafuld.hyperdaimc.network.paradox;

import com.google.common.collect.Sets;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxCapabilityPlayer;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxChain;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public record ClientboundParadoxUnchainSet(Set<ParadoxChain> chains) {
    public static void encode(ClientboundParadoxUnchainSet msg, FriendlyByteBuf buf) {
        buf.writeCollection(msg.chains(), (b, chain) -> {
            b.writeBlockPos(chain.from);
            b.writeBlockPos(chain.to);
        });
    }

    public static ClientboundParadoxUnchainSet decode(FriendlyByteBuf buf) {
        return new ClientboundParadoxUnchainSet(buf.readCollection(i -> Sets.newHashSet(), b -> new ParadoxChain(b.readBlockPos(), b.readBlockPos())));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }


    @OnlyIn(Dist.CLIENT)
    private void handle() {
        Objects.requireNonNull(Minecraft.getInstance().player).getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradox -> {
            for (ParadoxChain chain : this.chains())
                paradox.unchain(chain);
        });
    }
}
