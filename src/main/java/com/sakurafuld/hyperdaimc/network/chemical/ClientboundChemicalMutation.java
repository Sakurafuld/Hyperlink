package com.sakurafuld.hyperdaimc.network.chemical;

import com.sakurafuld.hyperdaimc.content.crafting.chemical.ChemicalHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record ClientboundChemicalMutation(int entity) {
    public static void encode(ClientboundChemicalMutation msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entity);
    }

    public static ClientboundChemicalMutation decode(FriendlyByteBuf buf) {
        return new ClientboundChemicalMutation(buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        if (Objects.requireNonNull(Minecraft.getInstance().level).getEntity(this.entity) instanceof Zombie zombie)
            zombie.getPersistentData().putInt(ChemicalHandler.TAG_MUTATION, 0);
    }
}
