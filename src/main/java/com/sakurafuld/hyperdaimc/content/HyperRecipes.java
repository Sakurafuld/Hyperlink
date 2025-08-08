package com.sakurafuld.hyperdaimc.content;

import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskShapedRecipe;
import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskShapelessRecipe;
import com.sakurafuld.hyperdaimc.content.crafting.desk.IDeskRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

public class HyperRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPE_REGISTRY
            = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, HYPERDAIMC);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZER_REGISTRY
            = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, HYPERDAIMC);

    public static final RegistryObject<RecipeType<IDeskRecipe>> DESK;
    public static final RegistryObject<DeskShapelessRecipe.Serializer> SHAPELESS_DESK;
    public static final RegistryObject<DeskShapedRecipe.Serializer> SHAPED_DESK;

    static {
        DESK = TYPE_REGISTRY.register("desk", () -> RecipeType.simple(identifier("desk")));
        SHAPELESS_DESK = SERIALIZER_REGISTRY.register("shapeless_desk", DeskShapelessRecipe.Serializer::new);
        SHAPED_DESK = SERIALIZER_REGISTRY.register("shaped_desk", DeskShapedRecipe.Serializer::new);
    }
}
