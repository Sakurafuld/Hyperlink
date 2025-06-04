package com.sakurafuld.hyperdaimc.mixin.tconstruct;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.api.content.GashatRenderer;
import com.sakurafuld.hyperdaimc.compat.tconstruct.HyperModifiers;
import com.sakurafuld.hyperdaimc.helper.Renders;
import moffy.ticex.item.modifiable.ModifiableSlashBladeItem;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.Set;

import static com.sakurafuld.hyperdaimc.helper.Deets.TICEX;
import static com.sakurafuld.hyperdaimc.helper.Deets.require;

@Pseudo
@Mixin(ItemRenderer.class)
@OnlyIn(Dist.CLIENT)
public abstract class ItemRendererMixin {
    @Unique
    private int color = 0xFFFFFFFF;
    @Unique
    private final Set<GashatRenderer.Particle> PARTICLES = Sets.newHashSet();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0, shift = At.Shift.AFTER))
    private void renderTConstruct$Transform(ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel, CallbackInfo ci) {
        if (this.isGahast(pItemStack)) {
            long millis = Util.getMillis() + Mth.square(pItemStack.getDescriptionId().length() * 16);
            double time = millis % 20000;
            if (time <= 200 || (6000 < time && time <= 6200) || (10000 < time && (time <= 10300)) || (10400 < time && (time <= 10450))) {
                pPoseStack.scale(GashatRenderer.RANDOM.nextFloat(0.5f, 1.75f), GashatRenderer.RANDOM.nextFloat(0.5f, 1.75f), GashatRenderer.RANDOM.nextFloat(0.5f, 1.75f));
                if (10000 < time) {
                    this.color = (0xFF000000) | GashatRenderer.RANDOM.nextInt(0xFFFFFF);
                } else {
                    this.color = 0xFFFFFFFF;
                }
            } else {
                this.color = 0xFFFFFFFF;
            }

            float cos = Mth.cos(millis / 800f);

            pPoseStack.mulPose(Axis.ZP.rotationDegrees(cos * 8));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(cos * 2));
            pPoseStack.mulPose(Axis.YP.rotationDegrees(cos * 2));
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    private void renderTConstruct$Color(ItemRenderer instance, BakedModel pModel, ItemStack pStack, int pCombinedLight, int pCombinedOverlay, PoseStack pMatrixStack, VertexConsumer pBuffer) {
        if (this.isGahast(pStack)) {
            Renders.model(pModel, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay, quad -> this.color);
        } else {
            instance.renderModelLists(pModel, pStack, pCombinedLight, pCombinedOverlay, pMatrixStack, pBuffer);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 1))
    private void renderTConstruct$Particle(ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel, CallbackInfo ci) {
        if (this.isGahast(pItemStack)) {
            for (int count = 0; count < 3; count++) {
                if (GashatRenderer.RANDOM.nextInt(400) == 0) {
                    PARTICLES.add(new GashatRenderer.Particle(pItemStack, () -> pModel));
                }
            }

            Renders.with(pPoseStack, () -> {
                if (isSlashblade(pItemStack)) {
                    switch (pDisplayContext) {
                        case FIRST_PERSON_RIGHT_HAND -> {
                            pPoseStack.translate(-1.25, 0, -0.5);
                        }
                        case THIRD_PERSON_RIGHT_HAND -> {
                            pPoseStack.translate(-1, 0, 0);
                        }
                        case GUI -> {
                            pPoseStack.translate(0, 0, 0.1);
                        }
                        case FIXED -> {

                            pPoseStack.translate(0, 0, -0.1);
                        }
                    }
                }
                PARTICLES.removeIf(particle -> particle.render(pItemStack, pDisplayContext, pPoseStack, pBuffer, pCombinedLight, pCombinedOverlay));
            });
        }
    }

    @Unique
    private boolean isGahast(ItemStack stack) {
        if (stack.getItem() instanceof IModifiable) {
            ToolStack tool = ToolStack.from(stack);
            return !tool.isBroken() && tool.getModifierLevel(HyperModifiers.NOVEL.getId()) > 0;
        } else {
            return false;
        }
    }

    @Unique
    private boolean isSlashblade(ItemStack stack) {
        return require(TICEX).ready() && stack.getItem() instanceof ModifiableSlashBladeItem;
    }
}
