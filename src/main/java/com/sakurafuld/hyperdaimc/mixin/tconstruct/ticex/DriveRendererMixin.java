package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.Calculates;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityTicEx;
import mods.flammpfeil.slashblade.client.renderer.entity.DriveRenderer;
import mods.flammpfeil.slashblade.entity.EntityDrive;
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
@Mixin(DriveRenderer.class)
@OnlyIn(Dist.CLIENT)
public abstract class DriveRendererMixin<T extends EntityDrive> {
    @Inject(method = "render(Lmods/flammpfeil/slashblade/entity/EntityDrive;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lmods/flammpfeil/slashblade/client/renderer/model/BladeModelManager;getModel(Lnet/minecraft/resources/ResourceLocation;)Lmods/flammpfeil/slashblade/client/renderer/model/obj/WavefrontObject;", remap = false), remap = false)
    private void renderTicEx(T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLightIn, CallbackInfo ci) {
        if (!NovelHandler.ModifierRenderingLevel.UNIQUE.check())
            return;
        if (((IEntityTicEx) entity).hyperdaimc$isTicExNovel()) {
            float ticks = Math.max(0, entity.tickCount + partialTicks);
            float rot = ticks * 12 * (entity.getId() % 2 == 0 ? -1 : 1);
            float size = (float) Calculates.curve(Math.min(1, ticks / 10f), 0.05, 0.25, 1.5, 1);
            int r = FastColor.ARGB32.red(entity.getColor());
            int g = FastColor.ARGB32.green(entity.getColor());
            int b = FastColor.ARGB32.blue(entity.getColor());
            r = 0xFF - r;
            g = 0xFF - g;
            b = 0xFF - b;
            int color = FastColor.ARGB32.color(255, r, g, b);
            Renders.with(matrixStack, () -> {
                matrixStack.scale(32, 32, 32);
                matrixStack.scale(size, size, size);
                this.render(matrixStack, -0.175, 80, rot, color);
                this.render(matrixStack, 0.15, 100, -rot, color);
            });
        }
    }

    @Unique
    private void render(PoseStack poseStack, double yOffset, float xRot, float zRot, int color) {
        Renders.with(poseStack, () -> {
            poseStack.translate(0, yOffset, 3.25);
            poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
            poseStack.mulPose(Axis.ZP.rotationDegrees(zRot));
            Renders.hollowTriangle(poseStack.last().pose(), Renders.getBuffer(Renders.Type.LIGHTNING_NO_CULL), 2.5f, 0.75f, color);
        });
    }
}
