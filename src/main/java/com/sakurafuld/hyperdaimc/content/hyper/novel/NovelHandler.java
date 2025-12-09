package com.sakurafuld.hyperdaimc.content.hyper.novel;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.infrastructure.Calculates;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityNovel;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.novel.ClientboundNovelize;
import com.sakurafuld.hyperdaimc.network.novel.ServerboundNovelize;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class NovelHandler {
    public static final Predicate<Entity> PREDICATE = entity -> {
        if (entity.isRemoved() || novelized(entity))
            return false;
        else if (entity instanceof Player player)
            return player.getHealth() > 0;
        else
            return !HyperCommonConfig.NOVEL_IGNORE.get().contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString());
    };

    private static List<ItemStack> capturedItems = null;
    private static int capturedExperiences = -1;

    public enum RenderingLevel {
        NONE,
        UNIQUE,
        ALL;

        public boolean check() {
            return this.ordinal() <= HyperCommonConfig.NOVEL_RENDERING_LEVEL.get().ordinal();
        }
    }

    public static boolean novelized(Entity entity) {
        return HyperCommonConfig.ENABLE_NOVEL.get() && ((IEntityNovel) entity).hyperdaimc$isNovelized() && !(HyperCommonConfig.MUTEKI_NOVEL.get() && entity instanceof LivingEntity living && MutekiHandler.muteki(living));
    }

    public static boolean special(Entity entity) {
        return HyperCommonConfig.NOVEL_SPECIAL.get().contains(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString());
    }

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
            if (writer.getId() != victim.getId() && PREDICATE.test(victim)) {
                event.setCanceled(true);
                if (writer.level() instanceof ServerLevel level) {
                    NovelHandler.novelize(writer, victim, true);
                    NovelHandler.playSound(level, victim.position());
                }
            }
        }
    }

    public static void novelize(LivingEntity writer, Entity victim, boolean send) {
        if (!HyperCommonConfig.ENABLE_NOVEL.get())
            return;

        if (send)
            HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> victim), new ClientboundNovelize(writer.getId(), victim.getId(), 1));

        if (victim instanceof PartEntity<?> part)
            novelize(writer, part.getParent(), send);
        ((IEntityNovel) victim).hyperdaimc$novelize(writer);
    }

    public static IntOpenHashSet rayTraceEntities(Player player, double reach) {
        Vec3 view = player.getViewVector(1);
        Vec3 vector = view.scale(reach);

        Vec3 start = player.getEyePosition().subtract(view);
        Vec3 end = start.add(vector);
        AABB area = player.getBoundingBox()
                .expandTowards(view.scale(-1))
                .expandTowards(vector)
                .inflate(1);

        IntOpenHashSet entities = new IntOpenHashSet();
        for (Entity entity : player.level().getEntities(player, area, PREDICATE)) {
            AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius()).inflate(1);
            Optional<Vec3> optional = aabb.clip(start, end);
            optional.ifPresent(hit -> entities.add(entity.getId()));
        }

        return entities;
    }

    public static int rayTraceEntity(Player player, double reach) {
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
        for (Entity entity : player.level().getEntities(player, area, PREDICATE)) {
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

    public static void playSound(ServerLevel level, Vec3 position) {
        level.playSound(null, position.x(), position.y(), position.z(), HyperSounds.NOVEL.get(), SoundSource.PLAYERS, 0.5f, 1.25f);
    }

    public static void playSound(ServerLevel level, Player player, Vec3 position) {
        level.playSound(player, position.x(), position.y(), position.z(), HyperSounds.NOVEL.get(), SoundSource.PLAYERS, 0.5f, 1.25f);
        player.playNotifySound(HyperSounds.NOVEL.get(), SoundSource.PLAYERS, 0.375f, 1.25f);
    }

    @SubscribeEvent
    public static void capture(EntityJoinLevelEvent event) {
        if (!HyperCommonConfig.ENABLE_NOVEL.get()) return;

        if (!event.getLevel().isClientSide() && !event.loadedFromDisk() && event.getEntity().isAlive()) {
            if (capturedItems != null) {
                ItemStack stack = ItemStack.EMPTY;
                if (event.getEntity() instanceof ItemEntity entity) {
                    event.setCanceled(true);
                    entity.discard();
                    stack = entity.getItem();
                }

                if (event.getEntity() instanceof FallingBlockEntity entity) {
                    event.setCanceled(true);
                    entity.discard();
                    stack = entity.getBlockState().getBlock().asItem().getDefaultInstance();
                }

                if (stack.isStackable()) {
                    for (ItemStack existing : capturedItems) {
                        if (stack.getItem() != existing.getItem()) continue;
                        if (!existing.isStackable()) continue;
                        if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) continue;

                        int space = existing.getMaxStackSize() - existing.getCount();
                        if (space <= 0) continue;

                        int growth = Math.min(stack.getCount(), space);
                        existing.grow(growth);
                        stack = ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - growth);
                        break;
                    }
                }

                if (!stack.isEmpty()) capturedItems.add(stack);
            }

            if (capturedExperiences >= 0 && event.getEntity() instanceof ExperienceOrb entity) {
                event.setCanceled(true);
                entity.discard();
                capturedExperiences += entity.getValue();
            }
        }
    }

    public static void captureAndTransfer(ServerPlayer player, Runnable runnable) {
        capturedItems = new ObjectArrayList<>();
        capturedExperiences = 0;

        runnable.run();

        List<ItemStack> items = capturedItems;
        int experiences = capturedExperiences;
        capturedItems = null;
        capturedExperiences = -1;

        ServerLevel level = player.serverLevel();
        if (!items.isEmpty()) {
            items.sort(Calculates.LOWEST_TO_HIGHEST);
            for (ItemStack item : items) {
                ItemEntity entity = new ItemEntity(level, player.getX(), player.getEyeY(), player.getZ(), item, 0, 0, 0);
                entity.setNoPickUpDelay();
                level.addFreshEntity(entity);
                entity.playerTouch(player);
            }
        }

        if (experiences > 0) {
            ExperienceOrb experience = new ExperienceOrb(level, player.getX(), player.getY(), player.getZ(), experiences);
            level.addFreshEntity(experience);
            player.takeXpDelay = 0;
            experience.playerTouch(player);
        }
    }
}
