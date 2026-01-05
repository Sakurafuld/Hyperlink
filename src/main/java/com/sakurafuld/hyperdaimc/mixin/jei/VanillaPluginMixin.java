package com.sakurafuld.hyperdaimc.mixin.jei;

import com.sakurafuld.hyperdaimc.addon.jei.HyperBrewingRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(VanillaPlugin.class)
public abstract class VanillaPluginMixin {
    @ModifyArg(method = "registerGuiHandlers", at = @At(value = "INVOKE", target = "Lmezz/jei/api/registration/IGuiHandlerRegistration;addRecipeClickArea(Ljava/lang/Class;IIII[Lmezz/jei/api/recipe/RecipeType;)V", ordinal = 2, remap = false), index = 5, remap = false)
    private RecipeType<?>[] registerGuiHandlersJei(RecipeType<?>[] recipeTypes) {
        return ArrayUtils.add(recipeTypes, HyperBrewingRecipeCategory.TYPE);
    }
}
