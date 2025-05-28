package com.sakurafuld.hyperdaimc.content.novel;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.api.mixin.IEntityNovel;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.network.PacketHandler;
import com.sakurafuld.hyperdaimc.network.novel.ServerboundNovelSound;
import com.sakurafuld.hyperdaimc.network.novel.ServerboundNovelize;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class NovelHandler {
    private static final Predicate<Entity> PREDICATE = entity -> {
        if (!entity.isRemoved() && entity.getPose() != Pose.DYING) {
            if (entity instanceof Player player) {
                return player.getHealth() > 0;
            } else if (!HyperServerConfig.NOVEL_IGNORE.get().isEmpty()) {
                return !HyperServerConfig.NOVEL_IGNORE.get().contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString());
            } else {
                return true;
            }
        } else {
            return false;
        }
    };

    public static boolean novelized(Entity entity) {
        return ((IEntityNovel) entity).isNovelized();
    }

    public static boolean special(Entity entity) {
        return HyperServerConfig.NOVEL_SPECIAL.get().contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString());
    }

    @SubscribeEvent(receiveCanceled = true)
    @OnlyIn(Dist.CLIENT)
    public static void novel(InputEvent.InteractionKeyMappingTriggered event) {
        if (!HyperServerConfig.ENABLE_NOVEL.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (event.isAttack() && mc.player.getMainHandItem().is(HyperItems.NOVEL.get())) {
            Vec3 view = mc.player.getViewVector(1);
            Vec3 eye = mc.player.getEyePosition();
            double reach = Math.max(mc.gameMode.getPickRange(), mc.player.getEntityReach());
            if (mc.player.isShiftKeyDown() != HyperCommonConfig.NOVEL_INVERT_SHIFT.get()) {

                List<Entity> entities = rayTraceEntities(mc.player, eye, eye.add(view.x() * reach, view.y() * reach, view.z() * reach), mc.player.getBoundingBox().expandTowards(view.scale(reach)).inflate(1), 0);
                Optional<Entity> optional = entities.stream()
                        .min(Comparator.comparingDouble(entity -> entity.position().distanceToSqr(eye)));

                if (optional.isPresent()) {
                    event.setCanceled(true);
                    Entity entity = optional.get();
                    novelize(mc.player, entity, true);
                    PacketHandler.INSTANCE.sendToServer(new ServerboundNovelSound(entity.position()));
                }
            } else {
                List<Entity> entities = rayTraceEntities(mc.player, eye.subtract(view.x(), view.y(), view.z()), eye.add(view.x() * reach, view.y() * reach, view.z() * reach), mc.player.getBoundingBox().expandTowards(view.scale(reach)).inflate(1), 0.75f);
                if (!entities.isEmpty()) {
                    event.setCanceled(true);
                    entities.forEach(entity ->
                            novelize(mc.player, entity, true));
                    entities.stream()
                            .min(Comparator.comparingDouble(entity -> mc.player.distanceTo(entity)))
                            .ifPresent(entity ->
                                    PacketHandler.INSTANCE.sendToServer(new ServerboundNovelSound(entity.position())));
                }
            }
        }
    }

    public static void novelize(LivingEntity writer, Entity victim, boolean send) {
        if (!HyperServerConfig.ENABLE_NOVEL.get()) {
            return;
        }

        if (victim instanceof PartEntity<?> part) {
            novelize(writer, part.getParent(), send);
        } else {
            ((IEntityNovel) victim).novelize(writer);
        }

        if (send) {
            PacketHandler.INSTANCE.sendToServer(new ServerboundNovelize(writer.getId(), victim.getId()));
        }
    }

    public static List<Entity> rayTraceEntities(Entity owner, Vec3 start, Vec3 end, AABB area, float adjust) {
        List<Entity> entities = new ArrayList<>();

        for (Entity entity : owner.level().getEntities(owner, area, PREDICATE)) {
            AABB aabb = entity.getBoundingBox().inflate(adjust);
            Optional<Vec3> optional = aabb.clip(start, end);
            optional.ifPresent(hit -> entities.add(entity));
        }
        return entities;
    }

    public static void playSound(ServerLevel level, Vec3 position) {
        level.playSound(null, position.x(), position.y(), position.z(), HyperSounds.NOVEL.get(), SoundSource.PLAYERS, 0.5f, 1.25f);
    }
}
