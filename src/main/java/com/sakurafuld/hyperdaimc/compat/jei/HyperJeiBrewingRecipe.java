package com.sakurafuld.hyperdaimc.compat.jei;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.IntSupplier;

public class HyperJeiBrewingRecipe implements IJeiBrewingRecipe {
    private final IJeiBrewingRecipe original;
    private final IntSupplier specialStep;

    public HyperJeiBrewingRecipe(IJeiBrewingRecipe original, IntSupplier specialStep) {
        this.original = original;
        this.specialStep = specialStep;
    }


    @Override
    public @Unmodifiable List<ItemStack> getPotionInputs() {
        return this.original.getPotionInputs();
    }

    @Override
    public @Unmodifiable List<ItemStack> getIngredients() {
        return this.original.getIngredients();
    }

    @Override
    public ItemStack getPotionOutput() {
        return this.original.getPotionOutput();
    }

    @Override
    public int getBrewingSteps() {
        return this.specialStep.getAsInt();
    }

    @Override
    public int hashCode() {
        return this.original.hashCode();
    }

    @Override
    public String toString() {
        return this.original.toString();
    }
}
