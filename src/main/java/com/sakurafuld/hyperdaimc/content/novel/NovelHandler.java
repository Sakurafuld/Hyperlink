package com.sakurafuld.hyperdaimc.content.novel;

import com.sakurafuld.hyperdaimc.api.ILivingEntityNovel;
import com.sakurafuld.hyperdaimc.content.ModItems;
import com.sakurafuld.hyperdaimc.network.PacketHandler;
import com.sakurafuld.hyperdaimc.network.novel.C2SNovelKill;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import static com.sakurafuld.hyperdaimc.Deets.*;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class NovelHandler {
    @SubscribeEvent(receiveCanceled = true)
    @OnlyIn(Dist.CLIENT)
    public static void novel(InputEvent.ClickInputEvent event){
        Minecraft mc = Minecraft.getInstance();
        LOG.debug("isAttack={}-isNovel={}", event.isAttack(), mc.player.getMainHandItem().is(ModItems.NOVEL.get()));
        if(event.isAttack() && mc.player.getMainHandItem().is(ModItems.NOVEL.get()) && mc.hitResult instanceof EntityHitResult hit && hit.getType() != HitResult.Type.MISS)
            novelKill(mc.player, hit.getEntity());
    }

    public static void novelKill(LivingEntity from, Entity entity){
        LOG.debug("{}-novelKill={}", side(), entity.getDisplayName().getString());
        if(!(entity instanceof ILivingEntityNovel target)){
            if(entity instanceof PartEntity<?> part){
                entity = part.getParent();
                novelKill(from, entity);
            }
            entity.kill();
            if(required(LogicalSide.CLIENT).ready())
                PacketHandler.INSTANCE.sendToServer(new C2SNovelKill(entity.getId()));
            return;
        }
        target.novelKill(from);
        if(required(LogicalSide.CLIENT).ready())
            PacketHandler.INSTANCE.sendToServer(new C2SNovelKill(entity.getId()));
    }
}
