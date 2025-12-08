package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatItemRenderer;
import moffy.ticex.client.modules.slashblade.LayerSBToolMainBlade;
import moffy.ticex.client.rendering.ItemRenderContext;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(LayerSBToolMainBlade.class)
@OnlyIn(Dist.CLIENT)
public abstract class LayerSBToolMainBladeMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Inject(method = "renderStandbyBlade", at = @At("HEAD"), remap = false)
    private void renderStandbyBladeTicEx(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, ItemStack blade, T entity, CallbackInfo ci) {
        GashatItemRenderer.lastMillisSBTool = -Util.getMillis();
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"), remap = false)
    private void renderTicEx(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        GashatItemRenderer.lastMillisSBTool = -Util.getMillis();
    }

    @Inject(method = "renderItemEntity", at = @At("HEAD"), remap = false)
    private void renderItemEntityTicEx(PoseStack matrixStack, MultiBufferSource bufferIn, ItemRenderContext itemRenderContext, int lightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        GashatItemRenderer.lastMillisSBTool = Util.getMillis();
    }
}
