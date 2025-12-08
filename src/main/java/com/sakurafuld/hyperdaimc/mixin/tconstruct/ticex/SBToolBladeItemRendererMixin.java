package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.sakurafuld.hyperdaimc.infrastructure.render.GashatItemRenderer;
import moffy.ticex.client.modules.slashblade.SBToolBladeItemRenderer;
import moffy.ticex.client.rendering.ItemRenderContext;
import net.minecraft.Util;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(SBToolBladeItemRenderer.class)
@OnlyIn(Dist.CLIENT)
public abstract class SBToolBladeItemRendererMixin {
    @Inject(method = "renderBlade", at = @At("HEAD"), remap = false)
    private void renderBladeTicEx$HEAD(ItemEntity itemIn, ItemRenderContext itemRenderContext, float entityYaw, float partialTicks, CallbackInfo ci) {
        GashatItemRenderer.lastMillisSBTool = Util.getMillis();
        GashatItemRenderer.transformSBTool = ItemDisplayContext.GROUND;
    }

    @Inject(method = "renderBlade", at = @At("RETURN"), remap = false)
    private void renderBladeTicEx$RETURN(ItemEntity itemIn, ItemRenderContext itemRenderContext, float entityYaw, float partialTicks, CallbackInfo ci) {
        GashatItemRenderer.transformSBTool = null;
    }
}
