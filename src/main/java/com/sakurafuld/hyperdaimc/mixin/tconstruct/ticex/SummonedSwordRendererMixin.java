package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.content.hyper.novel.system.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.Calculates;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityTicEx;
import mods.flammpfeil.slashblade.client.renderer.entity.SummonedSwordRenderer;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
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
@Mixin(SummonedSwordRenderer.class)
@OnlyIn(Dist.CLIENT)
public abstract class SummonedSwordRendererMixin<T extends EntityAbstractSummonedSword> {
    @Inject(method = "render(Lmods/flammpfeil/slashblade/entity/EntityAbstractSummonedSword;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lmods/flammpfeil/slashblade/client/renderer/model/BladeModelManager;getModel(Lnet/minecraft/resources/ResourceLocation;)Lmods/flammpfeil/slashblade/client/renderer/model/obj/WavefrontObject;", remap = false), remap = false)
    private void renderTicEx(T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLightIn, CallbackInfo ci) {
        if (!NovelHandler.RenderingLevel.UNIQUE.check())
            return;
        if (((IEntityTicEx) entity).hyperdaimc$isTicExNovel()) {
            boolean hit = ((EntityAbstractSummonedSwordAccessor) entity).isInGround() || entity.getHitEntity() != null;
            int start = hit ? 0 : 3;
            if (entity.tickCount < start) return;
            float ticks = Math.max(0, entity.tickCount - start + partialTicks);
            float delta = ticks / 10f;
            double z = hit ? -1.35 : Calculates.curve(Math.min(1, delta), 0, -30, 20, -1.35);
            float size = (float) Calculates.curve(Math.min(1, delta), 0.05, 0.25, 1.5, 1);

            float rot = (entity.getId() % 4 * 45 +
                    (270 * Math.min(1, delta) + (delta < 1 ? 0 : ticks * 12)))
                    * (entity.getId() % 2 == 0 ? -1 : 1);
            int r = FastColor.ARGB32.red(entity.getColor());
            int g = FastColor.ARGB32.green(entity.getColor());
            int b = FastColor.ARGB32.blue(entity.getColor());
            r = 0xFF - r;
            g = 0xFF - g;
            b = 0xFF - b;
            int color = FastColor.ARGB32.color(255, r, g, b);
            Renders.with(matrixStack, () -> {
                matrixStack.scale(32, 32, 32);
                matrixStack.translate(0, 0, z);
                matrixStack.scale(size, size, size);
                matrixStack.mulPose(Axis.ZP.rotationDegrees(rot));
                Renders.hollowTriangle(matrixStack.last().pose(), bufferIn.getBuffer(Renders.Type.LIGHTNING_NO_CULL), 2.5f, 0.75f, color);
            });
        }
    }
}
