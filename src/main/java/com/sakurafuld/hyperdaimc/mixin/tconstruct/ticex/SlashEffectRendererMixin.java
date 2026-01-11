package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.content.hyper.novel.system.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.Calculates;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntitySlashEffectTicEx;
import mods.flammpfeil.slashblade.client.renderer.entity.SlashEffectRenderer;
import mods.flammpfeil.slashblade.entity.EntitySlashEffect;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(SlashEffectRenderer.class)
@OnlyIn(Dist.CLIENT)
public abstract class SlashEffectRendererMixin<T extends EntitySlashEffect> {
    @Inject(method = "render(Lmods/flammpfeil/slashblade/entity/EntitySlashEffect;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V", ordinal = 0, remap = true), remap = false)
    private void renderTicEx(T entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, CallbackInfo ci) {
        if (!NovelHandler.RenderingLevel.UNIQUE.check())
            return;
        IEntitySlashEffectTicEx entityTicEx = (IEntitySlashEffectTicEx) entity;
        if (entityTicEx.hyperdaimc$isTicExNovel() && entityTicEx.hyperdaimc$isSpecial()) {
            float size = (float) Calculates.curve((entity.tickCount + partialTicks) / (double) (entity.getLifetime() + 1 + partialTicks), 0.2, 2, 0);
            int r = FastColor.ARGB32.red(entity.getColor());
            int g = FastColor.ARGB32.green(entity.getColor());
            int b = FastColor.ARGB32.blue(entity.getColor());
            r = 0xFF - r;
            g = 0xFF - g;
            b = 0xFF - b;
            int color = FastColor.ARGB32.color(255, r, g, b);
            float yRot = entity.getId() % 8 * 45;
            float zRot = ((entity.tickCount + partialTicks) * 10f) * (entity.getId() % 2 == 0 ? -1 : 1);
            Renders.with(matrixStackIn, () -> {
                matrixStackIn.scale(size, size, size);
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(yRot));
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(70));
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(zRot));

                Renders.hollowTriangle(matrixStackIn.last().pose(), bufferIn.getBuffer(Renders.Type.LIGHTNING_NO_CULL), 3, 0.5f, color);
            });
        }
    }
}
