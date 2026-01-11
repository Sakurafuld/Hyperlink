package com.sakurafuld.hyperdaimc.content.hyper.novel.system;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.novel.ServerboundNovelize;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;
import java.util.Optional;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class NovelTrigger {
    @SubscribeEvent(receiveCanceled = true)
    @OnlyIn(Dist.CLIENT)
    public static void novel(InputEvent.InteractionKeyMappingTriggered event) {
        if (!HyperCommonConfig.ENABLE_NOVEL.get())
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = Objects.requireNonNull(mc.player);
        if (event.isAttack() && player.getMainHandItem().is(HyperItems.NOVEL.get())) {
            double reach = HyperCommonConfig.NOVEL_REACH.get();
            if (reach == 0)
                return;
            if (player.isShiftKeyDown() == HyperCommonConfig.NOVEL_INVERT_SHIFT.get()) {
                IntOpenHashSet entities = rayTraceEntities(player, reach);
                if (!entities.isEmpty()) {
                    event.setCanceled(true);
                    HyperConnection.INSTANCE.sendToServer(new ServerboundNovelize(entities));
                }
            } else {
                int entity = rayTraceEntity(player, reach);
                if (entity > 0) {
                    event.setCanceled(true);
                    HyperConnection.INSTANCE.sendToServer(new ServerboundNovelize(IntOpenHashSet.of(entity)));
                }
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void hurt(LivingAttackEvent event) {
        if (event.getSource().getDirectEntity() instanceof LivingEntity writer && writer.getMainHandItem().is(HyperItems.NOVEL.get())) {
            LivingEntity victim = event.getEntity();
            if (writer.getId() != victim.getId() && NovelHandler.PREDICATE.test(victim)) {
                event.setCanceled(true);
                if (writer.level() instanceof ServerLevel level) {
                    NovelHandler.novelize(writer, victim, true);
                    NovelHandler.playSound(level, victim.position());
                }
            }
        }
    }

    private static IntOpenHashSet rayTraceEntities(Player player, double reach) {
        Vec3 view = player.getViewVector(1);
        Vec3 vector = view.scale(reach);

        Vec3 start = player.getEyePosition().subtract(view);
        Vec3 end = start.add(vector);
        AABB area = player.getBoundingBox()
                .expandTowards(view.scale(-1))
                .expandTowards(vector)
                .inflate(1);

        IntOpenHashSet entities = new IntOpenHashSet();
        for (Entity entity : player.level().getEntities(player, area, NovelHandler.PREDICATE)) {
            AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius()).inflate(1);
            Optional<Vec3> optional = aabb.clip(start, end);
            optional.ifPresent(hit -> entities.add(entity.getId()));
        }

        return entities;
    }

    private static int rayTraceEntity(Player player, double reach) {
        Vec3 view = player.getViewVector(1);
        Vec3 vector = view.scale(reach);

        Vec3 start = player.getEyePosition().subtract(view);
        Vec3 end = start.add(vector);
        AABB area = player.getBoundingBox()
                .expandTowards(view.scale(-1))
                .expandTowards(vector)
                .inflate(1);

        int e = -1;
        double d = Double.MAX_VALUE;
        for (Entity entity : player.level().getEntities(player, area, NovelHandler.PREDICATE)) {
            AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> optional = aabb.clip(start, end);
            if (optional.isPresent()) {
                double distance = start.distanceToSqr(optional.get());
                if (distance < d) {
                    e = entity.getId();
                    d = distance;
                }
            }
        }

        return e;
    }
}
