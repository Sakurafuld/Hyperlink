package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.addon.tconstruct.HyperModifiers;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatItemRenderer;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import mods.flammpfeil.slashblade.init.DefaultResources;
import moffy.ticex.item.modifiable.ModifiableSlashBladeItem;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.EmptyModel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.Random;
import java.util.Set;
import java.util.function.Function;

@Pseudo
@Mixin(BladeRenderState.class)
@OnlyIn(Dist.CLIENT)
public abstract class BladeRenderStateMixin {
    @Unique
    private static final Random RANDOM = new Random();
    @Unique
    private static final Set<GashatItemRenderer.Particle> PARTICLES = Sets.newHashSet();
    @Unique
    private static WavefrontObject durability = null;

    @Inject(method = "renderOverrided(Lnet/minecraft/world/item/ItemStack;Lmods/flammpfeil/slashblade/client/renderer/model/obj/WavefrontObject;Ljava/lang/String;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILjava/util/function/Function;Z)V", at = @At("HEAD"), remap = false)
    private static void renderOverridedTicEx$HEAD(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Function<ResourceLocation, RenderType> getRenderType, boolean enableEffect, CallbackInfo ci) {
        if (durability == null)
            durability = BladeModelManager.getInstance().getModel(DefaultResources.resourceDurabilityModel);
        if (model == durability)
            return;
        if (isGashat(stack)) {
            ToolStack tool = ToolStack.from(stack);
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

            @Nullable
            ItemDisplayContext transform = GashatItemRenderer.transformSBTool;
            float cos = Mth.cos(millis / 800f);
            if (tps) {
                matrixStackIn.mulPose(Axis.ZP.rotationDegrees(cos * 10));
                matrixStackIn.mulPose(Axis.XP.rotationDegrees(cos * 10));
                matrixStackIn.mulPose(Axis.YP.rotationDegrees(cos * 10 + 10));
            } else if (transform == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
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
            boolean render = tps || stack.isFramed() || transform == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || transform == ItemDisplayContext.GROUND;
            if (render && (!stack.isFramed() || stack.getFrame() instanceof BladeStandEntity)) {
                Renders.with(matrixStackIn, () -> {
                    matrixStackIn.scale(128, 128, 128);
                    PARTICLES.removeIf(particle -> particle.render(stack, transform == null ? ItemDisplayContext.FIXED : transform, matrixStackIn, bufferIn, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY));
                });
            }
        }
    }

    @Inject(method = "renderOverrided(Lnet/minecraft/world/item/ItemStack;Lmods/flammpfeil/slashblade/client/renderer/model/obj/WavefrontObject;Ljava/lang/String;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILjava/util/function/Function;Z)V", at = @At("RETURN"), remap = false)
    private static void renderOverridedTicEx$RETURN(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Function<ResourceLocation, RenderType> getRenderType, boolean enableEffect, CallbackInfo ci) {
        if (model == durability)
            return;
        if (isGashat(stack))
            matrixStackIn.popPose();
    }

    @Unique
    private static boolean isGashat(ItemStack stack) {
        if (stack.getItem() instanceof ModifiableSlashBladeItem) {
            ToolStack tool = ToolStack.from(stack);
            return !tool.isBroken() && (tool.getModifierLevel(HyperModifiers.NOVEL.getId()) > 0 || tool.getModifierLevel(HyperModifiers.PARADOX.getId()) > 0);
        } else return false;
    }
}
