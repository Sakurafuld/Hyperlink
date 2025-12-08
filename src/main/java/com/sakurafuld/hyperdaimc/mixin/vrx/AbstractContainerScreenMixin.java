package com.sakurafuld.hyperdaimc.mixin.vrx;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXSlot;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
@OnlyIn(Dist.CLIENT)
public abstract class AbstractContainerScreenMixin {
    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void renderSlotVRX(GuiGraphics pGuiGraphics, Slot pSlot, CallbackInfo ci) {
        if (pSlot instanceof VRXSlot slot && !slot.isEmpty()) {
            Renders.with(pGuiGraphics.pose(), () -> {
                slot.getOne().render(pGuiGraphics, slot.x, slot.y);
            });
            ci.cancel();
        }
    }
}
