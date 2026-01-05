package com.sakurafuld.hyperdaimc.content.hyper.chronicle.system;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleSavedData;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.system.ParadoxHandler;
import com.sakurafuld.hyperdaimc.infrastructure.Boxes;
import com.sakurafuld.hyperdaimc.infrastructure.Calculates;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.Objects;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = HYPERDAIMC, value = Dist.CLIENT)
public class ChronicleRenderer {
    private static long lastHanded = -1;
    private static long lastUnHanded = -1;
    public static Long2LongOpenHashMap hits = new Long2LongOpenHashMap();

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get())
            return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
            return;

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

            BlockPos cursor = ChronicleHandler.getCursorPos();
            if (ChronicleHandler.selected == null) {
                if (!mc.options.hideGui && player.getMainHandItem().is(HyperItems.CHRONICLE.get())) {
                    Renders.with(poseStack, () -> {
                        poseStack.translate(cursor.getX() - camera.x(), cursor.getY() - camera.y(), cursor.getZ() - camera.z());
                        Renders.cubeScaled(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), 1, 0x5500AAAA, face -> !ChronicleHandler.isPaused(level, cursor, null) || ChronicleHandler.isPaused(level, cursor.relative(face), null));
                        Renders.endBatch(Renders.Type.HIGHLIGHT);
                    });
                }
            } else {
                AABB aabb = Boxes.of(ChronicleHandler.selected, cursor);
                AABB identity = Boxes.identity(aabb);
                Renders.with(poseStack, () -> {
                    poseStack.translate(aabb.minX - camera.x(), aabb.minY - camera.y(), aabb.minZ - camera.z());
                    Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity, 0x880088BB, Predicates.alwaysTrue());
                    Renders.thickLineBoxBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity.inflate(0.015625), 0.03f, 0xFF00CC8B);
                    Renders.endBatch(Renders.Type.HIGHLIGHT);
                });
            }
        } else {
            ChronicleHandler.selected = null;
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

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void highlight(RenderHighlightEvent.Block event) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get())
            return;
        if (ChronicleHandler.selected != null)
            event.setCanceled(true);
        else if (HyperCommonConfig.CHRONICLE_SHOW_PROTECTION.get()) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = Objects.requireNonNull(mc.player);
            ClientLevel level = Objects.requireNonNull(mc.level);
            BlockHitResult result = event.getTarget();
            BlockPos pos = result.getBlockPos();

            if (ChronicleHandler.isPaused(level, pos, player)) {
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
}
