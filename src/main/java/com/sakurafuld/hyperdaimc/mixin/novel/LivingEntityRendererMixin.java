package com.sakurafuld.hyperdaimc.mixin.novel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityNovel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
@OnlyIn(Dist.CLIENT)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Inject(method = "setupRotations", at = @At("HEAD"))
    private void setupRotationsNovel(T pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks, CallbackInfo ci) {
        int dead = ((IEntityNovel) pEntityLiving).hyperdaimc$novelDead(false);
        if (dead > 0) pEntityLiving.deathTime = Math.max(dead, pEntityLiving.deathTime);
    }
}
