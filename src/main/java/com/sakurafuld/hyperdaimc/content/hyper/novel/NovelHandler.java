package com.sakurafuld.hyperdaimc.content.hyper.novel;

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
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class NovelHandler {
    public static final Predicate<Entity> PREDICATE = entity -> {
        if (!entity.isRemoved() && !novelized(entity)) {
            if (entity instanceof Player player) {
                return player.getHealth() > 0;
            } else if (!HyperCommonConfig.NOVEL_IGNORE.get().isEmpty()) {
                return !HyperCommonConfig.NOVEL_IGNORE.get().contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString());
            } else {
                return true;
            }
        } else {
            return false;
        }
    };

    public static boolean novelized(Entity entity) {
        return HyperCommonConfig.ENABLE_NOVEL.get() && ((IEntityNovel) entity).isNovelized() && !(HyperCommonConfig.MUTEKI_NOVEL.get() && entity instanceof LivingEntity living && MutekiHandler.muteki(living));
    }

    public static boolean special(Entity entity) {
        return entity instanceof Player || HyperCommonConfig.NOVEL_SPECIAL.get().contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString());
    }

    @SubscribeEvent(receiveCanceled = true)
    @OnlyIn(Dist.CLIENT)
    public static void novel(InputEvent.InteractionKeyMappingTriggered event) {
        if (!HyperCommonConfig.ENABLE_NOVEL.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (event.isAttack() && mc.player.getMainHandItem().is(HyperItems.NOVEL.get())) {
            double reach = Math.max(mc.gameMode.getPickRange(), mc.player.getEntityReach());
            Vec3 view = mc.player.getViewVector(1);
            Vec3 vector = view.scale(reach);
            Vec3 eye = mc.player.getEyePosition().subtract(view);
            if (mc.player.isShiftKeyDown() == HyperCommonConfig.NOVEL_INVERT_SHIFT.get()) {
                List<Entity> entities = rayTraceEntities(mc.player, eye, eye.add(vector), mc.player.getBoundingBox().expandTowards(vector).inflate(1), 1);
                if (!entities.isEmpty()) {
                    event.setCanceled(true);
                    HyperConnection.INSTANCE.sendToServer(new ServerboundNovelize());
                }
            } else {
                List<Entity> entities = rayTraceEntities(mc.player, eye, eye.add(vector), mc.player.getBoundingBox().expandTowards(vector).inflate(1), 0);
                Optional<Entity> optional = entities.stream()
                        .min(Comparator.comparingDouble(entity -> entity.position().distanceToSqr(eye)));

                if (optional.isPresent()) {
                    event.setCanceled(true);
                    HyperConnection.INSTANCE.sendToServer(new ServerboundNovelize());
                }
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void hurt(LivingHurtEvent event) {
        if (event.getSource().getDirectEntity() instanceof LivingEntity writer && writer.level() instanceof ServerLevel level && writer.getMainHandItem().is(HyperItems.NOVEL.get())) {
            LOG.debug("HurtNovelize");
            event.setCanceled(true);
            NovelHandler.novelize(writer, event.getEntity(), true);
            NovelHandler.playSound(level, event.getEntity().position());
        }
    }

    public static void novelize(LivingEntity writer, Entity victim, boolean send) {
        if (!HyperCommonConfig.ENABLE_NOVEL.get()) {
            return;
        }

        if (victim instanceof PartEntity<?> part) {
            novelize(writer, part.getParent(), send);
        }

        ((IEntityNovel) victim).novelize(writer);

        if (send) {
            HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> victim), new ClientboundNovelize(writer.getId(), victim.getId(), 1));
        }
    }

    public static List<Entity> rayTraceEntities(Entity owner, Vec3 start, Vec3 end, AABB area, float adjust) {
        List<Entity> entities = new ArrayList<>();

        for (Entity entity : owner.level().getEntities(owner, area, PREDICATE)) {
            AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius()).inflate(adjust);
            Optional<Vec3> optional = aabb.clip(start, end);
            optional.ifPresent(hit -> entities.add(entity));
        }
        return entities;
    }

    public static void playSound(ServerLevel level, Vec3 position) {
        level.playSound(null, position.x(), position.y(), position.z(), HyperSounds.NOVEL.get(), SoundSource.PLAYERS, 0.5f, 1.25f);
    }
}
