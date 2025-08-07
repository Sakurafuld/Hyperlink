package com.sakurafuld.hyperdaimc.api.content;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

@Cancelable
public class MaterializerRecipeLoadEvent extends Event {
    private final Level level;
    private final Recipe<?> recipe;
    private final List<ItemStack> ingredients;

    public MaterializerRecipeLoadEvent(Level level, Recipe<?> recipe, List<ItemStack> ingredients) {
        this.level = level;
        this.recipe = recipe;
        this.ingredients = ingredients;
    }

    public Level getLevel() {
        return this.level;
    }

    public Recipe<?> getRecipe() {
        return this.recipe;
    }

    public List<ItemStack> getIngredients() {
        return this.ingredients;
    }
}
