package com.sakurafuld.hyperdaimc.network.novel;

import com.sakurafuld.hyperdaimc.content.novel.NovelHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

public class ServerboundNovelize {
    private final int attacker;
    private final int victim;

    public ServerboundNovelize(int writer, int victim) {
        this.attacker = writer;
        this.victim = victim;
    }

    public static void encode(ServerboundNovelize msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.attacker);
        buf.writeVarInt(msg.victim);
    }

    public static ServerboundNovelize decode(FriendlyByteBuf buf) {
        return new ServerboundNovelize(buf.readVarInt(), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LOG.debug("handleNovelize");
            Level level = ctx.get().getSender().level();
            Entity attacker = level.getEntity(this.attacker);
            Entity victim = level.getEntity(this.victim);
            if (attacker instanceof LivingEntity living && victim != null)
                NovelHandler.novelize(living, victim, false);
        });
        ctx.get().setPacketHandled(true);
    }
}
