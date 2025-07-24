package com.sakurafuld.hyperdaimc.network.novel;

import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;
import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class ClientboundNovelize {
    private final int writer;
    private final int victim;
    private final int page;


    public ClientboundNovelize(int writer, int victim, int page) {
        this.writer = writer;
        this.victim = victim;
        this.page = page;
    }

    public static void encode(ClientboundNovelize msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.writer);
        buf.writeVarInt(msg.victim);
        buf.writeVarInt(msg.page);
    }

    public static ClientboundNovelize decode(FriendlyByteBuf buf) {
        return new ClientboundNovelize(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> require(LogicalSide.CLIENT).run(this::handle));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handle() {
        Entity mob = Minecraft.getInstance().level.getEntity(this.writer);
        Entity victim = Minecraft.getInstance().level.getEntity(this.victim);
        if (mob instanceof LivingEntity living && victim != null) {
            for (int pen = 0; pen < this.page && !NovelHandler.novelized(victim); pen++) {
                if (victim instanceof LocalPlayer player) {
                    LOG.debug("clientSkullNovelizePlayer:{}", player.shouldShowDeathScreen());
                }
                NovelHandler.novelize(living, victim, false);
            }
        }
    }
}
