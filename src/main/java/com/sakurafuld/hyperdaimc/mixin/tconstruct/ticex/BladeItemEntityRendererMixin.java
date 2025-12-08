package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatItemRenderer;
import mods.flammpfeil.slashblade.client.renderer.entity.BladeItemEntityRenderer;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(BladeItemEntityRenderer.class)
@OnlyIn(Dist.CLIENT)
public abstract class BladeItemEntityRendererMixin {
    @Inject(method = "renderBlade", at = @At("HEAD"), remap = false)
    private void renderBladeTicEx(ItemEntity itemIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, CallbackInfo ci) {
        GashatItemRenderer.lastMillisSBTool = Util.getMillis();
    }
}
