package com.sakurafuld.hyperdaimc.network.novel;

import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public record ClientboundNovelize(int writer, int victim, int page) {
    public static void encode(ClientboundNovelize msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.writer);
        buf.writeVarInt(msg.victim);
        buf.writeVarInt(msg.page);
    }

    public static ClientboundNovelize decode(FriendlyByteBuf buf) {
        return new ClientboundNovelize(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        ClientLevel level = Objects.requireNonNull(Minecraft.getInstance().level);
        Entity writer = level.getEntity(this.writer);
        Entity victim = level.getEntity(this.victim);
        if (writer instanceof LivingEntity living && victim != null)
            for (int pen = 0; pen < this.page && !NovelHandler.novelized(victim); pen++)
                NovelHandler.novelize(living, victim, false);
    }
}
