package com.sakurafuld.hyperdaimc.addon.jei;

import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskShapedRecipe;
import com.sakurafuld.hyperdaimc.content.crafting.desk.IDeskRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class DeskRecipeCategory extends AbstractRecipeCategory<IDeskRecipe> {
    public static final RecipeType<IDeskRecipe> TYPE = new RecipeType<>(identifier("desk"), IDeskRecipe.class);

    private final IDrawable background;
    private final IDrawable minecraftOff;
    private final IDrawable pickaxeOn;
    private final IDrawable blockOn;

    public DeskRecipeCategory(IGuiHelper helper) {
        super(TYPE, Component.translatable("recipe.hyperdaimc.desk"), helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(HyperBlocks.DESK.get())), 162, 192);
        ResourceLocation texture = identifier("textures/gui/container/desk.png");
        this.background = helper.drawableBuilder(texture, 7, 17, 162, 192).setTextureSize(512, 512).build();
        this.minecraftOff = helper.drawableBuilder(texture, 176, 0, 15, 12).setTextureSize(512, 512).build();
        this.pickaxeOn = helper.drawableBuilder(texture, 176, 12, 8, 8).setTextureSize(512, 512).build();
        this.blockOn = helper.drawableBuilder(texture, 176, 20, 10, 10).setTextureSize(512, 512).build();
    }

    @Override
    public boolean isHandled(IDeskRecipe recipe) {
        return recipe.showToJei();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(IDeskRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics);
        if (!recipe.isMinecraft())
            this.minecraftOff.draw(guiGraphics, 99, 172);
        else {
            this.pickaxeOn.draw(guiGraphics, 106, 172);
            this.blockOn.draw(guiGraphics, 99, 174);
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IDeskRecipe recipe, IFocusGroup focuses) {
        builder.moveRecipeTransferButton(this.background.getWidth() - 13, 163 + 13 + 2);
        builder.addOutputSlot(73, 171)
                .addItemStack(recipe.getResultItem(null));

        IRecipeSlotBuilder[][] slots = new IRecipeSlotBuilder[9][9];

        int index = 0;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                slots[x][y] = builder.addInputSlot(x * 18 + 1, y * 18 + 1);
                index++;
            }
        }
        if (recipe instanceof DeskShapedRecipe shaped) {
            int dy = 0;
            if (shaped.getRecipeHeight() % 2 == 1)
                dy = (9 - shaped.getRecipeHeight()) / 2;
            int dx = 0;
            if (shaped.getRecipeWidth() % 2 == 1)
                dx = (9 - shaped.getRecipeWidth()) / 2;

            index = 0;
            for (int y = 0; y < shaped.getRecipeHeight(); ++y) {
                for (int x = 0; x < shaped.getRecipeWidth(); ++x) {
                    slots[x + dx][y + dy].addIngredients(shaped.getIngredients().get(index));
                    index++;
                }
            }
        } else {
            builder.setShapeless(0, 163);

            index = 0;
            loop:
            for (int y = 0; y < 9; ++y) {
                for (int x = 0; x < 9; ++x) {
                    if (index >= recipe.getIngredients().size())
                        break loop;
                    slots[x][y].addIngredients(recipe.getIngredients().get(index));
                    index++;
                }
            }
        }
    }
}
