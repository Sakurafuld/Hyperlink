package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.sakurafuld.hyperdaimc.content.HyperRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;

public interface IDeskRecipe extends Recipe<RecipeWrapper> {
    default RecipeType<?> getType() {
        return HyperRecipes.DESK.get();
    }

    boolean isMinecraft();

    boolean showToJei();

    @Override
    ItemStack getResultItem(@Nullable RegistryAccess pRegistryAccess);
}
