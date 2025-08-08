package com.sakurafuld.hyperdaimc.content.over.materializer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.api.content.MaterializerRecipeEvent;
import com.sakurafuld.hyperdaimc.mixin.materializer.Ingredient$TagValueAccessor;
import com.sakurafuld.hyperdaimc.mixin.materializer.IngredientAccessor;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
import net.minecraftforge.common.MinecraftForge;
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
    private static final List<Process> PROCESSES = Lists.newArrayList();
    public static boolean reloaded = true;

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener((ResourceManagerReloadListener) pResourceManager -> reloaded = true);
    }

    public static List<ItemStack> materialize(Level level, ItemStack stack) {
        if (reloaded) {
            PROCESSES.clear();
            PROCESSES.addAll(loadRecipe(level));
            reloaded = false;
        }

        List<ItemStack> list = Lists.newArrayList();
        if (!stack.isEmpty()) {
            for (Process process : PROCESSES) {
                ItemStack result = process.result();
                if (result.is(stack.getItem()) && result.getCount() == stack.getCount() && (!result.hasTag() || (stack.hasTag() && NbtUtils.compareNbt(result.getTag(), stack.getTag(), true)))) {
                    list.addAll(process.ingredients());
                }
            }
        }

        return list;
    }

    public static List<Process> loadRecipe(Level level) {
        List<Process> processes = Lists.newArrayList();
        Set<Recipe<?>> searched = Sets.newHashSet();

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
                .forEach(searched::addAll);

        MinecraftForge.EVENT_BUS.post(new MaterializerRecipeEvent.Load(level, searched));

        searched.stream()
                .filter(recipe -> !HyperCommonConfig.MATERIALIZER_RECIPE_BLACKLIST.get().contains(recipe.getId().toString()))
                .forEach(recipe -> {
                    ItemStack result = recipe.getResultItem(level.registryAccess());
                    if (result.isEmpty()) {
                        return;
                    }

                    List<ItemStack> ingredients = Lists.newArrayList();
                    recipe.getIngredients().stream()
                            .filter(ingredient -> !ingredient.isEmpty())
                            .flatMap(ingredient -> Arrays.stream(((IngredientAccessor) ingredient).getValues()))
                            .filter(value -> !(value instanceof Ingredient.TagValue tag) || !HyperCommonConfig.MATERIALIZER_TAG_BLACKLIST.get().contains(((Ingredient$TagValueAccessor) tag).getTag().location().toString()))
                            .flatMap(value -> value.getItems().stream())
                            .map(ItemStack::copy)
                            .forEach(ingredient -> addOrStack(ingredients, ingredient));

                    MaterializerRecipeEvent.Add event = new MaterializerRecipeEvent.Add(level, recipe, ingredients);
                    MinecraftForge.EVENT_BUS.post(event);

                    if (!event.isCanceled()) {
                        for (Process process : processes) {
                            if (result.getCount() == process.result().getCount() && ItemHandlerHelper.canItemStacksStack(result, process.result())) {
                                ingredients.forEach(ingredient -> addOrStack(process.ingredients(), ingredient));
                                return;
                            }
                        }

                        if (!ingredients.isEmpty()) {
                            processes.add(new Process(result.copy(), ingredients));
                        }
                    }
                });

        return processes;
    }

    private static void addOrStack(List<ItemStack> list, ItemStack stack) {
        int index = -1;
        for (int at = 0; at < list.size(); at++) {
            if (ItemHandlerHelper.canItemStacksStack(list.get(at), stack)) {
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
                stack.setCount(1);
            }
            list.add(stack);
        } else {
            list.get(index).grow(stack.getCount());
        }
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

    public record Process(ItemStack result, List<ItemStack> ingredients) {
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
