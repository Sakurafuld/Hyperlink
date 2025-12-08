package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatItemRenderer;
import moffy.ticex.client.modules.slashblade.SBToolFirstPersonRender;
import moffy.ticex.client.rendering.ItemRenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(SBToolFirstPersonRender.class)
@OnlyIn(Dist.CLIENT)
public abstract class SBToolFirstPersonRenderMixin {
    @Inject(method = "render", at = @At("HEAD"), remap = false)
    private void renderTicEx$HEAD(PoseStack matrixStack, ItemRenderContext itemRenderContext, MultiBufferSource bufferIn, int combinedLightIn, CallbackInfo ci) {
        GashatItemRenderer.transformSBTool = ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }

    @Inject(method = "render", at = @At("RETURN"), remap = false)
    private void renderTicEx$RETURN(PoseStack matrixStack, ItemRenderContext itemRenderContext, MultiBufferSource bufferIn, int combinedLightIn, CallbackInfo ci) {
        GashatItemRenderer.transformSBTool = null;
    }
}
