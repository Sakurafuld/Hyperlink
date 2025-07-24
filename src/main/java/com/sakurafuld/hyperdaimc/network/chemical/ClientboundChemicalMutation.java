package com.sakurafuld.hyperdaimc.network.chemical;

import com.sakurafuld.hyperdaimc.content.crafting.chemical.ChemicalHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class ClientboundChemicalMutation {
    private final int entity;


    public ClientboundChemicalMutation(int entity) {
        this.entity = entity;
    }

    public static void encode(ClientboundChemicalMutation msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entity);
    }

    public static ClientboundChemicalMutation decode(FriendlyByteBuf buf) {
        return new ClientboundChemicalMutation(buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> require(LogicalSide.CLIENT).run(this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        if (Minecraft.getInstance().level.getEntity(this.entity) instanceof Zombie zombie) {
            zombie.getPersistentData().putInt(ChemicalHandler.TAG_MUTATION, 0);
        }
    }
}
