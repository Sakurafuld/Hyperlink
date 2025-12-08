package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sakurafuld.hyperdaimc.content.HyperRecipes;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.infrastructure.Deets.LOG;

public class DeskShapelessRecipe implements IDeskRecipe {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final boolean simple;
    private final boolean minecraft;
    private final boolean hideFromJei;

    public DeskShapelessRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, ItemStack result, boolean minecraft, boolean hideFromJei) {
        this.id = id;
        this.result = result;
        this.ingredients = ingredients;
        this.minecraft = minecraft;
        this.simple = ingredients.stream().allMatch(Ingredient::isSimple);
        this.hideFromJei = hideFromJei;
    }

    @Override
    public boolean isMinecraft() {
        return this.minecraft;
    }

    @Override
    public boolean showToJei() {
        return !this.hideFromJei;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    @Override
    public boolean matches(RecipeWrapper pContainer, Level pLevel) {
        StackedContents contents = new StackedContents();
        List<ItemStack> inputs = new ArrayList<>();
        int size = 0;

        for (int index = 0; index < pContainer.getContainerSize(); ++index) {
            ItemStack stack = pContainer.getItem(index);
            if (!stack.isEmpty()) {
                ++size;
                if (this.simple)
                    contents.accountStack(stack, 1);
                else inputs.add(stack);
            }
        }

        return size == this.ingredients.size() && (this.simple ? contents.canCraft(this, null) : RecipeMatcher.findMatches(inputs, this.ingredients) != null);

    }

    @Override
    public ItemStack assemble(RecipeWrapper pContainer, RegistryAccess pRegistryAccess) {
        return this.getResultItem(pRegistryAccess).copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= this.ingredients.size();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return this.result;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HyperRecipes.SHAPELESS_DESK.get();
    }

    public static class Serializer implements RecipeSerializer<DeskShapelessRecipe> {
        @Override
        public DeskShapelessRecipe fromJson(ResourceLocation recipeLoc, JsonObject recipeJson, ICondition.IContext context) {
            NonNullList<Ingredient> ingredients = NonNullList.create();

            List<Item> items = Lists.newArrayList();
            if (GsonHelper.isArrayNode(recipeJson, "values")) {
                boolean duplicate = GsonHelper.getAsBoolean(recipeJson, "duplicate", false);

                for (JsonElement element : GsonHelper.getAsJsonArray(recipeJson, "values")) {
                    String value = GsonHelper.convertToString(element, "value");
                    if (value.startsWith("#")) {
                        items.addAll(context.getTag(ItemTags.create(ResourceLocation.parse(value.substring(1)))).stream().map(Holder::value).toList());
                    } else {
                        items.add(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(value)));
                    }
                }
                List<Item> values = Lists.newArrayList();
                items.stream()
                        .filter(Objects::nonNull)
                        .filter(item -> item != Items.AIR)
                        .sorted(Comparator.comparing(item -> {
                            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);

                            if (id != null) {
                                return id.getNamespace();
                            } else {
                                return "";
                            }
                        }))
                        .sorted(Comparator.comparingInt(item -> {
                            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);

                            if (id != null) {
                                if (id.getNamespace().equals("minecraft")) {
                                    return -1;
                                } else if (id.getNamespace().equals(HYPERDAIMC)) {
                                    return -2;
                                }
                            }

                            return 0;
                        }))
                        .forEach(item -> {
                            if (duplicate || !values.contains(item)) {
                                values.add(item);
                            }
                        });

                if (GsonHelper.isArrayNode(recipeJson, "exclusion")) {
                    for (JsonElement element : GsonHelper.getAsJsonArray(recipeJson, "exclusion")) {
                        String exclusion = GsonHelper.convertToString(element, "exclusion");
                        if (exclusion.startsWith("#")) {
                            values.removeAll(context.getTag(ItemTags.create(ResourceLocation.parse(exclusion.substring(1)))).stream().map(Holder::value).toList());
                        } else {
                            values.remove(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(exclusion)));
                        }
                    }
                }

                ingredients.addAll(values.stream().map(Ingredient::of).toList());
            }

            if (recipeJson.has("ingredients")) {

                JsonArray array = GsonHelper.getAsJsonArray(recipeJson, "ingredients");
                for (int index = 0; index < array.size(); ++index) {
                    Ingredient ingredient = Ingredient.fromJson(array.get(index));
                    ingredients.add(ingredient);
                }

                if (ingredients.isEmpty()) {
                    throw new JsonParseException("No ingredients for desk shapeless recipe");
                } else if (ingredients.size() > 9 * 9) {
                    throw new JsonParseException("Too many ingredients for desk shapeless recipe. The maximum is " + (9 * 9));
                }
                LOG.debug("deskLoadIngredients:{}", ingredients.size());
            }

            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(recipeJson, "result"));
            boolean minecraft = GsonHelper.getAsBoolean(recipeJson, "minecraft", false);
            boolean hideFromJei = GsonHelper.getAsBoolean(recipeJson, "hideFromJei", false);
            return new DeskShapelessRecipe(recipeLoc, Util.make(NonNullList.create(), list -> list.addAll(ingredients.stream().limit(9 * 9).toList())), result, minecraft, hideFromJei);
        }

        @Override
        public DeskShapelessRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            LOG.debug("deskRecipeError!!");
            return null;
        }

        @Override
        public DeskShapelessRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            NonNullList<Ingredient> ingredients = NonNullList.withSize(pBuffer.readVarInt(), Ingredient.EMPTY);

            ingredients.replaceAll(ignored -> Ingredient.fromNetwork(pBuffer));

            return new DeskShapelessRecipe(pRecipeId, ingredients, pBuffer.readItem(), pBuffer.readBoolean(), pBuffer.readBoolean());
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, DeskShapelessRecipe pRecipe) {
            pBuffer.writeVarInt(pRecipe.ingredients.size());

            for (Ingredient ingredient : pRecipe.ingredients) {
                ingredient.toNetwork(pBuffer);
            }

            pBuffer.writeItem(pRecipe.result);
            pBuffer.writeBoolean(pRecipe.minecraft);
            pBuffer.writeBoolean(pRecipe.hideFromJei);
        }
    }
}
