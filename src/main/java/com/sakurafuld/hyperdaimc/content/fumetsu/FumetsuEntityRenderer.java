package com.sakurafuld.hyperdaimc.content.fumetsu;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.sakurafuld.hyperdaimc.helper.Deets.*;

@OnlyIn(Dist.CLIENT)
public class FumetsuEntityRenderer extends MobRenderer<FumetsuEntity, FumetsuEntityModel> {
    public static final ResourceLocation TEXTURE = identifier(HYPERDAIMC, "textures/entity/fumetsu.png");

    public FumetsuEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new FumetsuEntityModel(FumetsuEntityModel.createLayer().bakeRoot()), 1);
    }

    @Override
    public void render(FumetsuEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        if(pEntity.isGenocide()) {
            this.renderAura(pMatrixStack, pBuffer, pEntity, pPartialTicks);
        }
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
    private void renderAura(PoseStack poseStack, MultiBufferSource buffer, FumetsuEntity fumetsu, float partialTicks) {
        float xRot = Mth.cos(fumetsu.tickCount / 6f) * 6f;
        float size = Math.min(1f, fumetsu.genocideTime / 15f);

        Renders.with(poseStack, () -> {
            poseStack.translate(0, fumetsu.getBbHeight() / 2, 0);
            poseStack.scale(size, size, size);

            Renders.with(poseStack, () -> {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(120 + xRot));
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.rotLerp(partialTicks, ((fumetsu.tickCount - 1) % 360f) * 3f, (fumetsu.tickCount % 360f) * 3f)));

                Renders.hollowTriangle(poseStack.last().pose(), buffer.getBuffer(Renders.Type.LIGHTNING_NO_CULL), 3, 0.5f, 0x7FFF0101);
            });

            Renders.with(poseStack, () -> {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(70 - xRot));
                poseStack.mulPose(Vector3f.ZN.rotationDegrees(Mth.rotLerp(partialTicks, ((fumetsu.tickCount - 1) % 360f) * 3f, (fumetsu.tickCount % 360f) * 3f)));

                Renders.hollowTriangle(poseStack.last().pose(), buffer.getBuffer(Renders.Type.LIGHTNING_NO_CULL), 3, 0.5f, 0x7F01FFFF);
            });
        });
    }

    @Override
    protected int getBlockLightLevel(FumetsuEntity pEntity, BlockPos pPos) {
        return 15;
    }
    @Override
    public ResourceLocation getTextureLocation(FumetsuEntity pEntity) {
        return TEXTURE;
    }
    @Override
    protected void scale(FumetsuEntity pLivingEntity, PoseStack pMatrixStack, float pPartialTickTime) {
        pMatrixStack.scale(2, 2, 2);
    }
}
