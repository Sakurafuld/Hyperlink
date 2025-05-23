package com.sakurafuld.hyperdaimc.content.fumetsu.storm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.helper.Boxes;
import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ForgeModelBakery;
import org.jetbrains.annotations.NotNull;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

@OnlyIn(Dist.CLIENT)
public class FumetsuStormRenderer extends EntityRenderer<FumetsuStorm> {

    public FumetsuStormRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public void render(FumetsuStorm pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        AABB aabb = Boxes.identity(Boxes.lerp(pPartialTick, pEntity.oldAABB, pEntity.getBoundingBox()));

        pPoseStack.pushPose();
        pPoseStack.translate(-aabb.getXsize() / 2, -aabb.getYsize() / 2 + 0.25, -aabb.getZsize() / 2);
        if(pEntity.getId() % 2 == 0) {
            Renders.cubeBox(pPoseStack.last().pose(), pBuffer.getBuffer(Renders.Type.LIGHTNING_NO_CULL), aabb, 0x40FF0000, face -> true);
            LevelRenderer.renderLineBox(pPoseStack, pBuffer.getBuffer(RenderType.lines()), aabb, 0, 1, 1, 1);
        } else {
            Renders.cubeBox(pPoseStack.last().pose(), pBuffer.getBuffer(Renders.Type.LIGHTNING_NO_CULL), aabb, 0x4000FFFF, face -> true);
            LevelRenderer.renderLineBox(pPoseStack, pBuffer.getBuffer(RenderType.lines()), aabb, 1, 0, 0, 1);

        }
        pPoseStack.popPose();

        super.render(pEntity, pEntityYaw, pPartialTick, pPoseStack, pBuffer, pPackedLight);
    }
    @Override
    public @NotNull ResourceLocation getTextureLocation(FumetsuStorm pEntity) {
        return ForgeModelBakery.MISSING_MODEL_LOCATION;
    }
}
