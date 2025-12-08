package com.sakurafuld.hyperdaimc.mixin.tconstruct;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.addon.tconstruct.HyperModifiers;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatItemRenderer;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import moffy.ticex.item.modifiable.ModifiableSlashBladeItem;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.Random;
import java.util.Set;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.*;

@Mixin(ItemRenderer.class)
@OnlyIn(Dist.CLIENT)
public abstract class ItemRendererMixin {
    @Unique
    private static final Random RANDOM = new Random();
    @Unique
    private final Set<GashatItemRenderer.Particle> PARTICLES = Sets.newHashSet();
    @Unique
    private int color = 0xFFFFFFFF;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0, shift = At.Shift.AFTER))
    private void renderTConstruct$HEAD(ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel, CallbackInfo ci) {
        if (!pItemStack.isEmpty() && !this.isSlashblade(pItemStack) && this.isGashat(pItemStack)) {
            ToolStack tool = ToolStack.from(pItemStack);
            long millis = Util.getMillis() + Mth.square((long) tool.getMaterials().toString().length() * 16L + pItemStack.getDescriptionId().length() * 32L);
            double time = millis % 20000;
            if (time <= 200 || (6000 < time && time <= 6200) || (10000 < time && (time <= 10300)) || (10400 < time && (time <= 10450))) {
                pPoseStack.scale(RANDOM.nextFloat(0.5f, 1.75f), RANDOM.nextFloat(0.5f, 1.75f), RANDOM.nextFloat(0.5f, 1.75f));
                if (10000 < time)
                    this.color = (0xFF000000) | RANDOM.nextInt(0xFFFFFF);
                else this.color = 0xFFFFFFFF;
            } else this.color = 0xFFFFFFFF;

            float cos = Mth.cos(millis / 800f);

            pPoseStack.mulPose(Axis.ZP.rotationDegrees(cos * 8));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(cos * 2));
            pPoseStack.mulPose(Axis.YP.rotationDegrees(cos * 2));
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"))
    private void renderTConstruct$Color(ItemRenderer instance, BakedModel pModel, ItemStack pStack, int pCombinedLight, int pCombinedOverlay, PoseStack pMatrixStack, VertexConsumer pBuffer) {
        if (!this.isSlashblade(pStack) && this.isGashat(pStack))
            Renders.model(pModel, pMatrixStack, pBuffer, pCombinedLight, pCombinedOverlay, quad -> this.color);
        else
            instance.renderModelLists(pModel, pStack, pCombinedLight, pCombinedOverlay, pMatrixStack, pBuffer);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", ordinal = 1))
    private void renderTConstruct$Particle(ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, int pCombinedOverlay, BakedModel pModel, CallbackInfo ci) {
        if (this.isGashat(pItemStack)) {
            for (int count = 0; count < 3; count++)
                if (RANDOM.nextInt(400) == 0)
                    PARTICLES.add(new GashatItemRenderer.Particle(pItemStack, () -> pModel));
            Renders.with(pPoseStack, () -> {
                if (!this.isSlashblade(pItemStack) || pDisplayContext == ItemDisplayContext.GUI || (pDisplayContext == ItemDisplayContext.FIXED && !this.isBladeStand(pItemStack.getFrame()))) {
                    switch (pDisplayContext) {
                        case GUI -> pPoseStack.translate(0, 0, 0.1);
                        case FIXED -> pPoseStack.translate(0, 0, -0.1);
                    }
                    PARTICLES.removeIf(particle -> particle.render(pItemStack, pDisplayContext, pPoseStack, pBuffer, pCombinedLight, pCombinedOverlay));
                }
            });
        }
    }

    @Unique
    private boolean isGashat(ItemStack stack) {
        if (stack.getItem() instanceof IModifiable) {
            ToolStack tool = ToolStack.from(stack);
            return !tool.isBroken() && (tool.getModifierLevel(HyperModifiers.NOVEL.getId()) > 0 || tool.getModifierLevel(HyperModifiers.PARADOX.getId()) > 0);
        } else return false;
    }

    @Unique
    private boolean isSlashblade(ItemStack stack) {
        return require(TICEX) && require(SLASHBLADE) && stack.getItem() instanceof ModifiableSlashBladeItem;
    }

    @Unique
    private boolean isBladeStand(ItemFrame frame) {
        return require(SLASHBLADE) && frame instanceof BladeStandEntity;
    }
}
