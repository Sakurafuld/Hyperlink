package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.sakurafuld.hyperdaimc.content.HyperRecipes;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public interface IDeskRecipe extends Recipe<RecipeWrapper> {
    default RecipeType<?> getType() {
        return HyperRecipes.DESK.get();
    }

    boolean isMinecraft();
}
