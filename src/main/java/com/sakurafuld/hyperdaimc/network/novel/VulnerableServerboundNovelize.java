package com.sakurafuld.hyperdaimc.network.novel;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.novel.system.NovelHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@Deprecated
public class VulnerableServerboundNovelize {
    private final int attacker;
    private final int victim;

    public VulnerableServerboundNovelize(int writer, int victim) {
        this.attacker = writer;
        this.victim = victim;
    }

    public static void encode(VulnerableServerboundNovelize msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.attacker);
        buf.writeVarInt(msg.victim);
    }

    public static VulnerableServerboundNovelize decode(FriendlyByteBuf buf) {
        return new VulnerableServerboundNovelize(buf.readVarInt(), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        if (!HyperCommonConfig.NOVEL_VULNERABILIZATION.get()) {
            return;
        }
        ctx.get().enqueueWork(() -> {
            Level level = ctx.get().getSender().level();
            Entity attacker = level.getEntity(this.attacker);
            Entity victim = level.getEntity(this.victim);
            if (attacker instanceof LivingEntity living && victim != null)
                NovelHandler.novelize(living, victim, true);
        });
        ctx.get().setPacketHandled(true);
    }
}
