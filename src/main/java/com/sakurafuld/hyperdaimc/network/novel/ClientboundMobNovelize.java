package com.sakurafuld.hyperdaimc.network.novel;

import com.sakurafuld.hyperdaimc.content.novel.NovelHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class ClientboundMobNovelize {
    private final int victim;
    private final int mob;

    public ClientboundMobNovelize(int mob, int victim) {
        this.mob = mob;
        this.victim = victim;
    }

    public static void encode(ClientboundMobNovelize msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.mob);
        buf.writeVarInt(msg.victim);
    }

    public static ClientboundMobNovelize decode(FriendlyByteBuf buf) {
        return new ClientboundMobNovelize(buf.readVarInt(), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> require(LogicalSide.CLIENT).run(this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        Entity mob = Minecraft.getInstance().level.getEntity(this.mob);
        Entity victim = Minecraft.getInstance().level.getEntity(this.victim);
        if (mob instanceof LivingEntity living && victim != null)
            NovelHandler.novelize(living, victim, false);
    }
}
