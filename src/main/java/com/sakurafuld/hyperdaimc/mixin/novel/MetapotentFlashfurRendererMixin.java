package com.sakurafuld.hyperdaimc.mixin.novel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.content.hyper.novel.system.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IMetapotentFlashfurNovel;
import flashfur.omnimobs.entities.metapotent_flashfur.MetapotentFlashfurEntity;
import flashfur.omnimobs.entities.metapotent_flashfur.MetapotentFlashfurRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(MetapotentFlashfurRenderer.class)
public abstract class MetapotentFlashfurRendererMixin {
    @Unique
    private MetapotentFlashfurEntity flashfur;

    @Inject(method = "renderLiving", at = @At(value = "INVOKE", target = "Lflashfur/omnimobs/entities/metapotent_flashfur/MetapotentFlashfurRenderer;setupRotations(Lflashfur/omnimobs/entities/metapotent_flashfur/MetapotentFlashfurEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V", remap = false), remap = false)
    private void renderNovel(MetapotentFlashfurEntity metapotentFlashfur, float v, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int i1, CallbackInfo ci) {
        this.flashfur = metapotentFlashfur;
        if (flashfur.metapotentFlashfur instanceof IMetapotentFlashfurNovel flashfurNovel && NovelHandler.novelized(flashfurNovel.hyperdaimc$getOriginal()))
            if (flashfurNovel.hyperdaimc$getTime() > 0) {
                float f = (flashfurNovel.hyperdaimc$getTime() + partialTick - 1f) / 20f * 1.6f;
                f = Math.min(1, Mth.sqrt(f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(f * 90));
            }
    }

    @ModifyArg(method = "renderLiving", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/OverlayTexture;pack(II)I"), index = 1, remap = false)
    private int renderLivingNovel(int pU) {
        if (this.flashfur.metapotentFlashfur instanceof IMetapotentFlashfurNovel flashfurNovel && NovelHandler.novelized(flashfurNovel.hyperdaimc$getOriginal()))
            return OverlayTexture.v(true);
        return pU;
    }
}
