package com.sakurafuld.hyperdaimc.compat.jei;

import com.google.common.collect.Sets;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

public class MaterializerRecipeCategory implements IRecipeCategory<MaterializerHandler.Process> {
    public static final ResourceLocation ID = identifier("materializer");
    public static final RecipeType<MaterializerHandler.Process> TYPE = new RecipeType<>(ID, MaterializerHandler.Process.class);
    private static final Component TITLE = Component.translatable("recipe.hyperdaimc.materializer");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public MaterializerRecipeCategory(IGuiHelper helper) {
        ResourceLocation materializer = identifier("textures/gui/container/materializer.png");
        this.background = helper.createDrawable(materializer, 11, 16, 154, 53);
        this.icon = helper.createDrawableItemLike(HyperBlocks.MATERIALIZER.get());
        this.arrow = helper.createAnimatedDrawable(helper.createDrawable(materializer, 0, 166, 94, 9), 100, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public RecipeType<MaterializerHandler.Process> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

    @Override
    @SuppressWarnings("removal")
    public @Nullable IDrawable getBackground() {
        return this.background;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(MaterializerHandler.Process recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        int color = (0xFF << 24) | Writes.gameOver("A").getSiblings().get(0).getStyle().getColor().getValue();
        guiGraphics.fill(1, 21, 153, 23, color);
        this.arrow.draw(guiGraphics, 35, 40);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MaterializerHandler.Process recipe, IFocusGroup focuses) {
        builder.addInputSlot(5, 32).addItemStack(recipe.result());
        IRecipeSlotBuilder result = builder.addOutputSlot(137, 32);
        result.addIngredients(VanillaTypes.ITEM_STACK, recipe.ingredients());
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
