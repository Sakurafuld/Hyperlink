package com.sakurafuld.hyperdaimc.content.hyper.chronicle;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.handler.ParadoxHandler;
import com.sakurafuld.hyperdaimc.infrastructure.Boxes;
import com.sakurafuld.hyperdaimc.infrastructure.Calculates;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.chronicle.ClientboundChronicleHitEffect;
import com.sakurafuld.hyperdaimc.network.chronicle.ServerboundChroniclePause;
import com.sakurafuld.hyperdaimc.network.chronicle.ServerboundChronicleRestart;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class ChronicleHandler {
    public static boolean chunkGenerating = false;
    public static boolean clientForceNonPaused = false;
    public static BlockPos selected = null;
    private static long lastRestart = 0;
    private static long lastHanded = -1;
    private static long lastUnHanded = -1;
    public static Long2LongOpenHashMap hits = new Long2LongOpenHashMap();

    public static void hitEffect(BlockPos pos) {
        hits.put(pos.asLong(), Util.getMillis());
    }

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            ChronicleSavedData.get(player.level()).sync2Client(PacketDistributor.PLAYER.with(() -> player));
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            ChronicleSavedData.get(player.level()).sync2Client(PacketDistributor.PLAYER.with(() -> player));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void pauseOrRestart(InputEvent.InteractionKeyMappingTriggered event) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get()) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = Objects.requireNonNull(mc.player);
        ClientLevel level = Objects.requireNonNull(mc.level);
        if (player.getMainHandItem().is(HyperItems.CHRONICLE.get())) {

            BlockPos cursor = getCursorPos();
            ChronicleSavedData data = ChronicleSavedData.get(level);
            if (event.isUseItem()) {
                if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof ItemFrame)
                    return;

                event.setCanceled(true);
                event.setSwingHand(true);

                if (selected == null) {
                    selected = cursor;
                    level.playSound(player, selected, HyperSounds.CHRONICLE_SELECT.get(), SoundSource.PLAYERS, 1, 2);
                } else if (data.check(player.getUUID(), selected, cursor, error -> player.displayClientMessage(error, false))) {
                    HyperConnection.INSTANCE.sendToServer(new ServerboundChroniclePause(selected, cursor));
                    selected = null;
                }
            } else if (event.isAttack()) {
                if (selected != null) {
                    event.setCanceled(true);
                    selected = null;
                    lastRestart = Util.getMillis();
                    level.playSound(player, cursor, HyperSounds.CHRONICLE_RESTART.get(), SoundSource.PLAYERS, 1, 1);
                } else if (Util.getMillis() - lastRestart > 250) {
                    Vec3 eye = player.getEyePosition();
                    double reach = Math.max(Objects.requireNonNull(mc.gameMode).getPickRange(), player.getEntityReach());
                    Vec3 view = eye.add(player.getViewVector(1).scale(reach));

                    BlockPos target = BlockGetter.traverseBlocks(eye, view, Unit.INSTANCE, (unit, current) -> {
                        List<ChronicleSavedData.Entry> paused = data.getPaused(current);
                        if (paused == null || paused.isEmpty()) {
                            BlockState state = level.getBlockState(current);
                            VoxelShape shape = state.getShape(level, current, CollisionContext.of(player));
                            BlockHitResult result = level.clipWithInteractionOverride(eye, view, current, shape, state);
                            if (result != null && result.getType() != HitResult.Type.MISS) return Boxes.INVALID;
                            return null;
                        }

                        for (ChronicleSavedData.Entry entry : paused)
                            if (entry.uuid.equals(player.getUUID()))
                                return current;

                        return null;
                    }, unit -> Boxes.INVALID);

                    if (target != Boxes.INVALID) {
                        event.setCanceled(true);
                        HyperConnection.INSTANCE.sendToServer(new ServerboundChronicleRestart(target));
                        lastRestart = Util.getMillis();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void highlight(RenderHighlightEvent.Block event) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get())
            return;
        if (selected != null)
            event.setCanceled(true);
        else if (HyperCommonConfig.CHRONICLE_SHOW_PROTECTION.get()) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = Objects.requireNonNull(mc.player);
            ClientLevel level = Objects.requireNonNull(mc.level);
            BlockHitResult result = event.getTarget();
            BlockPos pos = result.getBlockPos();

            if (isPaused(level, pos, player)) {
                event.setCanceled(true);
                BlockState state = level.getBlockState(pos);
                PoseStack poseStack = event.getPoseStack();
                Vec3 camera = event.getCamera().getPosition();
                VertexConsumer buffer = event.getMultiBufferSource().getBuffer(RenderType.lines());
                VoxelShape shape = state.getShape(level, pos, CollisionContext.of(player));
                float r, g, b;
                if (ParadoxHandler.againstChronicle(player)) {
                    r = 1;
                    g = 1;
                    b = 0;
                } else {
                    r = 0;
                    g = 0.625f;
                    b = 0.5f;
                }
                LevelRenderer.renderShape(poseStack, buffer, shape, pos.getX() - camera.x(), pos.getY() - camera.y(), pos.getZ() - camera.z(), r, g, b, 1);
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void render(RenderLevelStageEvent event) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get()) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = Objects.requireNonNull(mc.player);
        ClientLevel level = Objects.requireNonNull(mc.level);
        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        long millis = System.currentTimeMillis();
        double max = 300;

        if (millis - lastHanded <= max) {
            ChronicleSavedData data = ChronicleSavedData.get(level);
            double unhandedDelta = Math.min(1d, (millis - lastHanded) / max);
            data.getEntries().stream()
                    .sorted(Comparator.comparingInt(entry -> entry.uuid.equals(player.getUUID()) ? -1 : 1))
                    .filter(entry -> entry.visible(camera, mc.gameRenderer.getRenderDistance()))
                    .forEach(entry -> Renders.with(poseStack, () -> {
                        AABB identity = Boxes.identity(entry.aabb);
                        boolean mine = entry.uuid.equals(player.getUUID());
                        double delta;
                        int alpha;
                        float thickness;
                        if (lastUnHanded > lastHanded) {
                            delta = Calculates.curve(unhandedDelta, 1, 0.25, 0);
                            alpha = Mth.floor(0xBB * delta);
                            thickness = (float) Mth.lerp(delta, 0, 0.075);
                        } else {
                            double time = Math.max(lastUnHanded, entry.time);
                            delta = Mth.clamp((millis - time) / 200d, -1d, 1d);
                            alpha = Mth.floor(0xBB * delta);
                            thickness = (float) Calculates.curve(delta, 0.03, 0.1, 0.075);
                        }

                        poseStack.translate(entry.aabb.minX - camera.x(), entry.aabb.minY - camera.y(), entry.aabb.minZ - camera.z());
                        Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity, (alpha << 24) | 0x00FF88, Predicates.alwaysTrue());
                        Renders.thickLineBoxBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity.inflate(0.05 * delta), thickness, mine ? 0xFFFFCC00 : 0xFFCC0044);
                        Renders.endBatch(Renders.Type.HIGHLIGHT);
                    }));
        }

        if (player.getMainHandItem().is(HyperItems.CHRONICLE.get()) || player.getOffhandItem().is(HyperItems.CHRONICLE.get())) {
            lastHanded = millis;

            BlockPos cursor = getCursorPos();
            if (selected == null) {
                if (!mc.options.hideGui && player.getMainHandItem().is(HyperItems.CHRONICLE.get())) {
                    Renders.with(poseStack, () -> {
                        poseStack.translate(cursor.getX() - camera.x(), cursor.getY() - camera.y(), cursor.getZ() - camera.z());
                        Renders.cubeScaled(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), 1, 0x5500AAAA, face -> !isPaused(level, cursor, null) || isPaused(level, cursor.relative(face), null));
                        Renders.endBatch(Renders.Type.HIGHLIGHT);
                    });
                }
            } else {
                AABB aabb = Boxes.of(selected, cursor);
                AABB identity = Boxes.identity(aabb);
                Renders.with(poseStack, () -> {
                    poseStack.translate(aabb.minX - camera.x(), aabb.minY - camera.y(), aabb.minZ - camera.z());
                    Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity, 0x880088BB, Predicates.alwaysTrue());
                    Renders.thickLineBoxBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity.inflate(0.015625), 0.03f, 0xFF00CC8B);
                    Renders.endBatch(Renders.Type.HIGHLIGHT);
                });
            }
        } else {
            selected = null;
            lastUnHanded = millis;
        }

        if (!hits.isEmpty()) {
            hits.long2LongEntrySet().removeIf(entry -> {
                if (!HyperCommonConfig.CHRONICLE_SHOW_PROTECTION.get())
                    return true;
                float delta = (Util.getMillis() - entry.getLongValue()) / 500f;
                if (delta > 1)
                    return true;
                BlockPos pos = BlockPos.of(entry.getLongKey());
                if (pos.distToCenterSqr(player.position()) > Mth.square(mc.gameRenderer.getRenderDistance() / 2))
                    return true;
                int alpha = Mth.lerpInt(delta, 0x45, 0);
                Renders.with(poseStack, () -> {
                    poseStack.translate(pos.getX() - camera.x(), pos.getY() - camera.y(), pos.getZ() - camera.z());
                    Renders.cube(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), 0, 0, 0, 1, 1, 1, alpha << 24 | 0x00FF8B, Predicates.alwaysTrue());
                });
                Renders.endBatch(Renders.Type.HIGHLIGHT);
                return false;
            });
        }
    }

    @SubscribeEvent
    public static void pause(EntityJoinLevelEvent event) {
        if ((event.getEntity() instanceof PrimedTnt || event.getEntity() instanceof FallingBlockEntity) && isPaused(event.getLevel(), event.getEntity().blockPosition(), null))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void pause(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (isPaused(level, pos, event.getEntity())) {
            event.setCanceled(true);
            if (level.isClientSide())
                hitEffect(pos);
        }
    }

    @SubscribeEvent
    public static void pause(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (HyperCommonConfig.CHRONICLE_INTERACT.get() && isPaused(level, pos, event.getEntity())) {
            event.setCanceled(true);
            event.getEntity().swing(event.getHand());
            if (level.isClientSide())
                hitEffect(pos);
        }
    }

    @SubscribeEvent
    public static void pause(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level && isPaused(level, event.getPos(), event.getPlayer()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void pause(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level) {
            BlockPos pos = event.getPos();
            if (isPaused(level, pos, event.getEntity())) {
                BlockSnapshot snapshot = event.getBlockSnapshot();
                if (!snapshot.getReplacedBlock().is(snapshot.getCurrentBlock().getBlock())) {
                    event.setCanceled(true);
                    if (!level.isClientSide())
                        HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), new ClientboundChronicleHitEffect(pos));
                }
            }
        }
    }

    @SubscribeEvent
    public static void pause(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getLevel() instanceof Level level) {
            BlockPos pos = event.getPos();
            if (isPaused(level, pos, null)) {
                event.setCanceled(true);
                if (!level.isClientSide())
                    HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), new ClientboundChronicleHitEffect(pos));
            }
        }
    }

    @SubscribeEvent
    public static void pause(PistonEvent.Pre event) {
        if (event.getLevel() instanceof Level level) {
            if (isPaused(level, event.getPos(), null))
                event.setCanceled(true);
            else if (!event.getState().isAir() && isPaused(level, event.getFaceOffsetPos(), null))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void pause(LivingDestroyBlockEvent event) {
        LivingEntity entity = event.getEntity();
        BlockPos pos = event.getPos();
        Level level = entity.level();
        if (isPaused(level, pos, entity)) {
            event.setCanceled(true);
            if (!level.isClientSide())
                HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), new ClientboundChronicleHitEffect(pos));
        }
    }

    @SubscribeEvent
    public static void pause(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        if (level.isClientSide())
            for (BlockPos pos : event.getAffectedBlocks())
                if (isPaused(level, pos, null))
                    hitEffect(pos);
    }

    public static boolean isPaused(Level level, BlockPos pos, @Nullable Entity entity, boolean force) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get())
            return false;
        if (chunkGenerating)
            return false;
        if (level.isClientSide() && clientForceNonPaused)
            return false;

        if (force || HyperCommonConfig.CHRONICLE_PARADOX.get() || ParadoxHandler.isNotParadox(entity)) {
            List<ChronicleSavedData.Entry> paused = ChronicleSavedData.get(level).getPaused(pos);
            if (paused == null || paused.isEmpty())
                return false;
            if (HyperCommonConfig.CHRONICLE_OWNER.get())
                return true;
            if (!(entity instanceof Player))
                return true;

            for (ChronicleSavedData.Entry entry : paused)
                if (!entry.uuid.equals(entity.getUUID()))
                    return true;
        }
        return false;
    }

    public static boolean isPaused(Level level, BlockPos pos, @Nullable Entity entity) {
        return isPaused(level, pos, entity, false);
    }

    @OnlyIn(Dist.CLIENT)
    private static BlockPos getCursorPos() {
        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        LocalPlayer player = Objects.requireNonNull(mc.player);
        ClientLevel level = Objects.requireNonNull(mc.level);

        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            Vec3 view = player.getViewVector(1).multiply(4, 4, 4);
            return Boxes.clamp(level, BlockPos.containing(player.getEyePosition().add(view)));
        } else if (hit instanceof BlockHitResult result) {
            BlockPos pos = result.getBlockPos().immutable();
            if (player.isShiftKeyDown() != HyperCommonConfig.CHRONICLE_INVERT_SHIFT.get())
                return Boxes.clamp(level, pos.relative(result.getDirection()));
            else return pos;
        } else return BlockPos.containing(hit.getLocation());
    }

    public static void playSound(ServerLevel level, Vec3 position, boolean pause) {
        if (pause)
            level.playSound(null, position.x(), position.y(), position.z(), HyperSounds.CHRONICLE_PAUSE.get(), SoundSource.PLAYERS, 1, 1);
        else
            level.playSound(null, position.x(), position.y(), position.z(), HyperSounds.CHRONICLE_RESTART.get(), SoundSource.PLAYERS, 1, 1);
    }
}
