package com.sakurafuld.hyperdaimc.api.content;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class IOItemHandler<T extends IItemHandler & INBTSerializable<CompoundTag>> implements IItemHandler, INBTSerializable<CompoundTag> {
    protected final T input;
    protected final T output;

    public IOItemHandler(T input, T output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public int getSlots() {
        return this.input.getSlots() + this.output.getSlots();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < this.input.getSlots()) {
            return this.input.getStackInSlot(slot);
        } else {
            return this.output.getStackInSlot(slot - this.input.getSlots());
        }
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot < this.input.getSlots()) {
            return this.input.insertItem(slot, stack, simulate);
        } else {
            return stack;
        }
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < this.input.getSlots()) {
            return ItemStack.EMPTY;
        } else {
            return this.output.extractItem(slot - this.input.getSlots(), amount, simulate);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot < this.input.getSlots()) {
            return this.input.getSlotLimit(slot);
        } else {
            return this.output.getSlotLimit(slot - this.input.getSlots());
        }
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot < this.input.getSlots()) {
            return this.input.isItemValid(slot, stack);
        } else {
            return false;
        }
    }

    // INBTSerializable.
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("Input", this.input.serializeNBT());
        tag.put("Output", this.output.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.input.deserializeNBT(nbt.getCompound("Input"));
        this.output.deserializeNBT(nbt.getCompound("Output"));
    }
}
