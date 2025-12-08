package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.addon.tconstruct.HyperModifiers;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatItemRenderer;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import moffy.ticex.client.modules.slashblade.SBToolRenderState;
import moffy.ticex.client.rendering.ItemRenderContext;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.EmptyModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.Random;
import java.util.Set;

@Pseudo
@Mixin(SBToolRenderState.class)
@OnlyIn(Dist.CLIENT)
public abstract class SBToolRenderStateMixin {
    @Unique
    private static final Random RANDOM = new Random();
    @Unique
    private static final Set<GashatItemRenderer.Particle> PARTICLES = Sets.newHashSet();

    @Inject(method = "renderOverride(Lnet/minecraft/world/item/ItemStack;Lmoffy/ticex/client/rendering/ItemRenderContext;Lslimeknights/tconstruct/library/tools/nbt/ToolStack;Lmods/flammpfeil/slashblade/client/renderer/model/obj/WavefrontObject;Ljava/lang/String;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILmoffy/ticex/client/modules/slashblade/SBToolRenderState$RenderGetter;Z)V", at = @At("HEAD"), remap = false)
    private static void renderOverrideTicEx$HEAD(ItemStack stack, ItemRenderContext itemRenderContext, ToolStack tool, WavefrontObject model, String target, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, SBToolRenderState.RenderGetter<MaterialVariantId, Runnable, RenderType> getRenderType, boolean enableEffect, CallbackInfo ci) {
        if (isGashat(tool)) {
            matrixStackIn.pushPose();
            long lastMillis = GashatItemRenderer.lastMillisSBTool;
            boolean tps = false;
            if (lastMillis < 0) {
                lastMillis = -lastMillis;
                tps = true;
            }

            Random seeded = new Random(lastMillis);
            long millis = lastMillis + Mth.square(tool.getMaterials().toString().length() * 16L);
            double time = millis % 20000;
            if (time <= 200 || (6000 < time && time <= 6200) || (10000 < time && (time <= 10300)) || (10400 < time && (time <= 10450)))
                matrixStackIn.scale(seeded.nextFloat(0.5f, 1.75f), seeded.nextFloat(0.5f, 1.75f), seeded.nextFloat(0.5f, 1.75f));

            ItemDisplayContext transform = itemRenderContext.displayContext();
            float cos = Mth.cos(millis / 800f);
            if (tps) {
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(cos * 10));
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(cos * 10));
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(cos * 10 + 10));
            } else if (transform.firstPerson()) {
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(cos * 10));
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(cos * 6));
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(cos * 10 + 10));
            } else if (transform == ItemDisplayContext.GROUND) {
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(cos * 2));
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(cos * 2));
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(cos * 2));
            } else {
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(cos * 8));
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(cos * 2));
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(cos * 2));
            }

            for (int count = 0; count < 6; count++)
                if (RANDOM.nextInt(400) == 0) {
                    float offset = RANDOM.nextFloat() * -2.75f;
                    float rotationX = RANDOM.nextFloat(90);
                    float rotationY = RANDOM.nextFloat(90);
                    PARTICLES.add(new GashatItemRenderer.Particle(stack, () -> EmptyModel.BAKED, poseStack -> {
                        poseStack.translate(offset, -0.5f, -0.5f);
                        poseStack.mulPose(Axis.XP.rotationDegrees(rotationX));
                        poseStack.mulPose(Axis.YP.rotationDegrees(rotationY));
                    }));
                }
            switch (transform) {
                case FIXED, FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND, GROUND -> {
                    if (!(transform == ItemDisplayContext.FIXED && stack.isFramed() && !(stack.getFrame() instanceof BladeStandEntity))) {
                        Renders.with(matrixStackIn, () -> {
                            matrixStackIn.scale(128, 128, 128);
                            PARTICLES.removeIf(particle -> particle.render(stack, transform, matrixStackIn, bufferIn, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY));
                        });
                    }
                }
            }
        }
    }

    @Inject(method = "renderOverride(Lnet/minecraft/world/item/ItemStack;Lmoffy/ticex/client/rendering/ItemRenderContext;Lslimeknights/tconstruct/library/tools/nbt/ToolStack;Lmods/flammpfeil/slashblade/client/renderer/model/obj/WavefrontObject;Ljava/lang/String;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILmoffy/ticex/client/modules/slashblade/SBToolRenderState$RenderGetter;Z)V", at = @At("RETURN"), remap = false)
    private static void renderOverrideTicEx$RETURN(ItemStack stack, ItemRenderContext itemRenderContext, ToolStack tool, WavefrontObject model, String target, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, SBToolRenderState.RenderGetter<MaterialVariantId, Runnable, RenderType> getRenderType, boolean enableEffect, CallbackInfo ci) {
        if (isGashat(tool))
            matrixStackIn.popPose();
    }

    @Unique
    private static boolean isGashat(ToolStack tool) {
        return !tool.isBroken() && (tool.getModifierLevel(HyperModifiers.NOVEL.getId()) > 0 || tool.getModifierLevel(HyperModifiers.PARADOX.getId()) > 0);
    }
}
