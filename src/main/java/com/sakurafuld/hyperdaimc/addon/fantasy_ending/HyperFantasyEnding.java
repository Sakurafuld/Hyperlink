package com.sakurafuld.hyperdaimc.addon.fantasy_ending;

import com.mega.uom.event.entity.CatchActuallyHurt0Event;
import com.mega.uom.util.data.LivingEntityExpandedContext;
import com.mega.uom.util.entity.EntityASMUtil;
import com.mega.uom.util.entity.EntityActuallyHurt;
import com.mega.uom.util.itf.LivingEntityEC;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.infrastructure.addon.AddonMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.FANTASY_ENDING;

@AddonMod(FANTASY_ENDING)
public class HyperFantasyEnding {
    public HyperFantasyEnding() {
        MinecraftForge.EVENT_BUS.addListener(this::actuallyHurt);
        MinecraftForge.EVENT_BUS.addListener(this::tick);
    }

    private void actuallyHurt(CatchActuallyHurt0Event event) {
        if (MutekiHandler.muteki(event.getEntity())) {
            event.setAmount(0);
            event.setCanceled(true);
        }
    }

    private void tick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (MutekiHandler.muteki(entity)) {
            float delta = EntityASMUtil.getHealthDelta(entity);
            if (delta != 0) {
                EntityActuallyHurt.catchSetTrueHealth(entity, entity.getHealth() + Math.abs(delta));
                EntityASMUtil.setHealthDelta(entity, 0);
            }
            LivingEntityExpandedContext context = ((LivingEntityEC) entity).uom$livingECData();
            context.isDead = false;
            entity.getTags().remove("deAddedKillsCount");
        }
    }
}
