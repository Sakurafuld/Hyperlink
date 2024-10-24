package com.sakurafuld.hyperdaimc.content.muteki;

import com.sakurafuld.hyperdaimc.api.ILivingEntityNovel;
import com.sakurafuld.hyperdaimc.content.ModItems;
import com.sakurafuld.hyperdaimc.content.novel.NovelItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import static com.sakurafuld.hyperdaimc.Deets.*;
@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class MutekiHandler {
    private static LivingEntity novelized = null;

    public static Entity getNovelized() {
        return novelized;
    }
    public static void setNovelized(LivingEntity novelized) {
        MutekiHandler.novelized = novelized;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void mutekiDeath(LivingDeathEvent event){
        LivingEntity entity = event.getEntityLiving();
        if(MutekiHandler.getNovelized() != null && getNovelized().equals(entity)){
            ((ILivingEntityNovel) entity).novelHeartAttack();
            event.setCanceled(false);
            return;
        }
        if(!CuriosApi.getCuriosHelper().findCurios(entity, ModItems.MUTEKI.get()).isEmpty()){
            event.setCanceled(true);
        }
    }
}
