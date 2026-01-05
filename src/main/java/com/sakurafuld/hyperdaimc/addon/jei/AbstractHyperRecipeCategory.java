package com.sakurafuld.hyperdaimc.addon.jei;

import com.sakurafuld.hyperdaimc.infrastructure.Writes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;

public abstract class AbstractHyperRecipeCategory<T> implements IRecipeCategory<T> {
    private final RecipeType<T> recipeType;
    private final Component title;
    private final IDrawable icon;
    private final int width;
    private final int height;

    public AbstractHyperRecipeCategory(RecipeType<T> recipeType, Component title, IDrawable icon, int width, int height) {
        this.recipeType = recipeType;
        this.title = title;
        this.icon = icon;
        this.width = width;
        this.height = height;
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return this.recipeType;
    }

    @Override
    public Component getTitle() {
        return Writes.gameOver(this.title.getString());
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }
}
