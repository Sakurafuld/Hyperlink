package com.sakurafuld.hyperdaimc.addon.jei;

import com.sakurafuld.hyperdaimc.infrastructure.Calculates;
import com.sakurafuld.hyperdaimc.infrastructure.Writes;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public record HyperBrewingRecipe(ResourceLocation id, List<ItemStack> result, List<ItemStack> potion,
                                 ItemStack ingredient, Supplier<Component> steps) {
    public static HyperBrewingRecipe unknownSteps(ResourceLocation id, List<ItemStack> result, List<ItemStack> potion, ItemStack ingredient) {
        Supplier<Component> steps = () -> Component.literal("steps").withStyle(ChatFormatting.OBFUSCATED);
        return new HyperBrewingRecipe(id, result, potion, ingredient, steps);
    }

    public static HyperBrewingRecipe randomSteps(ResourceLocation id, List<ItemStack> result, List<ItemStack> potion, ItemStack ingredient) {
        Random random = new Random();
        Supplier<Component> steps = new Supplier<>() {
            private long lastTime = 0;
            private int lastStep = 0;

            @Override
            public Component get() {
                if (Util.getMillis() - this.lastTime > 50) {
                    this.lastTime = Util.getMillis();
                    double delta = Math.min(1, ((Math.cos(this.lastTime / 800d) + 1) / 2d));
                    delta = Calculates.curve(delta, 0, 0.00001, 0.0002, 1);
                    int inaccuracy = (int) (delta * Integer.MAX_VALUE);
                    this.lastStep = inaccuracy - random.nextInt(inaccuracy / 2 + 1);
                }
                return Writes.gameOver(String.valueOf(this.lastStep));
            }
        };
        return new HyperBrewingRecipe(id, result, potion, ingredient, steps);
    }
}
