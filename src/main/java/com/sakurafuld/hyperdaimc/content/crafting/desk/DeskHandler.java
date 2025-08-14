package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.sakurafuld.hyperdaimc.api.content.AbstractGashatItem;
import com.sakurafuld.hyperdaimc.api.mixin.MixinLevelTickEvent;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.crafting.gameorb.GameOrbRenderer;
import com.sakurafuld.hyperdaimc.content.crafting.material.MaterialItem;
import com.sakurafuld.hyperdaimc.helper.Renders;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.desk.ClientboundDeskMinecraft;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Random;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class DeskHandler {
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void logIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            DeskSavedData.get(player.getLevel()).sync2Client(player);
        }
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            DeskSavedData.get(player.getLevel()).sync2Client(player);
        }
    }

    @SubscribeEvent
    public static void minecrafting(MixinLevelTickEvent event) {
        DeskSavedData.get(event.getLevel()).getEntries().removeIf(entry -> !entry.tick(event.getLevel()));
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();

        DeskSavedData.get(mc.level).getEntries().forEach(entry -> Renders.with(poseStack, () -> {
            poseStack.translate(-camera.getPosition().x(), -camera.getPosition().y(), -camera.getPosition().z());
            Vec3 center = Vec3.atCenterOf(entry.pos);
            if (entry.shouldRenderOnes()) {
                Renders.with(poseStack, () -> {
                    float size = Mth.lerp(event.getPartialTick(), entry.oldItemSize, entry.itemSize);
                    float zRot = Mth.rotLerp(event.getPartialTick(), entry.oldRot.z(), entry.rot.z());
                    float yRot = Mth.rotLerp(event.getPartialTick(), entry.oldRot.y(), entry.rot.y());
                    float xRot = Mth.rotLerp(event.getPartialTick(), entry.oldRot.x(), entry.rot.x());
                    BakedModel model = mc.getItemRenderer().getModel(entry.result, mc.level, null, 0);

                    poseStack.translate(center.x(), center.y(), center.z());
                    poseStack.scale(size, size, size);

                    Renders.with(poseStack, () -> {
                        poseStack.scale(0.8f, 0.8f, 0.8f);
                        poseStack.mulPose(Vector3f.ZN.rotationDegrees(zRot));
                        poseStack.mulPose(Vector3f.YP.rotationDegrees(yRot));
                        poseStack.mulPose(Vector3f.XN.rotationDegrees(xRot));

                        float transY = model.getTransforms().getTransform(ItemTransforms.TransformType.GROUND).translation.y()
                                + (shouldTweakTranslationAsGenerated(entry.result.getItem()) ? 0.125f : 0);

                        poseStack.translate(0, -transY * size, 0);

                        mc.getItemRenderer().render(entry.result, ItemTransforms.TransformType.GROUND, false, poseStack, Renders.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
                    });

                    Renders.with(poseStack, () -> {
                        float cube = 0.275f;

                        poseStack.mulPose(Vector3f.ZP.rotationDegrees(zRot));
                        poseStack.mulPose(Vector3f.YP.rotationDegrees(yRot));
                        poseStack.mulPose(Vector3f.XP.rotationDegrees(xRot));

                        Renders.cube(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), -cube, -cube, -cube, cube, cube, cube, 0x8BFFFFFF, quad -> true);
                    });

                    Renders.with(poseStack, () -> {
                        float cube = 0.4f;
                        poseStack.mulPose(Vector3f.ZP.rotationDegrees(zRot));
                        poseStack.mulPose(Vector3f.YP.rotationDegrees(yRot));
                        poseStack.mulPose(Vector3f.XP.rotationDegrees(xRot));

                        Renders.cube(poseStack.last().pose(), Renders.getBuffer(Renders.Type.HIGHLIGHT), -cube, -cube, -cube, cube, cube, cube, 0x8BCCCCCC, quad -> true);
                    });

                    Renders.endBatch(Renders.Type.HIGHLIGHT);
                });
                entry.ingredients.forEach(one -> Renders.with(poseStack, () -> {

                    double x = center.x() + Mth.lerp(event.getPartialTick(), one.oldPosition.x(), one.position.x());
                    double y = center.y() + Mth.lerp(event.getPartialTick(), one.oldPosition.y(), one.position.y());
                    double z = center.z() + Mth.lerp(event.getPartialTick(), one.oldPosition.z(), one.position.z());

                    float zRot = Mth.rotLerp(event.getPartialTick(), one.oldRot.z(), one.rot.z());
                    float yRot = Mth.rotLerp(event.getPartialTick(), one.oldRot.y(), one.rot.y());
                    float xRot = Mth.rotLerp(event.getPartialTick(), one.oldRot.x(), one.rot.x());
                    BakedModel model = mc.getItemRenderer().getModel(one.stack, mc.level, null, 0);

                    float transY = model.getTransforms().getTransform(ItemTransforms.TransformType.GROUND).translation.y()
                            + (shouldTweakTranslationAsGenerated(one.stack.getItem()) ? 0.125f : 0);

                    poseStack.translate(x, y, z);
                    if (one.stack.is(HyperItems.GAME_ORB.get())) {
                        Vec3 vec = camera.getPosition().subtract(x, y, z);
                        Renders.with(poseStack, () -> {
                            poseStack.scale(0.75f, 0.75f, 0.75f);
                            poseStack.mulPose(Vector3f.YP.rotationDegrees((float) Math.toDegrees(Mth.atan2(vec.x(), vec.z()))));
                            poseStack.mulPose(Vector3f.XN.rotationDegrees((float) Math.toDegrees(Mth.atan2(vec.y(), vec.horizontalDistance()))));
                            poseStack.translate(-0.5, -0.5, -0.5);
                            poseStack.translate(0, -transY, 0);

                            GameOrbRenderer.renderHalo(poseStack, ItemRenderer.getFoilBuffer(Renders.bufferSource(), Sheets.translucentCullBlockSheet(), true, one.stack.hasFoil()), ItemTransforms.TransformType.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                        });
                    }

                    Renders.with(poseStack, () -> {
                        poseStack.scale(0.85f, 0.85f, 0.85f);
                        poseStack.mulPose(Vector3f.ZP.rotationDegrees(zRot));
                        poseStack.mulPose(Vector3f.YP.rotationDegrees(yRot));
                        poseStack.mulPose(Vector3f.XP.rotationDegrees(xRot));

                        poseStack.translate(0, -transY, 0);

                        mc.getItemRenderer().render(one.stack, ItemTransforms.TransformType.GROUND, false, poseStack, Renders.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);
                    });

                    poseStack.mulPose(Vector3f.ZP.rotationDegrees(zRot));
                    poseStack.mulPose(Vector3f.YP.rotationDegrees(yRot));
                    poseStack.mulPose(Vector3f.XP.rotationDegrees(xRot));
                    if (one.radius > 0) {
                        float triXRot = Util.getMillis() / 1000f * Mth.lerp(((one.color % 32f) / 32f), -180, 180);
                        float triZRot = (one.ticks % 360f) * 6f;

                        Renders.with(poseStack, () -> {
                            poseStack.mulPose(Vector3f.ZP.rotationDegrees(triZRot));
                            poseStack.mulPose(Vector3f.XP.rotationDegrees(triXRot));

                            Renders.hollowTriangle(poseStack.last().pose(), Renders.getBuffer(Renders.Type.LIGHTNING_NO_CULL), one.radius, 0.3f, one.color);
                        });

                        Renders.with(poseStack, () -> {
                            poseStack.mulPose(Vector3f.ZN.rotationDegrees(triZRot));
                            poseStack.mulPose(Vector3f.XP.rotationDegrees(triXRot + 90));

                            Renders.hollowTriangle(poseStack.last().pose(), Renders.getBuffer(Renders.Type.LIGHTNING_NO_CULL), one.radius, 0.3f, one.color);
                        });

                        Renders.endBatch(Renders.Type.LIGHTNING_NO_CULL);
                    }
                }));
            }

            if (entry.fxSize > 0) {
                Renders.with(poseStack, () -> {
                    int[] colors = new int[]{
                            0xFF0000,
                            0x00FF00,
                            0x0000FF,
                            0xFFFF00,
                            0xFF00FF,
                            0x00FFFF,
                            0x000000,
                            0xFFFFFF
                    };


                    float size = Mth.lerp(event.getPartialTick(), entry.oldFXSize, entry.fxSize);

                    RANDOM.setSeed(entry.seed);

                    poseStack.translate(center.x(), center.y(), center.z());
                    poseStack.scale(size, size, size);

                    for (int index = 0, colorsLength = colors.length; index < colorsLength; index++) {
                        int color = colors[index];
                        float plus = RANDOM.nextFloat() * 2;
                        float xRot = (index - (colorsLength / 2f)) * 11.25f + RANDOM.nextFloat(-11.25f, 11.25f);
                        float yRot = RANDOM.nextFloat(360);
                        Renders.with(poseStack, () -> {
                            poseStack.scale(plus, plus, plus);
                            poseStack.mulPose(Vector3f.YP.rotationDegrees(yRot));
                            poseStack.mulPose(Vector3f.XP.rotationDegrees(xRot + 90));

                            Renders.hollowTriangle(poseStack.last().pose(), Renders.getBuffer(Renders.Type.LIGHTNING_NO_CULL), 1, 0.2f, ((int) (entry.fxAlpha * 255) << 24) | color);
                        });
                    }

                    Renders.endBatch(Renders.Type.LIGHTNING_NO_CULL);
                });
            }
        }));
    }

    private static boolean shouldTweakTranslationAsGenerated(Item item) {
        return item instanceof MaterialItem || item instanceof AbstractGashatItem;
    }

    public static void minecraftAt(Level level, BlockPos pos, List<ItemStack> ingredients, ItemStack result) {
        if (!level.isClientSide()) {
            DeskSavedData.Entry entry = DeskSavedData.get(level).add(pos, ingredients, result);
            HyperConnection.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), new ClientboundDeskMinecraft(entry));
        }
    }
}
