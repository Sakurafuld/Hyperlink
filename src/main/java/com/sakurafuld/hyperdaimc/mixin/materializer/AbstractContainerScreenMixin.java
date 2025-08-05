package com.sakurafuld.hyperdaimc.mixin.materializer;

import com.sakurafuld.hyperdaimc.content.over.materializer.MaterializerScreen;
import net.minecraft.client.Minecraft;
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
    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;III)V"))
    private void renderSlotMaterializer$BEFORE(GuiGraphics pGuiGraphics, Slot pSlot, CallbackInfo ci) {
        if ((Object) this instanceof MaterializerScreen) {
            if (pSlot.index == 0) {
                float partialTick = Minecraft.getInstance().getPartialTick();
                float pop = pSlot.getItem().getPopTime() - partialTick;
                if (pop > 0) {
                    float popper = 1 + pop / 5;
                    pGuiGraphics.pose().pushPose();
                    pGuiGraphics.pose().translate(pSlot.x + 8, pSlot.y + 12, 0);
                    pGuiGraphics.pose().scale(1 / popper, (popper + 1) / 2, 1);
                    pGuiGraphics.pose().translate(-(pSlot.x + 8), -(pSlot.y + 12), 0);
                }
            }
        }
    }

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;III)V", shift = At.Shift.AFTER))
    private void renderSlotMaterializer$AFTER(GuiGraphics pGuiGraphics, Slot pSlot, CallbackInfo ci) {
        if ((Object) this instanceof MaterializerScreen) {
            if (pSlot.index == 0) {
                float partialTick = Minecraft.getInstance().getPartialTick();
                float pop = pSlot.getItem().getPopTime() - partialTick;
                if (pop > 0) {
                    pGuiGraphics.pose().popPose();
                }
            }
        }
    }
}
