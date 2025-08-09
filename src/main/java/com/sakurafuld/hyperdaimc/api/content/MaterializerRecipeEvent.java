package com.sakurafuld.hyperdaimc.api.content;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
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

        public void addSimple(ItemStack result, List<ItemStack> ingredients) {
            this.getSearched().add(new Simple(result, ingredients.stream()
                    .map(Ingredient::of)
                    .collect(NonNullList::create, NonNullList::add, NonNullList::addAll)));
        }

        public static class Simple implements Recipe<Container> {
            private final ItemStack result;
            private final NonNullList<Ingredient> ingredients;

            public Simple(ItemStack result, NonNullList<Ingredient> ingredients) {
                this.result = result;
                this.ingredients = ingredients;
            }

            @Override
            public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
                return this.result;
            }

            @Override
            public NonNullList<Ingredient> getIngredients() {
                return this.ingredients;
            }

            @Override
            public boolean matches(Container pContainer, Level pLevel) {
                return false;
            }

            @Override
            public ItemStack assemble(Container pContainer, RegistryAccess pRegistryAccess) {
                return null;
            }

            @Override
            public boolean canCraftInDimensions(int pWidth, int pHeight) {
                return false;
            }

            @Override
            public ResourceLocation getId() {
                return null;
            }

            @Override
            public RecipeSerializer<?> getSerializer() {
                return null;
            }

            @Override
            public RecipeType<?> getType() {
                return null;
            }
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
