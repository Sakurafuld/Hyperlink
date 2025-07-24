package com.sakurafuld.hyperdaimc.mixin.gameorb;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.crafting.gameorb.GameOrbRenderer;
import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
@OnlyIn(Dist.CLIENT)
public abstract class ItemEntityRendererMixin {
    @Inject(method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"))
    private void renderGameOrb(ItemEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        if (pEntity.getItem().is(HyperItems.GAME_ORB.get())) {

            Vec3 vec = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(pEntity.getBoundingBox().getCenter());
            Renders.with(pMatrixStack, () -> {

                pMatrixStack.scale(0.5f, 0.5f, 0.5f);
                pMatrixStack.mulPose(Vector3f.YN.rotation(pEntity.getSpin(pPartialTicks)));
                pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float) Math.toDegrees(Mth.atan2(vec.x(), vec.z()))));
                pMatrixStack.mulPose(Vector3f.XN.rotationDegrees((float) Math.toDegrees(Mth.atan2(vec.y(), vec.horizontalDistance()))));
                pMatrixStack.translate(-0.5, -0.5, -0.5);

                GameOrbRenderer.renderHalo(pMatrixStack, ItemRenderer.getFoilBuffer(pBuffer, Sheets.translucentCullBlockSheet(), true, pEntity.getItem().hasFoil()), ItemTransforms.TransformType.GROUND, pPackedLight, OverlayTexture.NO_OVERLAY);
            });
        }
    }
}
