package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class VRXSlot extends SlotItemHandler {
    private final VRXMenu menu;
    private VRXOne one;

    public VRXSlot(ItemStackHandler itemHandler, int index, int xPosition, int yPosition, VRXMenu menu, VRXOne one) {
        super(itemHandler, index, xPosition, yPosition);
        this.menu = menu;
        this.one = one;
    }

    @Override
    public void setChanged() {
        this.menu.onVRXChanged(((Slot) this).index);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return false;
    }

    public void clicked(int button, ClickType type) {
        if (!this.menu.getCarried().isEmpty()) {
            VRXOne one = VRXRegistry.convert(this.menu.getCarried(), this.menu.getAvailableTypes());
            if (!one.isEmpty())
                one.stackSlot(this.menu, this, button, type);
        } else if (!this.isEmpty()) {
            this.getOne().stackSlot(this.menu, this, button, type);
        }
    }

    public boolean scrolled(double delta, boolean shiftDown) {
        if (!this.menu.getCarried().isEmpty()) {
            VRXOne one = VRXRegistry.convert(this.menu.getCarried(), this.menu.getAvailableTypes());
            return !one.isEmpty() && one.scrollSlot(this.menu, this, delta, shiftDown);
        } else if (!this.isEmpty()) {
            return this.getOne().scrollSlot(this.menu, this, delta, shiftDown);
        } else {
            return false;
        }
    }

    public VRXOne getOne() {
        return this.one;
    }

    public void setOne(VRXOne one) {
        this.one = one;
        this.setChanged();
    }

    public boolean isEmpty() {
        return this.getOne().isEmpty();
    }

    public void grow(long quantity) {
        quantity = Math.max(0, Math.round(Math.min(Long.MAX_VALUE, (double) this.getOne().getQuantity() + (double) quantity)));
        if (quantity > 0)
            this.getOne().setQuantity(quantity);
        else this.setOne(VRXOne.EMPTY);

        this.setChanged();
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        return !this.isEmpty() && this.getOne() instanceof VRXOneItem item ? item.getItemStack() : ItemStack.EMPTY;
    }

    @Override
    public void set(@NotNull ItemStack stack) {
        this.setChanged();
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
