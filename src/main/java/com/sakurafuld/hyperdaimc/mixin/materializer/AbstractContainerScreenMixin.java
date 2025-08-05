package com.sakurafuld.hyperdaimc.mixin.materializer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.content.over.materializer.MaterializerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
@OnlyIn(Dist.CLIENT)
public abstract class AbstractContainerScreenMixin {
    @Unique
    private PoseStack temporaryPose = null;

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderAndDecorateItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V"))
    private void renderSlotMaterializer$BEFORE(PoseStack pPoseStack, Slot pSlot, CallbackInfo ci) {
        if ((Object) this instanceof MaterializerScreen) {
            if (pSlot.index == 0) {
                float partialTick = Minecraft.getInstance().getFrameTime();
                float pop = pSlot.getItem().getPopTime() - partialTick;
                if (pop > 0) {
                    float popper = 1 + pop / 5;
                    this.temporaryPose = RenderSystem.getModelViewStack();
                    this.temporaryPose.pushPose();
                    this.temporaryPose.translate(pSlot.x + 8, pSlot.y + 12, 0);
                    this.temporaryPose.scale(1 / popper, (popper + 1) / 2, 1);
                    this.temporaryPose.translate(-(pSlot.x + 8), -(pSlot.y + 12), 0);
                    RenderSystem.applyModelViewMatrix();
                }
            }
        }
    }

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderAndDecorateItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V", shift = At.Shift.AFTER))
    private void renderSlotMaterializer$AFTER(PoseStack pPoseStack, Slot pSlot, CallbackInfo ci) {
        if ((Object) this instanceof MaterializerScreen) {
            if (pSlot.index == 0) {
                if (this.temporaryPose != null) {
                    this.temporaryPose.popPose();
                    RenderSystem.applyModelViewMatrix();
                    this.temporaryPose = null;
                }
            }
        }
    }
}
