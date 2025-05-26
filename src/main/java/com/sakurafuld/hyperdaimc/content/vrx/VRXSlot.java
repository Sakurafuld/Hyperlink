package com.sakurafuld.hyperdaimc.content.vrx;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class VRXSlot extends SlotItemHandler {
    private VRXOne one = VRXOne.EMPTY;

    public VRXSlot(ItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return false;
    }

    public void clicked(VRXMenu menu, int button, ClickType type) {
        if (!menu.getCarried().isEmpty()) {
            VRXOne one = VRXOne.Type.convert(menu.getCarried());
            one.stackSlot(menu, this, button, type);
        } else if (!this.isEmpty()) {
            this.getOne().stackSlot(menu, this, button, type);
        }
    }

    public boolean scrolled(VRXMenu menu, double delta, boolean shiftDown) {
        if (!menu.getCarried().isEmpty()) {
            VRXOne one = VRXOne.Type.convert(menu.getCarried());
            return one.scrollSlot(menu, this, delta, shiftDown);
        } else if (!this.isEmpty()) {
            return this.getOne().scrollSlot(menu, this, delta, shiftDown);
        } else {
            return false;
        }
    }

    public VRXOne getOne() {
        return this.one;
    }

    public void setOne(VRXOne one) {
        this.one = one;
    }

    public boolean isEmpty() {
        return this.getOne().isEmpty();
    }

    public void grow(long quantity) {
        quantity = Math.max(0, Math.round(Math.min(Long.MAX_VALUE, (double) this.getOne().getQuantity() + (double) quantity)));
        if (quantity > 0) {
            this.getOne().setQuantity(quantity);
        } else {
            this.setOne(VRXOne.EMPTY);
        }
        this.setChanged();
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        return !this.isEmpty() && this.getOne() instanceof VRXOne.Item item ? item.getItemStack() : ItemStack.EMPTY;
    }

    @Override
    public void set(@NotNull ItemStack stack) {
    }

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return Integer.MAX_VALUE;
    }
}
