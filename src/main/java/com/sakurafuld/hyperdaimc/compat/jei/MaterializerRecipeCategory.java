package com.sakurafuld.hyperdaimc.compat.jei;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.over.materializer.MaterializerHandler;
import com.sakurafuld.hyperdaimc.helper.Writes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

public class MaterializerRecipeCategory implements IRecipeCategory<MaterializerRecipe> {
    public static final ResourceLocation ID = identifier("materializer");
    public static final RecipeType<MaterializerRecipe> TYPE = new RecipeType<>(ID, MaterializerRecipe.class);
    private static final Component TITLE = new TranslatableComponent("recipe.hyperdaimc.materializer");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public MaterializerRecipeCategory(IGuiHelper helper) {
        ResourceLocation materializer = identifier("textures/gui/container/materializer.png");
        this.background = helper.createDrawable(materializer, 11, 16, 154, 53);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(HyperBlocks.MATERIALIZER.get()));
        this.arrow = helper.createAnimatedDrawable(helper.createDrawable(materializer, 0, 166, 94, 9), 100, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getUid() {
        return ID;
    }

    @Override
    @SuppressWarnings("removal")
    public Class<? extends MaterializerRecipe> getRecipeClass() {
        return MaterializerRecipe.class;
    }

    @Override
    public RecipeType<MaterializerRecipe> getRecipeType() {
        return TYPE;
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(MaterializerRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        int color = (0xFF << 24) | Writes.gameOver("A").getSiblings().get(0).getStyle().getColor().getValue();
        GuiComponent.fill(stack, 1, 21, 153, 23, color);
        this.arrow.draw(stack, 35, 40);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MaterializerRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 32).addItemStack(recipe.catalyst());
        IRecipeSlotBuilder result = builder.addSlot(RecipeIngredientRole.OUTPUT, 137, 32);
        result.addIngredients(VanillaTypes.ITEM_STACK, recipe.results());
        IRecipeSlotBuilder fuel = builder.addSlot(RecipeIngredientRole.CATALYST, 80 - 11, 17 - 16);
        Set<Item> fuelItems = Sets.newHashSet();
        MaterializerHandler.Fuel.ITEMS.object2IntEntrySet().stream()
                .filter(entry -> ForgeRegistries.ITEMS.containsKey(entry.getKey()))
                .forEach(entry -> {
                    Item item = ForgeRegistries.ITEMS.getValue(entry.getKey());
                    fuel.addItemStack(new ItemStack(item)).addTooltipCallback((view, tooltip) -> view.getDisplayedItemStack().filter(stack -> stack.is(item)).ifPresent(stack ->
                            tooltip.add(Writes.gameOver(I18n.get("tooltip.hyperdaimc.materializer.fuel", entry.getIntValue())))));
                    fuelItems.add(item);
                });
        MaterializerHandler.Fuel.TAGS.forEach((tag, time) -> ForgeRegistries.ITEMS.tags().getTag(tag)
                .stream()
                .filter(item -> !fuelItems.contains(item))
                .forEach(item -> fuel.addItemStack(new ItemStack(item)).addTooltipCallback((view, tooltip) -> view.getDisplayedItemStack().filter(stack -> stack.is(item)).ifPresent(stack ->
                        tooltip.add(Writes.gameOver(I18n.get("tooltip.hyperdaimc.materializer.fuel", time)))))));
    }
}
