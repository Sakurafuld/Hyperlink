package com.sakurafuld.hyperdaimc.addon.jei;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXScreen;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXSetJeiSimple;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record SimpleGhostTarget(VRXScreen screen, Slot slot) implements IGhostIngredientHandler.Target<ItemStack> {
    @Override
    public Rect2i getArea() {
        return new Rect2i(this.screen().getGuiLeft() + this.slot().x, this.screen().getGuiTop() + this.slot().y, 16, 16);
    }

    @Override
    public void accept(ItemStack stack) {
        if (this.slot().getItem().isEmpty()) {
            this.slot().set(stack.copy());
            HyperConnection.INSTANCE.sendToServer(new ServerboundVRXSetJeiSimple(this.screen().getMenu().containerId, this.slot().index, stack));
        }
    }
}
