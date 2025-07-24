package com.sakurafuld.hyperdaimc.compat.jei;

import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperRecipes;
import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskMenu;
import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskScreen;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXScreen;
import com.sakurafuld.hyperdaimc.helper.Calculates;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.registration.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

@JeiPlugin
public class HyperJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = identifier(HYPERDAIMC, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerRecipes(IRecipeRegistration registration) {
//        List<ItemStack> materials = HyperItems.MATERIAL.values().stream()
//                .map(RegistryObject::get)
//                .map(Item::getDefaultInstance)
//                .toList();
//        registration.addIngredientInfo(materials, VanillaTypes.ITEM_STACK, new TranslatableComponent("information.hyperdaimc.material.0"));
        List<ItemStack> fumetsuWither = Stream.of(HyperItems.GOD_SIGIL.get(), HyperBlocks.SOUL.get().asItem(), HyperBlocks.FUMETSU_LEFT.get().asItem(), HyperBlocks.FUMETSU_SKULL.get().asItem(), HyperBlocks.FUMETSU_RIGHT.get().asItem(), HyperItems.BUG_STARS.get(0).get())
                .map(Item::getDefaultInstance)
                .toList();
        List<String> fumetsuWitherParameter = fumetsuWither.stream()
                .map(stack -> I18n.get(stack.getDescriptionId()))
                .collect(Collectors.toList());
        fumetsuWitherParameter.add(HyperEntities.FUMETSU.get().getDescription().getString());
        registration.addIngredientInfo(fumetsuWither, VanillaTypes.ITEM_STACK, new TranslatableComponent("information.hyperdaimc.fumetsu_wither.0", fumetsuWitherParameter.toArray(Object[]::new)));

        registration.addRecipes(DeskRecipeCategory.TYPE, Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(HyperRecipes.DESK.get()));

        IJeiBrewingRecipe chemicalMAX = registration.getVanillaRecipeFactory().createBrewingRecipe(
                List.of(HyperItems.GOD_SIGIL.get().getDefaultInstance()),
                Stream.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION)
                        .flatMap(bottle -> StreamSupport.stream(ForgeRegistries.POTIONS.spliterator(), false)
                                .filter(potion -> potion != Potions.EMPTY)
                                .map(potion -> PotionUtils.setPotion(new ItemStack(bottle), potion)))
                        .toList(),
                HyperItems.CHEMICAL_MAX.get().getDefaultInstance());

        IJeiBrewingRecipe fumetsuSkull = registration.getVanillaRecipeFactory().createBrewingRecipe(List.of(Items.NETHER_STAR.getDefaultInstance()), HyperItems.CHEMICAL_MAX.get().getDefaultInstance(), HyperBlocks.FUMETSU_SKULL.get().asItem().getDefaultInstance());
        registration.addRecipes(RecipeTypes.BREWING, List.of(new HyperJeiBrewingRecipe(chemicalMAX, () -> Integer.MAX_VALUE), new HyperJeiBrewingRecipe(fumetsuSkull, new IntSupplier() {
            private final Random RANDOM = new Random();
            private long lastTime = 0;
            private int lastStep = 0;

            @Override
            public int getAsInt() {
                if (Util.getMillis() - this.lastTime > 50) {
                    this.lastTime = Util.getMillis();
                    double delta = Math.min(1, ((Math.cos(this.lastTime / 800d) + 1) / 2d));
                    delta = Calculates.curve(delta, 0, 0.00001, 0.0002, 1);
                    int inaccuracy = (int) (delta * 50000000);
                    this.lastStep = inaccuracy - RANDOM.nextInt(inaccuracy / 2 + 1);
                }
                return this.lastStep;
            }
        })));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new DeskRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(HyperBlocks.DESK.get()), DeskRecipeCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(DeskMenu.class, DeskRecipeCategory.TYPE, 1, 81, 82, 36);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(DeskScreen.class, 56, 186, 14, 15, DeskRecipeCategory.TYPE);
        registration.addGhostIngredientHandler(VRXScreen.class, new VRXGhostIngredientHandler());
    }
}
