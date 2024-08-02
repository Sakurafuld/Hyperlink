package com.sakurafuld.hyperdaimc.network.novel;

import com.sakurafuld.hyperdaimc.content.novel.NovelHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.Deets.LOG;

public class C2SNovelKill {
    private int ID;

    public C2SNovelKill(int id){
        this.ID = id;
    }

    public static void encode(C2SNovelKill msg, FriendlyByteBuf buf){
        buf.writeInt(msg.ID);
    }
    public static C2SNovelKill decode(FriendlyByteBuf buf){
        return new C2SNovelKill(buf.readInt());
    }
    public static void handle(C2SNovelKill msg, Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{
            LOG.debug("handleNovelKill");
            ServerPlayer player = ctx.get().getSender();
            Entity entity =  player.getLevel().getEntity(msg.ID);
            if(entity != null) NovelHandler.novelKill(player, entity);
        });
        ctx.get().setPacketHandled(true);
    }
}
