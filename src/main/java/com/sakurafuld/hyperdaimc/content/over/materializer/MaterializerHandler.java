package com.sakurafuld.hyperdaimc.content.over.materializer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.mixin.materializer.Ingredient$TagValueAccessor;
import com.sakurafuld.hyperdaimc.mixin.materializer.IngredientAccessor;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class MaterializerHandler {
    private static final Object2ObjectOpenHashMap<ItemStack, List<Ingredient.Value>> RECIPES = new Object2ObjectOpenHashMap<>();
    public static boolean reloaded = true;

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener((ResourceManagerReloadListener) pResourceManager -> reloaded = true);
    }

    public static List<ItemStack> materialize(Level level, ItemStack stack) {
        if (reloaded) {
            loadRecipe(level);
            reloaded = false;
        }

        List<ItemStack> list = Lists.newArrayList();
        if (!stack.isEmpty()) {
            for (Object2ObjectMap.Entry<ItemStack, List<Ingredient.Value>> entry : RECIPES.object2ObjectEntrySet()) {
                ItemStack result = entry.getKey();
                if (result.is(stack.getItem()) && result.getCount() == stack.getCount() && (!result.hasTag() || (stack.hasTag() && NbtUtils.compareNbt(result.getTag(), stack.getTag(), true)))) {
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
                }
            }
        }

        return list;
    }

    public static void loadRecipe(Level level) {
        RECIPES.clear();
        Set<Recipe<?>> searching = Sets.newHashSet();
        class Caster {
            @SuppressWarnings("unchecked")
            static <T extends Recipe<?>> RecipeType<T> cast(RecipeType<?> type) {
                return (RecipeType<T>) type;
            }
        }
        HyperCommonConfig.MATERIALIZER_RECIPE.get().stream()
                .map(String.class::cast)
                .map(ResourceLocation::parse)
                .filter(ForgeRegistries.RECIPE_TYPES::containsKey)
                .map(ForgeRegistries.RECIPE_TYPES::getValue)
                .map(type -> level.getRecipeManager().getAllRecipesFor(Caster.cast(type)))
                .forEach(searching::addAll);

        searching.stream()
                .filter(recipe -> !HyperCommonConfig.MATERIALIZER_RECIPE_BLACKLIST.get().contains(recipe.getId().toString()))
                .forEach(recipe -> {
                    ItemStack result = recipe.getResultItem(level.registryAccess());
                    List<Ingredient.Value> ingredients = Lists.newArrayList();
                    recipe.getIngredients().stream()
                            .filter(ingredient -> !ingredient.isEmpty())
                            .flatMap(ingredient -> Arrays.stream(((IngredientAccessor) ingredient).getValues()))
                            .forEach(ingredients::add);

                    RECIPES.put(result, ImmutableList.copyOf(ingredients));
                });
    }

    public static int getFuel(Item item) {
        ResourceLocation identifier = ForgeRegistries.ITEMS.getKey(item);
        if (Fuel.ITEMS.containsKey(identifier)) {
            return Fuel.ITEMS.getInt(identifier);
        } else {
            for (Object2IntMap.Entry<TagKey<Item>> entry : Fuel.TAGS.object2IntEntrySet()) {
                if (item.builtInRegistryHolder().is(entry.getKey())) {
                    return entry.getIntValue();
                }
            }

            return 0;
        }
    }

    @Mod.EventBusSubscriber(modid = HYPERDAIMC, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Fuel {
        public static final Object2IntOpenHashMap<ResourceLocation> ITEMS = new Object2IntOpenHashMap<>();
        public static final Object2IntOpenHashMap<TagKey<Item>> TAGS = new Object2IntOpenHashMap<>();

        @SubscribeEvent
        public static void config(ModConfigEvent event) {
            if (event.getConfig().getModId().equals(HYPERDAIMC) && event.getConfig().getType() == ModConfig.Type.COMMON) {
                ITEMS.clear();
                TAGS.clear();
                HyperCommonConfig.MATERIALIZER_FUEL.get().stream()
                        .map(String.class::cast)
                        .forEach(string -> {
                            boolean tag = string.startsWith("#");
                            if (tag) {
                                string = string.substring(1);
                            }

                            String[] split = string.split("=");
                            ResourceLocation identifier = ResourceLocation.parse(split[0]);
                            int value = Integer.parseInt(split[1]);
                            if (tag) {
                                TAGS.put(ItemTags.create(identifier), value);
                            } else {
                                ITEMS.put(identifier, value);
                            }
                        });
            }
        }
    }
}
