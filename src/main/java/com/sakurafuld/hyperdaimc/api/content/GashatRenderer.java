package com.sakurafuld.hyperdaimc.api.content;

import com.google.common.collect.Sets;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class GashatRenderer extends BlockEntityWithoutLevelRenderer {
    public static final Random RANDOM = new Random();
    private static final Set<Particle> PARTICLES = Sets.newHashSet();
    private final Supplier<ResourceLocation> model;
    private final long delay = Math.round(Mth.lerp(Math.random(), 0, 10000));

    public GashatRenderer(Supplier<ResourceLocation> model) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.model = model;
    }

    @Override
    public void renderByItem(ItemStack pStack, ItemDisplayContext pDisplayContext, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        VertexConsumer buffer = ItemRenderer.getFoilBuffer(pBuffer, Sheets.translucentCullBlockSheet(), true, pStack.hasFoil());
        Renders.with(pPoseStack, () ->
                renderModel(pPoseStack, buffer, pDisplayContext, pPackedLight, pPackedOverlay));

        for (int count = 0; count < 3; count++) {
            if (RANDOM.nextInt(400) == 0) {
                PARTICLES.add(new Particle(pStack, () -> Minecraft.getInstance().getModelManager().getModel(this.model.get())));
            }
        }

        PARTICLES.removeIf(particle -> particle.render(pStack, pDisplayContext, pPoseStack, pBuffer, pPackedLight, pPackedOverlay));
    }

    public void renderModel(PoseStack poseStack, VertexConsumer consumer, ItemDisplayContext context, int light, int overlay) {
        poseStack.translate(0.5, 0.5, 0.5);

        long delayed = (this.delay + Util.getMillis());
        double time = delayed % 20000;
        int color;
        if (time <= 200 || (6000 < time && time <= 6200) || (10000 < time && (time <= 10300)) || (10400 < time && (time <= 10450))) {
            poseStack.scale(RANDOM.nextFloat(0.5f, 1.75f), RANDOM.nextFloat(0.5f, 1.75f), RANDOM.nextFloat(0.5f, 1.75f));
            if (10000 < time) {
                color = (0xFF000000) | RANDOM.nextInt(0xFFFFFF);
            } else {
                color = 0xFFFFFFFF;
            }
        } else {
            color = 0xFFFFFFFF;
        }

        float cos = Mth.cos(delayed / 800f);

        poseStack.mulPose(Axis.ZP.rotationDegrees(cos * 8));
        poseStack.mulPose(Axis.XP.rotationDegrees(cos * 2));
        poseStack.mulPose(Axis.YP.rotationDegrees(cos * 2));

        BakedModel model = ForgeHooksClient.handleCameraTransforms(poseStack, Minecraft.getInstance().getModelManager().getModel(this.model.get()), context, context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        poseStack.translate(-0.5, -0.5, -0.5);

        Renders.model(model, poseStack, consumer, light, overlay, quad -> color);
    }

    public static class Particle {
        private final ItemStack stack;
        private final Supplier<BakedModel> model;
        private final long made;
        private final float age;
        private final long delay;
        private final int color;
        private final double x;
        private final double y;
        private final float xRot;
        private final float yRot;


        public Particle(ItemStack stack, Supplier<BakedModel> model) {
            this.stack = stack;
            this.model = model;
            this.made = Util.getMillis();
            this.age = RANDOM.nextInt(500, 1000);
            this.delay = Math.round(Mth.lerp(Math.random(), 0, 10000));

            this.color = RANDOM.nextInt(0xFFFFFF);

            double angle = Math.toRadians(RANDOM.nextInt(360));
            this.x = Math.cos(angle) / 3;
            this.y = Math.sin(angle) / 3;
            this.xRot = RANDOM.nextFloat(-22.5f, 22.5f);
            this.yRot = RANDOM.nextFloat(-22.5f, 22.5f);
        }

        public boolean render(ItemStack stack, ItemDisplayContext context, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
            if (Util.getMillis() - this.made >= this.age) {
                return true;
            } else if (this.stack == stack) {
                poseStack.pushPose();

                poseStack.translate(0.5, 0.5, 0.5);

                ForgeHooksClient.handleCameraTransforms(poseStack, this.model.get(), context, context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND);

                poseStack.translate(this.x, this.y, 0);

                float cos = Mth.cos((this.delay + Util.getMillis()) / 200f);
                poseStack.mulPose(Axis.ZP.rotationDegrees(cos * 20));

                poseStack.mulPose(Axis.XP.rotationDegrees(this.xRot));
                poseStack.mulPose(Axis.YP.rotationDegrees(this.yRot));

                float t = (this.age - (Util.getMillis() - this.made)) / (this.age - 100);
                poseStack.scale(t, t, 0);

                Renders.hollowTriangle(poseStack.last().pose(), Renders.getBuffer(Renders.Type.LIGHTNING_NO_CULL), 0.3f, 0.1f, (0x7F << 24) | this.color);

                poseStack.popPose();
            }
            return false;
        }
    }
}
