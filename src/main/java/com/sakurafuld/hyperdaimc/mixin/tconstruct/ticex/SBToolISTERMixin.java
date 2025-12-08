package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.addon.tconstruct.HyperModifiers;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatItemRenderer;
import moffy.ticex.client.modules.slashblade.SBToolISTER;
import moffy.ticex.client.rendering.ItemRenderContext;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.Random;

@Pseudo
@Mixin(SBToolISTER.class)
@OnlyIn(Dist.CLIENT)
public abstract class SBToolISTERMixin {
    @Inject(method = "renderBlade", at = @At("HEAD"), remap = false)
    private void renderBladeTicEx(ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, CallbackInfo ci) {
        if (transformType != ItemDisplayContext.THIRD_PERSON_LEFT_HAND && transformType != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND && transformType != ItemDisplayContext.FIRST_PERSON_LEFT_HAND && transformType != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && transformType != ItemDisplayContext.NONE)
            GashatItemRenderer.lastMillisSBTool = Util.getMillis();
    }

    @Inject(method = "renderIcon(Lnet/minecraft/world/item/ItemStack;Lmoffy/ticex/client/rendering/ItemRenderContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IFZ)V", at = @At(value = "INVOKE", target = "Lmods/flammpfeil/slashblade/client/renderer/util/BladeRenderState;renderOverrided(Lnet/minecraft/world/item/ItemStack;Lmods/flammpfeil/slashblade/client/renderer/model/obj/WavefrontObject;Ljava/lang/String;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", remap = false), remap = false)
    private void renderIconTicEx$BEFORE(ItemStack stack, ItemRenderContext itemRenderContext, PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, float scale, boolean renderDurability, CallbackInfo ci) {
        ToolStack tool = ToolStack.from(stack);
        if (this.isGashat(tool)) {
            matrixStack.pushPose();

            long lastMillis = Math.abs(GashatItemRenderer.lastMillisSBTool);
            Random random = new Random(lastMillis);
            long millis = lastMillis + Mth.square(tool.getMaterials().toString().length() * 16L);
            double time = millis % 20000;
            if (time <= 200 || (6000 < time && time <= 6200) || (10000 < time && (time <= 10300)) || (10400 < time && (time <= 10450)))
                matrixStack.scale(random.nextFloat(0.5f, 1.75f), random.nextFloat(0.5f, 1.75f), random.nextFloat(0.5f, 1.75f));

            float cos = Mth.cos(millis / 800f);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(cos * 8));
            matrixStack.mulPose(Axis.XP.rotationDegrees(cos * 2));
            matrixStack.mulPose(Axis.YP.rotationDegrees(cos * 2));
        }
    }

    @Inject(method = "renderIcon(Lnet/minecraft/world/item/ItemStack;Lmoffy/ticex/client/rendering/ItemRenderContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IFZ)V", at = @At(value = "INVOKE", target = "Lmods/flammpfeil/slashblade/client/renderer/util/BladeRenderState;renderOverrided(Lnet/minecraft/world/item/ItemStack;Lmods/flammpfeil/slashblade/client/renderer/model/obj/WavefrontObject;Ljava/lang/String;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", shift = At.Shift.AFTER, remap = false), remap = false)
    private void renderIconTicEx$AFTER(ItemStack stack, ItemRenderContext itemRenderContext, PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, float scale, boolean renderDurability, CallbackInfo ci) {
        ToolStack tool = ToolStack.from(stack);
        if (this.isGashat(tool))
            matrixStack.popPose();
    }

    @Unique
    private boolean isGashat(ToolStack tool) {
        return !tool.isBroken() && (tool.getModifierLevel(HyperModifiers.NOVEL.getId()) > 0 || tool.getModifierLevel(HyperModifiers.PARADOX.getId()) > 0);
    }
}
