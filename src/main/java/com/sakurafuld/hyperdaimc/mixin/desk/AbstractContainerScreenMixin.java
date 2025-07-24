package com.sakurafuld.hyperdaimc.mixin.desk;

import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Mth;
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
    private void renderSlotDesk$BEFORE(GuiGraphics pGuiGraphics, Slot pSlot, CallbackInfo ci) {
        if ((Object) this instanceof DeskScreen screen) {
            if (pSlot.index < 82) {
                float partialTick = Minecraft.getInstance().getFrameTime();
                float pop = pSlot.getItem().getPopTime() - partialTick;
                if (pop > 0) {
                    float popper = 1 + pop / 5;
                    pGuiGraphics.pose().pushPose();
                    pGuiGraphics.pose().translate(pSlot.x + 8, pSlot.y + 12, 0);
                    pGuiGraphics.pose().scale(1 / popper, (popper + 1) / 2, 1);
                    pGuiGraphics.pose().translate(-(pSlot.x + 8), -(pSlot.y + 12), 0);
                } else if (pSlot.index == 0 && screen.canCraftTicks > 0 && 37 > screen.canCraftTicks) {

                    float x = Mth.lerp(partialTick, screen.resultOldPos.x, screen.resultPos.x);
                    float y = Mth.lerp(partialTick, screen.resultOldPos.y, screen.resultPos.y);
                    float size = Mth.lerp(partialTick, screen.resultOldSize, screen.resultSize);

                    pGuiGraphics.pose().pushPose();
                    pGuiGraphics.pose().translate(x, y, 0);
                    pGuiGraphics.pose().translate(pSlot.x + 8, pSlot.y + 8, 0);
                    pGuiGraphics.pose().scale(size, size, size);
                    pGuiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(partialTick, screen.resultOldRot, screen.resultRot)));
                    pGuiGraphics.pose().translate(-(pSlot.x + 8), -(pSlot.y + 8), 0);
                }
            }
        }
    }

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;III)V", shift = At.Shift.AFTER))
    private void renderSlotDesk$AFTER(GuiGraphics pGuiGraphics, Slot pSlot, CallbackInfo ci) {
        if ((Object) this instanceof DeskScreen screen) {
            if (pSlot.index < 82) {
                float partialTick = Minecraft.getInstance().getFrameTime();
                float pop = pSlot.getItem().getPopTime() - partialTick;
                if (pop > 0 || (pSlot.index == 0 && screen.canCraftTicks > 0 && 37 > screen.canCraftTicks)) {
                    pGuiGraphics.pose().popPose();
                }
            }
        }
    }
}
