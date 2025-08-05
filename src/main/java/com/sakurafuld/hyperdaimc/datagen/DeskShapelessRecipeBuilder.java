package com.sakurafuld.hyperdaimc.datagen;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperRecipes;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

public class DeskShapelessRecipeBuilder implements RecipeBuilder {
    private boolean minecraft = false;
    private final ItemLike result;
    private final int count;
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final List<String> values = Lists.newArrayList();
    private final List<String> exclusion = Lists.newArrayList();

    public DeskShapelessRecipeBuilder(ItemLike result, int count) {
        this.result = result;
        this.count = count;
    }

    public DeskShapelessRecipeBuilder(ItemLike result) {
        this(result, 1);
    }

    public static DeskShapelessRecipeBuilder essence(String name) {
        return new DeskShapelessRecipeBuilder(HyperItems.getEssence(name))
                .values(HyperItems.BUG_STARS.get(0).get())
                .values(ItemTags.create(identifier("essence/" + name)));
    }

    public DeskShapelessRecipeBuilder minecraft() {
        this.minecraft = true;
        return this;
    }

    public DeskShapelessRecipeBuilder ingredients(Ingredient... ingredients) {
        this.ingredients.addAll(List.of(ingredients));
        return this;
    }

    public DeskShapelessRecipeBuilder values(Item... values) {
        this.values.addAll(Arrays.stream(values).map(item -> item.getRegistryName().toString()).toList());
        return this;
    }

    @SafeVarargs
    public final DeskShapelessRecipeBuilder values(TagKey<Item>... values) {
        this.values.addAll(Arrays.stream(values).map(tag -> "#" + tag.location()).toList());
        return this;
    }

    public DeskShapelessRecipeBuilder exclusion(Item... exclusion) {
        this.exclusion.addAll(Arrays.stream(exclusion).map(item -> item.getRegistryName().toString()).toList());
        return this;
    }

    @SafeVarargs
    public final DeskShapelessRecipeBuilder exclusion(TagKey<Item>... exclusion) {
        this.exclusion.addAll(Arrays.stream(exclusion).map(tag -> "#" + tag.location()).toList());
        return this;
    }

    @Override
    public DeskShapelessRecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
        return this;
    }

    @Override
    public DeskShapelessRecipeBuilder group(@Nullable String pGroupName) {
        return this;
    }

    @Override
    public Item getResult() {
        return this.result.asItem();
    }

    @Override
    public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId) {
        pFinishedRecipeConsumer.accept(new Finisher(pRecipeId));
    }

    public void saveMaterial(Consumer<FinishedRecipe> finisher) {
        String material = this.result.asItem().getRegistryName().getPath();
        int separator = material.lastIndexOf('_');
        String base = material.substring(0, separator);
        String suffix = material.substring(separator + 1);
        this.save(finisher, identifier("material/" + suffix + "/" + base));
    }

    class Finisher implements FinishedRecipe {
        private final ResourceLocation id;

        Finisher(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public void serializeRecipeData(JsonObject pJson) {
            if (DeskShapelessRecipeBuilder.this.minecraft) {
                pJson.addProperty("minecraft", true);
            }
            if (!DeskShapelessRecipeBuilder.this.ingredients.isEmpty()) {
                JsonArray ingredients = new JsonArray();

                for (Ingredient ingredient : DeskShapelessRecipeBuilder.this.ingredients) {
                    ingredients.add(ingredient.toJson());
                }

                pJson.add("ingredients", ingredients);
            }

            if (!DeskShapelessRecipeBuilder.this.values.isEmpty()) {
                JsonArray values = new JsonArray();

                for (String value : DeskShapelessRecipeBuilder.this.values) {
                    values.add(value);
                }

                pJson.add("values", values);
            }

            if (!DeskShapelessRecipeBuilder.this.exclusion.isEmpty()) {
                JsonArray exclusion = new JsonArray();

                for (String value : DeskShapelessRecipeBuilder.this.exclusion) {
                    exclusion.add(value);
                }

                pJson.add("exclusion", exclusion);
            }

            JsonObject result = new JsonObject();

            result.addProperty("item", DeskShapelessRecipeBuilder.this.getResult().getRegistryName().toString());
            if (DeskShapelessRecipeBuilder.this.count > 1) {
                result.addProperty("count", DeskShapelessRecipeBuilder.this.count);
            }

            pJson.add("result", result);
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return HyperRecipes.SHAPELESS_DESK.get();
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
