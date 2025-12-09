package com.sakurafuld.hyperdaimc.content.hyper.paradox.handler;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxCapabilityItem;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxCapabilityPlayer;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxChain;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxChainCluster;
import com.sakurafuld.hyperdaimc.infrastructure.Boxes;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Deque;
import java.util.Objects;
import java.util.Set;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = HYPERDAIMC, value = Dist.CLIENT)
public class ParadoxRenderer {
    private static long lastNoHeld = 0;
    private static long lastHeld = 0;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void cancelRender(RenderHighlightEvent.Block event) {
        if (!HyperCommonConfig.ENABLE_PARADOX.get())
            return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = Objects.requireNonNull(mc.player);
        if (!player.getMainHandItem().is(HyperItems.PARADOX.get()) && !player.getOffhandItem().is(HyperItems.PARADOX.get()))
            return;
        player.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradox -> {
            if (paradox.hasSelected())
                event.setCanceled(true);
        });
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void render(RenderLevelStageEvent event) {
        if (!HyperCommonConfig.ENABLE_PARADOX.get())
            return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
            return;

        Minecraft mc = Objects.requireNonNull(Minecraft.getInstance());
        LocalPlayer player = Objects.requireNonNull(mc.player);
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offhand = player.getOffhandItem();
        boolean mainHanded = mainHand.is(HyperItems.PARADOX.get());
        boolean hasParadox = mainHanded || offhand.is(HyperItems.PARADOX.get());
        if (!hasParadox) lastNoHeld = player.tickCount;
        else lastHeld = player.tickCount;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        LazyOptional<ParadoxCapabilityPlayer> capability = LazyOptional.empty();


        MutableObject<BlockPos> cursor = new MutableObject<>();
        MutableObject<ParadoxChainCluster> cluster = new MutableObject<>();
        if (mainHanded)
            mainHand.getCapability(ParadoxCapabilityItem.TOKEN).ifPresent(paradoxItem ->
                    cluster.setValue(paradoxItem.getCluster()));
        else
            offhand.getCapability(ParadoxCapabilityItem.TOKEN).ifPresent(paradoxItem ->
                    paradoxItem.setCluster(paradoxItem.getCluster()));

        if (cluster.getValue() != null) {
            BlockPos at = null;
            Direction direction = null;
            float partialTick = event.getPartialTick();
            HitResult hit = player.pick(Math.max(Objects.requireNonNull(mc.gameMode).getPickRange(), player.getEntityReach()), partialTick, HyperCommonConfig.PARADOX_HIT_FLUID.get());
            if (hit instanceof BlockHitResult result && result.getType() != HitResult.Type.MISS) {
                at = result.getBlockPos();
                direction = result.getDirection().getOpposite();
            }

            if (at == null) {
                if (cursor.getValue() == null)
                    cursor.setValue(Boxes.getCursor(player, HyperCommonConfig.PARADOX_HIT_FLUID.get(), true));
                at = cursor.getValue();
            }

            if (direction == null)
                direction = Direction.orderedByNearest(player)[0];

            Set<ParadoxChain> chains = cluster.getValue().get(at, direction, player.getDirection());
            if (!chains.isEmpty()) {
                for (ParadoxChain chain : chains) {
                    if (chain.visible(camera, mc.gameRenderer.getRenderDistance())) {
                        AABB identity = Boxes.identity(chain.aabb);
                        Renders.with(poseStack, () -> {
                            poseStack.translate(chain.aabb.minX - camera.x(), chain.aabb.minY - camera.y(), chain.aabb.minZ - camera.z());
                            Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity, 0x45A0A0FF, Predicates.alwaysTrue());
                            Renders.thickLineBoxBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity.inflate(0.05f), 0.05f, 0xFFFF8B8B);
                        });
                        Renders.endBatch(Renders.Type.HIGHLIGHT);
                    }
                }
            }
        }

        if (hasParadox && ParadoxCapabilityPlayer.isCapable(player)) {
            (capability = player.getCapability(ParadoxCapabilityPlayer.TOKEN)).ifPresent(paradox -> {
                BlockPos selected = paradox.getSelected();
                if (selected != null) {
                    if (cursor.getValue() == null)
                        cursor.setValue(Boxes.getCursor(player, HyperCommonConfig.PARADOX_HIT_FLUID.get(), true));
                    AABB aabb = Boxes.of(selected, cursor.getValue());
                    AABB identity = Boxes.identity(aabb);
                    Renders.with(poseStack, () -> {
                        poseStack.translate(aabb.minX - camera.x(), aabb.minY - camera.y(), aabb.minZ - camera.z());
                        Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity, 0x45FF8B8B, Predicates.alwaysTrue());
                        LevelRenderer.renderLineBox(poseStack, Renders.getBuffer(RenderType.lines()), identity, 0.75f, 0.75f, 1, 1);
                    });
                    Renders.endBatch(Renders.Type.HIGHLIGHT);
                }
            });
        }

//        if (player.tickCount % 40 == 0)
//            LOG.debug("paradoFade lastNoHeld={}, lastHeld={}, fade={}, answer={}", lastNoHeld, lastHeld, HyperCommonConfig.PARADOX_FADE.get(),
//                    (lastNoHeld - lastHeld) / (float) HyperCommonConfig.PARADOX_FADE.get());
        float alpha = hasParadox ? 1 : (1f - Mth.clamp((lastNoHeld - lastHeld) / (float) HyperCommonConfig.PARADOX_FADE.get(), 0, 1));
        if (alpha <= 0) return;

        if (!capability.isPresent() && ParadoxCapabilityPlayer.isCapable(player))
            capability = player.getCapability(ParadoxCapabilityPlayer.TOKEN);
        capability.ifPresent(paradox -> {
            Deque<ParadoxChain> chains = paradox.getChains();
            for (ParadoxChain chain : chains) {
                if (chain.visible(camera, mc.gameRenderer.getRenderDistance())) {
                    AABB identity = Boxes.identity(chain.aabb);
                    Renders.with(poseStack, () -> {
                        poseStack.translate(chain.aabb.minX - camera.x(), chain.aabb.minY - camera.y(), chain.aabb.minZ - camera.z());
                        Renders.cubeBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity, (int) (alpha / 2f * 0xFF) << 24 | 0x45008B, Predicates.alwaysTrue());
                        Renders.thickLineBoxBox(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), identity.inflate(0.05f), 0.075f, (int) (alpha * 0xFF) << 24 | 0x8B00A0);
                    });
                    Renders.endBatch(Renders.Type.HIGHLIGHT);
                }
            }
        });
    }
}
