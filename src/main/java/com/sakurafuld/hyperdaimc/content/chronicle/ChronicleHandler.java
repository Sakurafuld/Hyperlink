package com.sakurafuld.hyperdaimc.content.chronicle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.paradox.ParadoxHandler;
import com.sakurafuld.hyperdaimc.helper.Boxes;
import com.sakurafuld.hyperdaimc.helper.Renders;
import com.sakurafuld.hyperdaimc.network.PacketHandler;
import com.sakurafuld.hyperdaimc.network.chronicle.ServerboundChronicleSound;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.sakurafuld.hyperdaimc.helper.Deets.*;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class ChronicleHandler {
    private static final Color COLOR_PAUSED = new Color(0xBB00FF88, true);
    private static final Color COLOR_SELECTION = new Color(0x880088BB, true);
    private static final Color COLOR_POINTED = new Color(0x5500AAAA, true);


    public static ThreadLocal<Boolean> chunkGenerating = ThreadLocal.withInitial(() -> false);
    public static boolean clientForceNonPaused = false;
    public static BlockPos selected = null;
    private static long lastRestart = 0;

    @SubscribeEvent
    public static void logIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            ChronicleSavedData.get(player.getLevel()).sync2Client(player);
        }
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            ChronicleSavedData.get(player.getLevel()).sync2Client(player);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void pauseAndRestart(InputEvent.ClickInputEvent event) {
        if (!HyperServerConfig.ENABLE_CHRONICLE.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player.getMainHandItem().is(HyperItems.CHRONICLE.get())) {

            BlockPos cursor = getCursorPos();
            ChronicleSavedData data = ChronicleSavedData.get(mc.level);
            if (event.isUseItem()) {
                if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof ItemFrame) {
                    return;
                }
                event.setCanceled(true);
                event.setSwingHand(true);

                if (selected == null) {
                    selected = cursor;
                    mc.level.playSound(mc.player, selected, HyperSounds.CHRONICLE_SELECT.get(), SoundSource.PLAYERS, 1, 2);
                } else if (data.pause(mc.player.getUUID(), selected, cursor, error -> mc.player.displayClientMessage(error, false))) {
                    data.sync2Server();
                    selected = null;
                    PacketHandler.INSTANCE.sendToServer(new ServerboundChronicleSound(Vec3.atCenterOf(cursor), false));
                }
            } else if (event.isAttack()) {
                if (selected != null) {
                    event.setCanceled(true);
                    selected = null;
                    lastRestart = Util.getMillis();
                    PacketHandler.INSTANCE.sendToServer(new ServerboundChronicleSound(Vec3.atCenterOf(cursor), true));
                } else if (Util.getMillis() - lastRestart > 200) {

                    BlockPos target;

                    Vec3 eye = mc.player.getEyePosition();
                    Vec3 view = eye.add(mc.player.getViewVector(1).multiply(4, 4, 4));

                    target = BlockGetter.traverseBlocks(eye, view, Unit.INSTANCE, (unit, current) -> {
                        Optional<List<ChronicleSavedData.Entry>> list = data.getPaused(current);
                        return list.isPresent() && list.get().stream().anyMatch(entry -> entry.uuid.equals(mc.player.getUUID())) ? current : null;
                    }, unit -> Boxes.INVALID);

                    if (target == Boxes.INVALID) {
                        if (mc.hitResult instanceof BlockHitResult result && result.getType() != HitResult.Type.MISS) {

                            Optional<List<ChronicleSavedData.Entry>> list = data.getPaused(result.getBlockPos());
                            target = list.isPresent() && list.get().stream().anyMatch(entry -> entry.uuid.equals(mc.player.getUUID())) ? result.getBlockPos() : Boxes.INVALID;
                        }
                    }

                    if (target != Boxes.INVALID) {
                        event.setCanceled(true);
                        data.restart(mc.player.getUUID(), target);
                        data.sync2Server();
                        lastRestart = Util.getMillis();
                        PacketHandler.INSTANCE.sendToServer(new ServerboundChronicleSound(Vec3.atCenterOf(cursor), true));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void render(RenderLevelStageEvent event) {
        if (!HyperServerConfig.ENABLE_CHRONICLE.get()) {
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();

        if (!mc.player.getMainHandItem().is(HyperItems.CHRONICLE.get()) && !mc.player.getOffhandItem().is(HyperItems.CHRONICLE.get())) {
            selected = null;
            return;
        }

        ChronicleSavedData data = ChronicleSavedData.get(mc.level);
        data.getEntries().stream()
                .sorted(Comparator.comparingInt(entry -> entry.uuid.equals(mc.player.getUUID()) ? -1 : 1))
                .filter(entry -> event.getFrustum().isVisible(entry.aabb))
                .forEach(entry -> Renders.with(poseStack, () -> {
                    AABB identity = Boxes.identity(entry.aabb);
                    boolean mine = entry.uuid.equals(mc.player.getUUID());
                    poseStack.translate(entry.aabb.minX - camera.x(), entry.aabb.minY - camera.y(), entry.aabb.minZ - camera.z());
                    Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity, COLOR_PAUSED.getRGB(), face -> true);
                    LevelRenderer.renderLineBox(poseStack, Renders.getBuffer(RenderType.lines()), identity, mine ? 1 : 0.8f, mine ? 0.8f : 0, mine ? 0 : 0.2f, 1);
                    Renders.endBatch(Renders.Type.HIGHLIGHT);
                    Renders.endBatch(RenderType.lines());
                }));

        BlockPos cursor = getCursorPos();

        if (selected == null) {
            if (mc.player.getMainHandItem().is(HyperItems.CHRONICLE.get())) {
                Renders.with(poseStack, () -> {
                    poseStack.translate(cursor.getX() - camera.x(), cursor.getY() - camera.y(), cursor.getZ() - camera.z());
                    Renders.cubeScaled(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), 1, COLOR_POINTED.getRGB(), face -> !isPaused(mc.level, cursor, null) || isPaused(mc.level, cursor.relative(face), null));
                    Renders.endBatch(Renders.Type.HIGHLIGHT);
                });
            }
        } else {
            AABB aabb = Boxes.of(selected, cursor);
            AABB identity = Boxes.identity(aabb);
            Renders.with(poseStack, () -> {
                poseStack.translate(aabb.minX - camera.x(), aabb.minY - camera.y(), aabb.minZ - camera.z());
                Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity, COLOR_SELECTION.getRGB(), face -> true);
                LevelRenderer.renderLineBox(poseStack, Renders.getBuffer(RenderType.lines()), identity, 0, 0.8f, 0.5f, 1);
                Renders.endBatch(Renders.Type.HIGHLIGHT);
            });
        }
    }

    @SubscribeEvent
    public static void pause(EntityJoinWorldEvent event) {
        // 微妙な実装です、、、.
        if ((event.getEntity() instanceof PrimedTnt || event.getEntity() instanceof FallingBlockEntity) && isPaused(event.getWorld(), event.getEntity().blockPosition(), null)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void pause(PlayerInteractEvent.LeftClickBlock event) {
        if (isPaused(event.getWorld(), event.getPos(), event.getPlayer())) {
            LOG.debug("ChronicleLeftBlock");
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void pause(PlayerInteractEvent.RightClickBlock event) {
        if (HyperServerConfig.CHRONICLE_INTERACT.get() && isPaused(event.getWorld(), event.getPos(), event.getPlayer())) {
            event.setCanceled(true);
            event.getPlayer().swing(event.getHand());
        }
    }

    //    @SubscribeEvent
//    public static void pause(PlayerEvent.BreakSpeed event) {
//        if(isPaused(event.getPlayer().getLevel(), event.getPos(), event.getPlayer()))  {
//            LOG.debug("ChronicleBreakSpeed");
//            event.setCanceled(true);
//        }
//    }
    @SubscribeEvent
    public static void pause(BlockEvent.BreakEvent event) {
        if (event.getWorld() instanceof Level level && isPaused(level, event.getPos(), event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void pause(BlockEvent.EntityPlaceEvent event) {
        if (event.getWorld() instanceof Level level && isPaused(level, event.getPos(), event.getEntity())) {
            BlockSnapshot snapshot = event.getBlockSnapshot();
            if (!snapshot.getReplacedBlock().is(snapshot.getCurrentBlock().getBlock())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void pause(BlockEvent.EntityMultiPlaceEvent event) {
        if (event.getWorld() instanceof Level level && isPaused(level, event.getPos(), event.getEntity())) {
            BlockSnapshot snapshot = event.getBlockSnapshot();
            if (!snapshot.getReplacedBlock().is(snapshot.getCurrentBlock().getBlock())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void pause(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getWorld() instanceof Level level && isPaused(level, event.getPos(), null)) {
            event.setCanceled(true);
        }
    }

    //    @SubscribeEvent
//    @SuppressWarnings("all")
//    public static void pause(BlockEvent.BlockToolInteractEvent event) {
//        if(event.getWorld() instanceof Level level && isPaused(level, event.getPos(), event.getPlayer())) {
//            event.setCanceled(true);
//        }
//    }
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void pause(BlockEvent.BlockToolModificationEvent event) {
//        if(event.getWorld() instanceof Level level && !event.getFinalState().is(event.getState().getBlock()) && isPaused(level, event.getPos(), event.getPlayer())) {
//            event.setFinalState(null);
//        }
//    }
//    @SubscribeEvent
//    public static void pause(ExplosionEvent.Detonate event) {
//        for(BlockPos pos : Lists.newArrayList(event.getAffectedBlocks())) {
//            if(isPaused(event.getWorld(), pos, null)) {
//                event.getAffectedBlocks().remove(pos);
//            }
//        }
//    }
    @SubscribeEvent
    public static void pause(PistonEvent.Pre event) {
        if (event.getWorld() instanceof Level level) {
            if (isPaused(level, event.getPos(), null)) {
                event.setCanceled(true);
            } else if (!event.getState().isAir() && isPaused(level, event.getFaceOffsetPos(), null)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void pause(LivingDestroyBlockEvent event) {
        if (isPaused(event.getEntityLiving().getLevel(), event.getPos(), event.getEntityLiving())) {
            event.setCanceled(true);
        }
    }

    public static boolean isPaused(Level level, BlockPos pos, @Nullable Entity entity) {
        if (!HyperServerConfig.ENABLE_CHRONICLE.get()) {
            return false;
        }
        if (chunkGenerating.get()) {
//            LOG.debug("isNotPausedChunkGenerating");
            return false;
        }
        if (side().isClient() && clientForceNonPaused) {
            return false;
        }
        if (HyperServerConfig.CHRONICLE_PARADOX.get() || ParadoxHandler.isNotParadox(entity)) {
            Optional<List<ChronicleSavedData.Entry>> optional = ChronicleSavedData.get(level).getPaused(pos);
            return optional.filter(list -> {
                if (HyperServerConfig.CHRONICLE_OWNER.get()) {
                    return true;
                } else if (!(entity instanceof Player)) {
//                    LOG.debug("isPausedNullOrNoPlayer:{}", entity);
                    return true;
                }
                if (!list.stream().allMatch(entry -> entry.uuid.equals(entity.getUUID()))) {
//                    LOG.debug("isPausedNoOwner");
                    return true;
                }
//                LOG.debug("isNotPaused");
                return false;
            }).isPresent();
        } else {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static BlockPos getCursorPos() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.hitResult == null || mc.hitResult.getType() == HitResult.Type.MISS) {
            Vec3 view = mc.player.getViewVector(1).multiply(4, 4, 4);
            return new BlockPos(mc.player.getEyePosition().add(view));
        } else if (mc.hitResult instanceof BlockHitResult hit) {
            if (mc.player.isShiftKeyDown() != HyperCommonConfig.CHRONICLE_INVERT_SHIFT.get()) {
                return hit.getBlockPos().immutable().relative(hit.getDirection());
            } else {
                return hit.getBlockPos().immutable();
            }
        } else {
            return new BlockPos(mc.hitResult.getLocation());
        }
    }
}
