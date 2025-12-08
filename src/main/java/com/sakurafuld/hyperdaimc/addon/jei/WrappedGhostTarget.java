package com.sakurafuld.hyperdaimc.addon.jei;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXJeiWrapper;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXScreen;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXSlot;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record WrappedGhostTarget<I>(VRXScreen screen, VRXSlot slot,
                                    VRXJeiWrapper<I> wrapper) implements IGhostIngredientHandler.Target<I> {
    @Override
    public Rect2i getArea() {
        return new Rect2i(this.screen().getGuiLeft() + this.slot.x, this.screen().getGuiTop() + this.slot.y, 16, 16);
    }

    @Override
    public void accept(I ingredient) {
        this.wrapper().accept(this.screen().getMenu().containerId, this.slot());
    }
}
