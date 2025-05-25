package com.sakurafuld.hyperdaimc.content.fumetsu.skull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FumetsuSkullRenderer extends EntityRenderer<FumetsuSkull> {
    private final FumetsuSkullModel model;

    public FumetsuSkullRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.model = new FumetsuSkullModel(FumetsuSkullModel.createLayer().bakeRoot());
    }

    @Override
    public void render(FumetsuSkull pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        Renders.with(pPoseStack, () -> {
            pPoseStack.scale(-1, -1, 1);
            pPoseStack.scale(this.getSize(), this.getSize(), this.getSize());
            float yaw = Mth.rotLerp(pEntity.yRotO, pEntity.getYRot(), pPartialTick);
            float pitch = Mth.lerp(pPartialTick, pEntity.xRotO, pEntity.getXRot());
            VertexConsumer vertexconsumer = pBuffer.getBuffer(this.model.renderType(this.getTextureLocation(pEntity)));
            this.model.setup(yaw, pitch);
            this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        });

        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    protected int getBlockLightLevel(FumetsuSkull pEntity, BlockPos pPos) {
        return 15;
    }

    @Override
    public ResourceLocation getTextureLocation(FumetsuSkull pEntity) {
        return pEntity.getSkullType().getTexture();
    }

    protected float getSize() {
        return 1;
    }
}
