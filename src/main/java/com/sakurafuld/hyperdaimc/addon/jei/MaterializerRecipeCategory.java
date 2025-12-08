package com.sakurafuld.hyperdaimc.addon.jei;

import com.google.common.collect.Sets;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.over.materializer.MaterializerHandler;
import com.sakurafuld.hyperdaimc.infrastructure.Writes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class MaterializerRecipeCategory extends AbstractRecipeCategory<MaterializerHandler.Process> {
    public static final RecipeType<MaterializerHandler.Process> TYPE = new RecipeType<>(identifier("materializer"), MaterializerHandler.Process.class);

    private final IDrawable background;
    private final IDrawableAnimated arrow;

    public MaterializerRecipeCategory(IGuiHelper helper) {
        super(TYPE, Component.translatable("recipe.hyperdaimc.materializer"), helper.createDrawableItemLike(HyperBlocks.MATERIALIZER.get()), 154, 53);
        ResourceLocation texture = identifier("textures/gui/container/materializer.png");
        this.background = helper.createDrawable(texture, 11, 16, 154, 53);
        this.arrow = helper.createAnimatedDrawable(helper.createDrawable(texture, 0, 166, 94, 9), 100, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(MaterializerHandler.Process recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
        int color = 0xFF << 24 | Writes.gameOver(0);
        guiGraphics.fill(1, 21, 153, 23, color);
        this.arrow.draw(guiGraphics, 35, 40);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MaterializerHandler.Process recipe, IFocusGroup focuses) {
        builder.addInputSlot(5, 32).addItemStack(recipe.result());
        IRecipeSlotBuilder result = builder.addOutputSlot(137, 32);
        result.addItemStacks(recipe.ingredients());
        IRecipeSlotBuilder fuel = builder.addSlot(RecipeIngredientRole.CATALYST, 80 - 11, 17 - 16);
        Set<Item> fuelItems = Sets.newHashSet();
        MaterializerHandler.Fuel.ITEMS.object2IntEntrySet().stream()
                .filter(entry -> ForgeRegistries.ITEMS.containsKey(entry.getKey()))
                .forEach(entry -> {
                    Item item = ForgeRegistries.ITEMS.getValue(entry.getKey());
                    fuel.addItemStack(new ItemStack(item)).addRichTooltipCallback((view, tooltip) -> view.getDisplayedItemStack().filter(stack -> stack.is(item)).ifPresent(stack ->
                            tooltip.add(Writes.gameOver(I18n.get("tooltip.hyperdaimc.materializer.fuel", entry.getIntValue())))));
                    fuelItems.add(item);
                });
        MaterializerHandler.Fuel.TAGS.forEach((tag, time) -> ForgeRegistries.ITEMS.tags().getTag(tag)
                .stream()
                .filter(item -> !fuelItems.contains(item))
                .forEach(item -> fuel.addItemStack(new ItemStack(item)).addRichTooltipCallback((view, tooltip) -> view.getDisplayedItemStack().filter(stack -> stack.is(item)).ifPresent(stack ->
                        tooltip.add(Writes.gameOver(I18n.get("tooltip.hyperdaimc.materializer.fuel", time)))))));
    }
}
