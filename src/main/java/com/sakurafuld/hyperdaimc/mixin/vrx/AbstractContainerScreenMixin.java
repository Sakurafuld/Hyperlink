package com.sakurafuld.hyperdaimc.mixin.vrx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.content.vrx.VRXSlot;
import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void renderSlotVRX(PoseStack pPoseStack, Slot pSlot, CallbackInfo ci) {
        if (pSlot instanceof VRXSlot slot && !slot.isEmpty()) {
            Renders.with(pPoseStack, () -> {
                slot.getOne().render(pPoseStack, slot.x, slot.y);
            });
            ci.cancel();
        }
    }
}
