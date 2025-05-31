package com.sakurafuld.hyperdaimc.mixin.novel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.api.mixin.IMetapotentFlashfurNovel;
import com.sakurafuld.hyperdaimc.content.novel.NovelHandler;
import flashfur.omnimobs.entities.metapotent_flashfur.MetapotentFlashfurEntity;
import flashfur.omnimobs.entities.metapotent_flashfur.MetapotentFlashfurRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(MetapotentFlashfurRenderer.class)
public abstract class MetapotentFlashfurRendererMixin {
    @Inject(method = "render(Lflashfur/omnimobs/entities/metapotent_flashfur/MetapotentFlashfurEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), remap = false)
    private void renderNovel(MetapotentFlashfurEntity flashfur, float v, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (flashfur.metapotentFlashfur instanceof IMetapotentFlashfurNovel flashfurNovel) {
            if (NovelHandler.novelized(flashfurNovel.getOriginal())) {
                flashfur.deathTime = flashfurNovel.getTime();
            }
        }
    }
}
