package com.sakurafuld.hyperdaimc.content.hyper.fumetsu;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

@OnlyIn(Dist.CLIENT)
public class FumetsuEntityRenderer extends MobRenderer<FumetsuEntity, FumetsuEntityModel> {
    public static final ResourceLocation TEXTURE = identifier("textures/entity/fumetsu.png");

    public FumetsuEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new FumetsuEntityModel(FumetsuEntityModel.createLayer().bakeRoot()), 1);
    }

    @Override
    public void render(FumetsuEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.isGenocide()) {
            this.renderAura(pMatrixStack, pBuffer, pEntity, pPartialTicks);
        }

        pMatrixStack.pushPose();

        float yBodyRot = Mth.rotLerp(pPartialTicks, pEntity.yBodyRotO, pEntity.yBodyRot);
        float yHeadRot = Mth.rotLerp(pPartialTicks, pEntity.yHeadRotO, pEntity.yHeadRot) - yBodyRot;

        float xRot = Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot());
        if (isEntityUpsideDown(pEntity)) {
            xRot *= -1;
            yHeadRot *= -1;
        }

        float ticks = this.getBob(pEntity, pPartialTicks);
        this.setupRotations(pEntity, pMatrixStack, ticks, yBodyRot, pPartialTicks);
        pMatrixStack.scale(-1, -1, 1);
        this.scale(pEntity, pMatrixStack, pPartialTicks);
        pMatrixStack.translate(0, -1.501F, 0);
        float limbSwing = 0;
        float limbSwingAmount = 0;
        if (!NovelHandler.novelized(pEntity)) {
            limbSwingAmount = Mth.lerp(pPartialTicks, pEntity.animationSpeedOld, pEntity.animationSpeed);
            limbSwing = pEntity.animationPosition - pEntity.animationSpeed * (1 - pPartialTicks);

            if (limbSwingAmount > 1) {
                limbSwingAmount = 1;
            }
        }

        this.model.prepareMobModel(pEntity, limbSwing, limbSwingAmount, pPartialTicks);
        this.model.setupAnim(pEntity, limbSwing, limbSwingAmount, ticks, yHeadRot, xRot);
        Minecraft mc = Minecraft.getInstance();
        boolean visible = this.isBodyVisible(pEntity);
        boolean invisibleTeam = !visible && !pEntity.isInvisibleTo(mc.player);
        boolean glowing = mc.shouldEntityAppearGlowing(pEntity);
        RenderType rendertype = this.getRenderType(pEntity, visible, invisibleTeam, glowing);
        if (rendertype != null) {
            VertexConsumer vertexconsumer = pBuffer.getBuffer(rendertype);
            int overlay = getOverlayCoords(pEntity, this.getWhiteOverlayProgress(pEntity, pPartialTicks));
            this.model.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, overlay, 1, 1, 1, invisibleTeam ? 0.15F : 1);
        }

        if (!pEntity.isSpectator()) {
            for (RenderLayer<FumetsuEntity, FumetsuEntityModel> renderlayer : this.layers) {
                renderlayer.render(pMatrixStack, pBuffer, pPackedLight, pEntity, limbSwing, limbSwingAmount, pPartialTicks, ticks, yHeadRot, xRot);
            }
        }

        pMatrixStack.popPose();
        RenderNameplateEvent nameplateEvent = new RenderNameplateEvent(pEntity, pEntity.getDisplayName(), this, pMatrixStack, pBuffer, pPackedLight, pPartialTicks);
        MinecraftForge.EVENT_BUS.post(nameplateEvent);
        if (nameplateEvent.getResult() != Event.Result.DENY && (nameplateEvent.getResult() == Event.Result.ALLOW || this.shouldShowName(pEntity))) {
            this.renderNameTag(pEntity, nameplateEvent.getContent(), pMatrixStack, pBuffer, pPackedLight);
        }
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(pEntity, this, pPartialTicks, pMatrixStack, pBuffer, pPackedLight));
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
    protected void setupRotations(FumetsuEntity pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
        if (this.isShaking(pEntityLiving)) {
            pRotationYaw += (float) (Math.cos((double) pEntityLiving.tickCount * 3.25D) * Math.PI * (double) 0.4F);
        }

        Pose pose = pEntityLiving.getPose();
        if (pose != Pose.SLEEPING) {
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180 - pRotationYaw));
        }

        if (pEntityLiving.deathTime > 0 && NovelHandler.novelized(pEntityLiving)) {
            float died = ((float) pEntityLiving.deathTime + pPartialTicks - 1) / 20 * 1.6F;
            died = Mth.sqrt(died);
            if (died > 1) {
                died = 1;
            }

            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(died * this.getFlipDegrees(pEntityLiving)));
        } else if (isEntityUpsideDown(pEntityLiving)) {
            pMatrixStack.translate(0, pEntityLiving.getBbHeight() + 0.1F, 0);
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
        }
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
