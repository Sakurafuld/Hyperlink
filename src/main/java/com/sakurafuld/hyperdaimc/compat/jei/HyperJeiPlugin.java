package com.sakurafuld.hyperdaimc.compat.jei;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperRecipes;
import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskMenu;
import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskScreen;
import com.sakurafuld.hyperdaimc.content.crafting.desk.IDeskRecipe;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXScreen;
import com.sakurafuld.hyperdaimc.content.over.materializer.MaterializerMenu;
import com.sakurafuld.hyperdaimc.content.over.materializer.MaterializerScreen;
import com.sakurafuld.hyperdaimc.helper.Calculates;
import com.sakurafuld.hyperdaimc.mixin.materializer.Ingredient$TagValueAccessor;
import com.sakurafuld.hyperdaimc.mixin.materializer.IngredientAccessor;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.registration.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

@JeiPlugin
public class HyperJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = identifier("jei");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerRecipes(IRecipeRegistration registration) {
        this.addFumetsuWither(registration);
        this.addBrewing(registration);

        registration.addRecipes(DeskRecipeCategory.TYPE, Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(HyperRecipes.DESK.get()).stream()
                .filter(IDeskRecipe::showToJei)
                .toList());
        registration.addRecipes(MaterializerRecipeCategory.TYPE, this.getMaterializerRecipes());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new DeskRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MaterializerRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(HyperBlocks.DESK.get()), DeskRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(HyperBlocks.MATERIALIZER.get()), MaterializerRecipeCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(DeskMenu.class, DeskRecipeCategory.TYPE, 1, 81, 82, 36);
        registration.addRecipeTransferHandler(MaterializerMenu.class, MaterializerRecipeCategory.TYPE, 0, 1, 3, 36);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(DeskScreen.class, 56, 186, 14, 15, DeskRecipeCategory.TYPE);
        registration.addRecipeClickArea(MaterializerScreen.class, 46, 56, 94, 9, MaterializerRecipeCategory.TYPE);

        registration.addGhostIngredientHandler(VRXScreen.class, new VRXGhostIngredientHandler());
    }

    private void addBrewing(IRecipeRegistration registration) {
        if (!HyperCommonConfig.FUMETSU_RECIPE.get()) {
            return;
        }
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

    private void addFumetsuWither(IRecipeRegistration registration) {
        if (!HyperCommonConfig.FUMETSU_SUMMON.get()) {
            return;
        }
        List<ItemStack> fumetsuWither = Stream.of(HyperItems.GOD_SIGIL.get(), HyperBlocks.SOUL.get().asItem(), HyperBlocks.FUMETSU_LEFT.get().asItem(), HyperBlocks.FUMETSU_SKULL.get().asItem(), HyperBlocks.FUMETSU_RIGHT.get().asItem(), HyperItems.BUG_STARS.get(0).get())
                .map(Item::getDefaultInstance)
                .toList();
        List<String> fumetsuWitherParameter = fumetsuWither.stream()
                .map(stack -> I18n.get(stack.getDescriptionId()))
                .collect(Collectors.toList());
        fumetsuWitherParameter.add(HyperEntities.FUMETSU.get().getDescription().getString());
        registration.addIngredientInfo(fumetsuWither, VanillaTypes.ITEM_STACK, new TranslatableComponent("information.hyperdaimc.fumetsu_wither.0", fumetsuWitherParameter.toArray(Object[]::new)));
    }

    @OnlyIn(Dist.CLIENT)
    private List<MaterializerRecipe> getMaterializerRecipes() {
        Set<Recipe<?>> searching = Sets.newHashSet();
        Object2ObjectOpenHashMap<ItemStack, List<Ingredient.Value>> recipes = new Object2ObjectOpenHashMap<>();
        class Caster {
            @SuppressWarnings("unchecked")
            static <T extends Recipe<?>> RecipeType<T> cast(RecipeType<?> type) {
                return (RecipeType<T>) type;
            }
        }
        HyperCommonConfig.MATERIALIZER_RECIPE.get().stream()
                .map(String.class::cast)
                .map(ResourceLocation::new)
                .filter(Registry.RECIPE_TYPE::containsKey)
                .map(Registry.RECIPE_TYPE::get)
                .map(type -> Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(Caster.cast(type)))
                .forEach(searching::addAll);

        searching.stream()
                .filter(recipe -> !HyperCommonConfig.MATERIALIZER_RECIPE_BLACKLIST.get().contains(recipe.getId().toString()))
                .forEach(recipe -> {
                    ItemStack result = recipe.getResultItem();
                    if (result.isEmpty()) {
                        return;
                    }
                    List<Ingredient.Value> ingredients = Lists.newArrayList();
                    recipe.getIngredients().stream()
                            .filter(ingredient -> !ingredient.isEmpty())
                            .flatMap(ingredient -> Arrays.stream(((IngredientAccessor) ingredient).getValues()))
                            .forEach(ingredients::add);

                    for (Object2ObjectMap.Entry<ItemStack, List<Ingredient.Value>> entry : recipes.object2ObjectEntrySet()) {
                        if (result.getCount() == entry.getKey().getCount() && ItemHandlerHelper.canItemStacksStack(result, entry.getKey())) {
                            entry.getValue().addAll(ingredients);
                            return;
                        }
                    }

                    recipes.put(result, ingredients);
                });

        List<MaterializerRecipe> materializerRecipes = Lists.newArrayList();
        for (Object2ObjectMap.Entry<ItemStack, List<Ingredient.Value>> entry : recipes.object2ObjectEntrySet()) {
            List<ItemStack> list = Lists.newArrayList();
            entry.getValue().stream()
                    .filter(value -> !(value instanceof Ingredient.TagValue tagValue) || !HyperCommonConfig.MATERIALIZER_TAG_BLACKLIST.get().contains(((Ingredient$TagValueAccessor) tagValue).getTag().location().toString()))
                    .flatMap(value -> value.getItems().stream())
                    .map(ItemStack::copy)
                    .forEach(ingredient -> {
                        int index = -1;
                        for (int at = 0; at < list.size(); at++) {
                            if (ItemHandlerHelper.canItemStacksStack(list.get(at), ingredient)) {
                                if (!HyperCommonConfig.MATERIALIZER_STACK_INGREDIENTS.get()) {
                                    return;
                                } else {
                                    index = at;
                                    break;
                                }
                            }
                        }

                        if (index < 0) {
                            if (!HyperCommonConfig.MATERIALIZER_STACK_INGREDIENTS.get()) {
                                ingredient.setCount(1);
                            }
                            list.add(ingredient);
                        } else {
                            list.get(index).grow(ingredient.getCount());
                        }
                    });

            if (!list.isEmpty()) {
                materializerRecipes.add(new MaterializerRecipe(entry.getKey(), list));
            }
        }

        return materializerRecipes;
    }
}
