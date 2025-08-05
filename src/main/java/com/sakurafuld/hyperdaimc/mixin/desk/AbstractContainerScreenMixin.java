package com.sakurafuld.hyperdaimc.mixin.desk;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Mth;
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
    private void renderSlotDesk$BEFORE(PoseStack pPoseStack, Slot pSlot, CallbackInfo ci) {
        if ((Object) this instanceof DeskScreen screen) {
            if (pSlot.index < 82) {
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
                } else if (pSlot.index == 0 && screen.canCraftTicks > 0 && 37 > screen.canCraftTicks) {

                    float x = Mth.lerp(partialTick, screen.resultOldPos.x, screen.resultPos.x);
                    float y = Mth.lerp(partialTick, screen.resultOldPos.y, screen.resultPos.y);
                    float size = Mth.lerp(partialTick, screen.resultOldSize, screen.resultSize);

                    this.temporaryPose = RenderSystem.getModelViewStack();
                    this.temporaryPose.pushPose();
                    this.temporaryPose.translate(x, y, 0);
                    this.temporaryPose.translate(pSlot.x + 8, pSlot.y + 8, 0);
                    this.temporaryPose.scale(size, size, 1);
                    this.temporaryPose.mulPose(Vector3f.ZP.rotationDegrees(Mth.rotLerp(partialTick, screen.resultOldRot, screen.resultRot)));
                    this.temporaryPose.translate(-(pSlot.x + 8), -(pSlot.y + 8), 0);
                    RenderSystem.applyModelViewMatrix();
                }
            }
        }
    }

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderAndDecorateItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;III)V", shift = At.Shift.AFTER))
    private void renderSlotDesk$AFTER(PoseStack pPoseStack, Slot pSlot, CallbackInfo ci) {
        if ((Object) this instanceof DeskScreen) {
            if (pSlot.index < 82) {
                if (this.temporaryPose != null) {
                    this.temporaryPose.popPose();
                    RenderSystem.applyModelViewMatrix();
                    this.temporaryPose = null;
                }
            }
        }
    }
}
