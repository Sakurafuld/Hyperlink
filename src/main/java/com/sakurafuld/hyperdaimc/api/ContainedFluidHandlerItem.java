package com.sakurafuld.hyperdaimc.api;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class ContainedFluidHandlerItem implements IFluidHandlerItem {
    private final IFluidHandlerItem handler;
    private final IItemHandlerModifiable container;
    private final int slot;

    public ContainedFluidHandlerItem(IFluidHandlerItem handler, IItemHandlerModifiable container, int slot) {
        this.handler = handler;
        this.container = container;
        this.slot = slot;
    }

    @NotNull
    @Override
    public ItemStack getContainer() {
        return this.handler.getContainer();
    }

    @Override
    public int getTanks() {
        return this.handler.getTanks();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return this.handler.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.handler.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.handler.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int filled = this.handler.fill(resource, action);
        if (action.execute()) {
            this.container.setStackInSlot(this.slot, this.getContainer());
        }
        return filled;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        FluidStack drained = this.handler.drain(resource, action);
        if (action.execute()) {
            this.container.setStackInSlot(this.slot, this.getContainer());
        }
        return drained;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack drained = this.handler.drain(maxDrain, action);
        if (action.execute()) {
            this.container.setStackInSlot(this.slot, this.getContainer());
        }
        return drained;
    }
}
