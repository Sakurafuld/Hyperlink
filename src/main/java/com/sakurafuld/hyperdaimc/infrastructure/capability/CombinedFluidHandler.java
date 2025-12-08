package com.sakurafuld.hyperdaimc.infrastructure.capability;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CombinedFluidHandler implements IFluidHandler {
    private final IFluidHandler[] handlers;
    private final int[] indexes;
    private final int totalTanks;

    public CombinedFluidHandler(IFluidHandler... handlers) {
        this.handlers = handlers;
        this.indexes = new int[handlers.length];
        int totalIndex = 0;
        for (int index = 0; index < handlers.length; index++) {
            totalIndex += handlers[index].getTanks();
            this.indexes[index] = totalIndex;
        }
        this.totalTanks = totalIndex;
    }

    protected <T> T execute(int tank, BiFunction<IFluidHandler, Integer, T> function, Supplier<T> fail) {
        if (tank >= 0) {
            for (int index = 0; index < this.indexes.length; index++) {
                if (tank - this.indexes[index] < 0) {
                    if (index == 0)
                        return function.apply(this.handlers[index], tank);
                    else
                        return function.apply(this.handlers[index], tank - this.indexes[index - 1]);
                }
            }
        }
        return fail.get();
    }

    @Override
    public int getTanks() {
        return this.totalTanks;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return this.execute(tank, IFluidHandler::getFluidInTank, () -> FluidStack.EMPTY);
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.execute(tank, IFluidHandler::getTankCapacity, () -> 0);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.execute(tank, (handler, index) -> handler.isFluidValid(index, stack), () -> false);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int amount = 0;
        for (IFluidHandler handler : this.handlers) {
            if (resource.isEmpty()) break;

            int filled = handler.fill(resource.copy(), action);
            amount += filled;
            resource.shrink(filled);
        }
        return amount;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        int amount = 0;
        for (IFluidHandler handler : this.handlers) {
            if (resource.isEmpty()) break;

            int drained = handler.drain(resource.copy(), action).getAmount();
            amount += drained;
            resource.shrink(drained);
        }
        resource.setAmount(amount);
        return resource.isEmpty() ? FluidStack.EMPTY : resource;
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack stack = FluidStack.EMPTY;
        int remaining = maxDrain;
        for (IFluidHandler handler : this.handlers) {
            if (remaining <= 0) break;

            FluidStack drained = handler.drain(remaining, FluidAction.SIMULATE);
            if (!drained.isEmpty() && (stack.isEmpty() || stack.equals(drained))) {
                stack = (action.simulate() ? drained : handler.drain(maxDrain, action)).copy();
                remaining -= stack.getAmount();
            }
        }
        if (stack.isEmpty() || remaining == maxDrain)
            return FluidStack.EMPTY;
        else {
            stack.setAmount(maxDrain - remaining);
            return stack;
        }
    }
}
