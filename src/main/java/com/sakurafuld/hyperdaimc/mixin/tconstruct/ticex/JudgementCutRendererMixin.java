package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.Calculates;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityTicEx;
import mods.flammpfeil.slashblade.client.renderer.entity.JudgementCutRenderer;
import mods.flammpfeil.slashblade.entity.EntityJudgementCut;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(JudgementCutRenderer.class)
@OnlyIn(Dist.CLIENT)
public abstract class JudgementCutRendererMixin<T extends EntityJudgementCut> {
    @Inject(method = "render(Lmods/flammpfeil/slashblade/entity/EntityJudgementCut;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"), remap = false)
    private void renderTicEx(T entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, CallbackInfo ci) {
        if (!NovelHandler.ModifierRenderingLevel.UNIQUE.check())
            return;
        if (((IEntityTicEx) entity).hyperdaimc$isTicExNovel()) {
            float size = (float) Calculates.curve((entity.tickCount + partialTicks) / (double) (entity.getLifetime() + 1 + partialTicks), 0.2, 2, 0);
            int r = FastColor.ARGB32.red(entity.getColor());
            int g = FastColor.ARGB32.green(entity.getColor());
            int b = FastColor.ARGB32.blue(entity.getColor());
            r = 0xFF - r;
            g = 0xFF - g;
            b = 0xFF - b;
            int color = FastColor.ARGB32.color(255, r, g, b);
            Renders.with(matrixStackIn, () -> {
                matrixStackIn.scale(size, size, size);
                this.render(matrixStackIn, bufferIn, entity, 110, color, partialTicks);
                this.render(matrixStackIn, bufferIn, entity, 70, color, partialTicks);
            });
        }
    }

    @Unique
    private void render(PoseStack poseStack, MultiBufferSource buffer, EntityJudgementCut entity, float angle, int color, float partialTicks) {
        float hRot = entity.getId() % 4 * 45;
        float vRot = ((entity.tickCount + partialTicks) * 10f) * (entity.getId() % 2 == 0 ? -1 : 1);

        Renders.with(poseStack, () -> {
            poseStack.mulPose(Axis.YP.rotationDegrees(hRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(angle));
            poseStack.mulPose(Axis.ZP.rotationDegrees(vRot));

            Renders.hollowTriangle(poseStack.last().pose(), buffer.getBuffer(Renders.Type.LIGHTNING_NO_CULL), 3, 0.5f, color);
        });
    }
}
