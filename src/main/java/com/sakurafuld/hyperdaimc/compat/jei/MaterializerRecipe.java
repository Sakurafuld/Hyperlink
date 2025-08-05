package com.sakurafuld.hyperdaimc.compat.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record MaterializerRecipe(ItemStack catalyst, List<ItemStack> results) {
}
