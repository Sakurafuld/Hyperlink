package com.sakurafuld.hyperdaimc.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
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
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

public class DeskRecipeCategory implements IRecipeCategory<IDeskRecipe> {
    public static final ResourceLocation ID = identifier("desk");
    public static final RecipeType<IDeskRecipe> TYPE = new RecipeType<>(ID, IDeskRecipe.class);
    private static final Component TITLE = new TranslatableComponent("recipe.hyperdaimc.desk");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable minecraftOff;
    private final IDrawable pickaxeOn;
    private final IDrawable blockOn;

    public DeskRecipeCategory(IGuiHelper helper) {
        ResourceLocation desk = identifier("textures/gui/container/desk.png");
        this.background = helper.drawableBuilder(desk, 7, 17, 162, 192).setTextureSize(512, 512).build();
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(HyperBlocks.DESK.get()));
        this.minecraftOff = helper.drawableBuilder(desk, 176, 0, 15, 12).setTextureSize(512, 512).build();
        this.pickaxeOn = helper.drawableBuilder(desk, 176, 12, 8, 8).setTextureSize(512, 512).build();
        this.blockOn = helper.drawableBuilder(desk, 176, 20, 10, 10).setTextureSize(512, 512).build();
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
    public Class<? extends IDeskRecipe> getRecipeClass() {
        return IDeskRecipe.class;
    }

    @Override
    public RecipeType<IDeskRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(IDeskRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        if (!recipe.isMinecraft()) {
            this.minecraftOff.draw(stack, 99, 172);
        } else {
            this.pickaxeOn.draw(stack, 106, 172);
            this.blockOn.draw(stack, 99, 174);
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IDeskRecipe recipe, IFocusGroup focuses) {
        builder.moveRecipeTransferButton(this.background.getWidth() - 13, 163);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 73, 171).addItemStack(recipe.getResultItem());

        IRecipeSlotBuilder[][] slots = new IRecipeSlotBuilder[9][9];

        int index = 0;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                slots[x][y] = builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 1, y * 18 + 1);
                index++;
            }
        }
        if (recipe instanceof DeskShapedRecipe shaped) {
            int dy = 0;
            if (shaped.getRecipeHeight() % 2 == 1) {
                dy = (9 - shaped.getRecipeHeight()) / 2;
            }
            int dx = 0;
            if (shaped.getRecipeWidth() % 2 == 1) {
                dx = (9 - shaped.getRecipeWidth()) / 2;
            }

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
            for (int y = 0; y < 9; ++y) {
                for (int x = 0; x < 9; ++x) {
                    if (index < recipe.getIngredients().size()) {
                        slots[x][y].addIngredients(recipe.getIngredients().get(index));
                    }
                    index++;
                }
            }
        }
    }
}
