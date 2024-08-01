package com.sakurafuld.hyperdaimc.content.novel;

import com.sakurafuld.hyperdaimc.api.ILivingEntityNovel;
import com.sakurafuld.hyperdaimc.content.ModItems;
import com.sakurafuld.hyperdaimc.network.PacketHandler;
import com.sakurafuld.hyperdaimc.network.novel.C2SNovelKill;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sakurafuld.hyperdaimc.Deets.*;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class NovelHandler {
    @SubscribeEvent(receiveCanceled = true)
    @OnlyIn(Dist.CLIENT)
    public static void novel(InputEvent.ClickInputEvent event){
        Minecraft mc = Minecraft.getInstance();
        LOG.debug("isAttack={}-isNovel={}", event.isAttack(), mc.player.getMainHandItem().is(ModItems.NOVEL.get()));
        if(event.isAttack() && mc.player.getMainHandItem().is(ModItems.NOVEL.get())){
            event.setCanceled(true);
            Vec3 view = mc.player.getViewVector(1f);
            Vec3 eye = mc.player.getEyePosition();
            double reach = Math.max(mc.player.getReachDistance(), mc.player.getAttackRange());
            List<Entity> entities = rayTraceEntities(mc.player, eye, eye.add(view.x() * reach, view.y() * reach, view.z() * reach), mc.player.getBoundingBox().expandTowards(view.scale(reach)).inflate(1), 0.2f);
            if(!entities.isEmpty())
                for(Entity entity : entities){
                    novelKill(mc.player, entity);
                }
        }
    }

    public static void novelKill(LivingEntity from, Entity entity){
        LOG.debug("{}-novelKill={}", side(), entity.getDisplayName().getString());
        if(!(entity instanceof ILivingEntityNovel target)){
            if(entity instanceof PartEntity<?> part){
                entity = part.getParent();
                novelKill(from, entity);
                LOG.debug("{}-Part", side());
            }
            LOG.debug("{}-Entity", side());
            entity.kill();
            if(side().isClient())
                PacketHandler.INSTANCE.sendToServer(new C2SNovelKill(entity.getId()));
            return;
        }
        target.novelKill(from);
        if(side().isClient())
            PacketHandler.INSTANCE.sendToServer(new C2SNovelKill(entity.getId()));
    }
    public static List<Entity> rayTraceEntities(Entity owner, Vec3 start, Vec3 end, AABB aabb, float inflate) {
        List<Entity> entities = new ArrayList<>();

        for(Entity entity : owner.getLevel().getEntities(owner, aabb, entity -> true)) {
            AABB adjust = entity.getBoundingBox().inflate(inflate);
            Optional<Vec3> optional = adjust.clip(start, end);
            if (optional.isPresent()) {
                entities.add(entity);
            }
        }
        return entities;
    }
}
