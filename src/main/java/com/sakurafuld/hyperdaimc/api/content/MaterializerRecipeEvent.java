package com.sakurafuld.hyperdaimc.api.content;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;
import java.util.Set;

public class MaterializerRecipeEvent extends Event {
    private final Level level;

    public MaterializerRecipeEvent(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return this.level;
    }

    public static class Load extends MaterializerRecipeEvent {
        private final Set<Recipe<?>> searched;

        public Load(Level level, Set<Recipe<?>> searched) {
            super(level);
            this.searched = searched;
        }

        public Set<Recipe<?>> getSearched() {
            return this.searched;
        }
    }

    @Cancelable
    public static class Add extends MaterializerRecipeEvent {
        private final Recipe<?> recipe;
        private final List<ItemStack> ingredients;

        public Add(Level level, Recipe<?> recipe, List<ItemStack> ingredients) {
            super(level);
            this.recipe = recipe;
            this.ingredients = ingredients;
        }

        public Recipe<?> getRecipe() {
            return this.recipe;
        }

        public List<ItemStack> getIngredients() {
            return this.ingredients;
        }
    }
}
