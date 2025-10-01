package com.sakurafuld.hyperdaimc.content.hyper.novel;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.api.mixin.IEntityNovel;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.novel.ClientboundNovelize;
import com.sakurafuld.hyperdaimc.network.novel.ServerboundNovelize;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class NovelHandler {
    public static final Predicate<Entity> PREDICATE_SINGLE = entity -> {
        if (entity.isRemoved() || novelized(entity)) {
            return false;
        } else {
            if (entity instanceof Player player) {
                return player.getHealth() > 0;
            } else {
                return !HyperCommonConfig.NOVEL_IGNORE.get().contains(entity.getType().getRegistryName().toString());
            }
        }
    };
    public static final Predicate<Entity> PREDICATE_MULTIPLE = entity -> {
        if (entity.isRemoved()) {
            return false;
        } else {
            if (entity instanceof Player player) {
                return player.getHealth() > 0;
            } else {
                return !HyperCommonConfig.NOVEL_IGNORE.get().contains(entity.getType().getRegistryName().toString());
            }
        }
    };

    public static boolean novelized(Entity entity) {
        return HyperCommonConfig.ENABLE_NOVEL.get() && ((IEntityNovel) entity).isNovelized() && !(HyperCommonConfig.MUTEKI_NOVEL.get() && entity instanceof LivingEntity living && MutekiHandler.muteki(living));
    }

    public static boolean special(Entity entity) {
        return entity instanceof Player || HyperCommonConfig.NOVEL_SPECIAL.get().contains(entity.getType().getRegistryName().toString());
    }

    @SubscribeEvent(receiveCanceled = true)
    @OnlyIn(Dist.CLIENT)
    public static void novel(InputEvent.ClickInputEvent event) {
        if (!HyperCommonConfig.ENABLE_NOVEL.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (event.isAttack() && mc.player.getMainHandItem().is(HyperItems.NOVEL.get())) {
            double reach = Math.max(mc.gameMode.getPickRange(), mc.player.getAttackRange());
            if (mc.player.isShiftKeyDown() == HyperCommonConfig.NOVEL_INVERT_SHIFT.get()) {
                if (!rayTraceEntities(mc.player, reach).isEmpty()) {
                    event.setCanceled(true);
                    HyperConnection.INSTANCE.sendToServer(new ServerboundNovelize());
                }
            } else if (rayTraceEntity(mc.player, reach) != null) {
                event.setCanceled(true);
                HyperConnection.INSTANCE.sendToServer(new ServerboundNovelize());
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void hurt(LivingAttackEvent event) {
        if (event.getSource().getDirectEntity() instanceof LivingEntity writer && writer.getMainHandItem().is(HyperItems.NOVEL.get())) {
            LOG.debug("HurtNovelize");
            event.setCanceled(true);
            if (writer.getLevel() instanceof ServerLevel level) {
                NovelHandler.novelize(writer, event.getEntity(), true);
                NovelHandler.playSound(level, event.getEntityLiving().position());
            }
        }
    }

    public static void novelize(LivingEntity writer, Entity victim, boolean send) {
        if (!HyperCommonConfig.ENABLE_NOVEL.get()) {
            return;
        }

        if (send) {
            HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> victim), new ClientboundNovelize(writer.getId(), victim.getId(), 1));
        }

        if (victim instanceof PartEntity<?> part) {
            novelize(writer, part.getParent(), send);
        }

        ((IEntityNovel) victim).novelize(writer);
    }

    public static List<Entity> rayTraceEntities(Player player, double reach) {
        Vec3 view = player.getViewVector(1);
        Vec3 vector = view.scale(reach);

        Vec3 start = player.getEyePosition().subtract(view);
        Vec3 end = start.add(vector);
        AABB area = player.getBoundingBox()
                .expandTowards(view.scale(-1))
                .expandTowards(vector)
                .inflate(1);

        List<Entity> entities = Lists.newArrayList();
        for (Entity entity : player.getLevel().getEntities(player, area, PREDICATE_MULTIPLE)) {
            AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius()).inflate(1);
            if (aabb.contains(start) || aabb.contains(end)) {
                entities.add(entity);
            } else {
                Optional<Vec3> optional = aabb.clip(start, end);
                optional.ifPresent(hit -> entities.add(entity));
            }
        }
        return entities;
    }

    public static Entity rayTraceEntity(Player player, double reach) {
        Vec3 view = player.getViewVector(1);
        Vec3 vector = view.scale(reach);

        Vec3 start = player.getEyePosition().subtract(view);
        Vec3 end = start.add(vector);
        AABB area = player.getBoundingBox()
                .expandTowards(view.scale(-1))
                .expandTowards(vector)
                .inflate(1);

        Entity e = null;
        double d = Double.MAX_VALUE;
        for (Entity entity : player.getLevel().getEntities(player, area, PREDICATE_SINGLE)) {
            AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> optional = aabb.clip(start, end);
            if (optional.isPresent()) {
                double distance = start.distanceToSqr(optional.get());
                if (distance < d) {
                    e = entity;
                    d = distance;
                }
            }
        }
        return e;
    }

    public static void playSound(ServerLevel level, Vec3 position) {
        level.playSound(null, position.x(), position.y(), position.z(), HyperSounds.NOVEL.get(), SoundSource.PLAYERS, 0.5f, 1.25f);
    }
}
