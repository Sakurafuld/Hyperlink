package com.sakurafuld.hyperdaimc.datagen;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperRecipes;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

public class DeskShapedRecipeBuilder implements RecipeBuilder {
    private boolean minecraft = false;
    private final ItemLike result;
    private final int count;
    private final List<String> pattern = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();

    public DeskShapedRecipeBuilder(ItemLike result, int count) {
        this.result = result;
        this.count = count;
    }

    public DeskShapedRecipeBuilder(ItemLike result) {
        this(result, 1);
    }

    public static DeskShapedRecipeBuilder core(String name) {
        return new DeskShapedRecipeBuilder(HyperItems.getCore(name));
    }

    public static DeskShapedRecipeBuilder gist(String name) {
        return new DeskShapedRecipeBuilder(HyperItems.getGist(name));
    }

    public DeskShapedRecipeBuilder minecraft() {
        this.minecraft = true;
        return this;
    }

    public DeskShapedRecipeBuilder define(Character symbol, Ingredient ingredient) {
        if (this.key.containsKey(symbol)) {
            throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
        } else if (symbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(symbol, ingredient);
            return this;
        }
    }

    public DeskShapedRecipeBuilder define(Character symbol, TagKey<Item> tag) {
        return this.define(symbol, Ingredient.of(tag));
    }

    public DeskShapedRecipeBuilder define(Character symbol, ItemLike pItem) {
        return this.define(symbol, Ingredient.of(pItem));
    }

    public DeskShapedRecipeBuilder pattern(String pattern) {
        if (!this.pattern.isEmpty() && pattern.length() != this.pattern.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.pattern.add(pattern);
            return this;
        }
    }

    @Override
    public DeskShapedRecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
        return this;
    }

    @Override
    public DeskShapedRecipeBuilder group(@Nullable String pGroupName) {
        return this;
    }

    @Override
    public Item getResult() {
        return this.result.asItem();
    }

    @Override
    public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId) {
        this.ensureValid(pRecipeId);
        pFinishedRecipeConsumer.accept(new DeskShapedRecipeBuilder.Result(pRecipeId));
    }

    public void saveMaterial(Consumer<FinishedRecipe> finisher) {
        String material = this.result.asItem().getRegistryName().getPath();
        int separator = material.lastIndexOf('_');
        String base = material.substring(0, separator);
        String suffix = material.substring(separator + 1);
        this.save(finisher, identifier(HYPERDAIMC, "material/" + suffix + "/" + base));
    }

    private void ensureValid(ResourceLocation pId) {
        if (this.pattern.isEmpty()) {
            throw new IllegalStateException("No pattern is defined for shaped recipe " + pId + "!");
        } else {
            Set<Character> set = Sets.newHashSet(this.key.keySet());
            set.remove(' ');

            for (String s : this.pattern) {
                for (int i = 0; i < s.length(); ++i) {
                    char c0 = s.charAt(i);
                    if (!this.key.containsKey(c0) && c0 != ' ') {
                        throw new IllegalStateException("Pattern in recipe " + pId + " uses undefined symbol '" + c0 + "'");
                    }

                    set.remove(c0);
                }
            }

            if (!set.isEmpty()) {
                throw new IllegalStateException("Ingredients are defined but not used in pattern for recipe " + pId);
            } else if (this.pattern.size() == 1 && this.pattern.get(0).length() == 1) {
                throw new IllegalStateException("Shaped recipe " + pId + " only takes in a single item - should it be a shapeless recipe instead?");
            }
        }
    }

    public class Result implements FinishedRecipe {
        private final ResourceLocation id;

        public Result(ResourceLocation id) {
            this.id = id;
        }

        public void serializeRecipeData(JsonObject pJson) {
            if (DeskShapedRecipeBuilder.this.minecraft) {
                pJson.addProperty("minecraft", true);
            }
            JsonArray pattern = new JsonArray();

            for (String string : DeskShapedRecipeBuilder.this.pattern) {
                pattern.add(string);
            }

            pJson.add("pattern", pattern);
            JsonObject key = new JsonObject();

            for (Entry<Character, Ingredient> entry : DeskShapedRecipeBuilder.this.key.entrySet()) {
                key.add(String.valueOf(entry.getKey()), entry.getValue().toJson());
            }

            pJson.add("key", key);
            JsonObject result = new JsonObject();
            result.addProperty("item", DeskShapedRecipeBuilder.this.result.asItem().getRegistryName().toString());
            if (DeskShapedRecipeBuilder.this.count > 1) {
                result.addProperty("count", DeskShapedRecipeBuilder.this.count);
            }

            pJson.add("result", result);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return HyperRecipes.SHAPED_DESK.get();
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
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