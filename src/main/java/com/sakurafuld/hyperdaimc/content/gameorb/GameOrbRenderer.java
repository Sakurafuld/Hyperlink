package com.sakurafuld.hyperdaimc.content.gameorb;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.Random;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class GameOrbRenderer extends BlockEntityWithoutLevelRenderer {
    private static final Random RANDOM = new Random();
    public static final Supplier<BakedModel> ORB = Renders.importSpecialModel("game_orb");
    public static final Supplier<BakedModel> HALO = Renders.importSpecialModel("game_over_halo");

    public GameOrbRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        VertexConsumer buffer = ItemRenderer.getFoilBuffer(pBuffer, Sheets.translucentCullBlockSheet(), true, pStack.hasFoil());

        Renders.with(pPoseStack, () ->
                renderOrb(pPoseStack, buffer, pDisplayContext, pPackedLight, pPackedOverlay));

        switch (pDisplayContext) {
            case FIXED, GUI, FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> Renders.with(pPoseStack, () ->
                    renderHalo(pPoseStack, buffer, pDisplayContext, pPackedLight, pPackedOverlay));
        }
    }

    public static void renderOrb(PoseStack poseStack, VertexConsumer consumer, ItemDisplayContext context, int light, int overlay) {
        poseStack.translate(0.5, 0.5, 0.5);

        double time = Util.getMillis() % 2000;
        if (time <= 100 || (500 < time && time <= 550)) {
            poseStack.scale(RANDOM.nextFloat(0.5f, 1.5f), RANDOM.nextFloat(0.5f, 1.5f), RANDOM.nextFloat(0.5f, 1.5f));
        }

        BakedModel model = ForgeHooksClient.handleCameraTransforms(poseStack, ORB.get(), context, context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        poseStack.translate(-0.5, -0.5, -0.5);

        double blur = Util.getMillis() % 1500 / 1500d;
        double cos = Math.cos(Util.getMillis() / 100d);
        double dokidoki = (Math.random() - 0.5) / 7;
        dokidoki *= blur;
        dokidoki *= cos;
        poseStack.translate(dokidoki, dokidoki, dokidoki);

        Renders.model(model, poseStack, consumer, light, overlay);
    }

    public static void renderHalo(PoseStack poseStack, VertexConsumer consumer, ItemDisplayContext context, int light, int overlay) {

        double delta = ((1 + Math.cos(Util.getMillis() / 800d)) / 2);
        switch (context) {
            case GUI -> poseStack.translate(0, 0, Mth.lerp(delta, 0.25, -1));
            case FIRST_PERSON_RIGHT_HAND -> {
                poseStack.translate(0.1, 0, -0.15);

                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.scale(0.5f, 0.5f, 0.5f);
                poseStack.mulPose(Axis.YN.rotationDegrees(15));
                poseStack.mulPose(Axis.XN.rotationDegrees(20));
                poseStack.translate(-0.5, -0.5, -0.5);
            }
            case FIRST_PERSON_LEFT_HAND -> {
                poseStack.translate(-0.1, 0, -0.15);

                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.scale(0.5f, 0.5f, 0.5f);
                poseStack.mulPose(Axis.YP.rotationDegrees(15));
                poseStack.mulPose(Axis.XN.rotationDegrees(20));
                poseStack.translate(-0.5, -0.5, -0.5);
            }
        }

        float size = (float) Mth.lerp(delta, 1.25f, 2);

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(size, size, size);
        poseStack.translate(-0.5, -0.5, -0.5);

        Renders.model(HALO.get(), poseStack, consumer, light, overlay, quad -> 0xFF000000);
    }
}
