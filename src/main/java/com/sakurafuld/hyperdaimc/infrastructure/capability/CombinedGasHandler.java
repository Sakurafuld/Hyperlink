package com.sakurafuld.hyperdaimc.infrastructure.capability;

import mekanism.api.Action;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CombinedGasHandler implements IGasHandler {
    private final IGasHandler[] handlers;
    private final int[] indexes;
    private final int totalTanks;

    public CombinedGasHandler(IGasHandler... handlers) {
        this.handlers = handlers;
        this.indexes = new int[handlers.length];
        int totalIndex = 0;
        for (int index = 0; index < handlers.length; index++) {
            totalIndex += handlers[index].getTanks();
            this.indexes[index] = totalIndex;
        }
        this.totalTanks = totalIndex;
    }

    protected <T> T execute(int tank, BiFunction<IGasHandler, Integer, T> function, Supplier<T> fail) {
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

    protected void execute(int tank, BiConsumer<IGasHandler, Integer> consumer) {
        this.execute(tank, (handler, index) -> {
            consumer.accept(handler, index);
            return null;
        }, () -> null);
    }

    @Override
    public int getTanks() {
        return this.totalTanks;
    }

    @NotNull
    @Override
    public GasStack getChemicalInTank(int tank) {
        return this.execute(tank, IGasHandler::getChemicalInTank, () -> GasStack.EMPTY);
    }

    @Override
    public void setChemicalInTank(int tank, GasStack stack) {
        this.execute(tank, (handler, index) -> handler.setChemicalInTank(index, stack));
    }

    @Override
    public long getTankCapacity(int tank) {
        return this.execute(tank, IChemicalHandler::getTankCapacity, () -> 0L);
    }

    @Override
    public boolean isValid(int tank, @NotNull GasStack stack) {
        return this.execute(tank, (handler, index) -> handler.isValid(index, stack), () -> false);
    }

    @Override
    public @NotNull GasStack insertChemical(int tank, GasStack stack, Action action) {
        return this.execute(tank, (handler, index) -> handler.insertChemical(index, stack, action), () -> stack);
    }

    @Override
    public @NotNull GasStack extractChemical(int tank, long amount, Action action) {
        return this.execute(tank, (handler, index) -> handler.extractChemical(index, amount, action), () -> GasStack.EMPTY);
    }
}
