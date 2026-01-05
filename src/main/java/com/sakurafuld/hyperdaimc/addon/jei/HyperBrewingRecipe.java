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

public abstract class HyperBrewingRecipe {
    public final ResourceLocation id;
    public final List<ItemStack> result;
    public final List<ItemStack> potion;
    public final ItemStack ingredient;

    public HyperBrewingRecipe(ResourceLocation id, List<ItemStack> result, List<ItemStack> potion, ItemStack ingredient) {
        this.id = id;
        this.result = result;
        this.potion = potion;
        this.ingredient = ingredient;
    }

    public abstract Component steps();

    public static HyperBrewingRecipe bugStar(ResourceLocation id, List<ItemStack> result, List<ItemStack> potion, ItemStack ingredient) {
        return new HyperBrewingRecipe(id, result, potion, ingredient) {
            @Override
            public Component steps() {
                return Component.literal("steps").withStyle(ChatFormatting.OBFUSCATED);
            }
        };
    }

    public static HyperBrewingRecipe skulls(ResourceLocation id, List<ItemStack> result, List<ItemStack> potion, ItemStack ingredient) {
        Random random = new Random();
        return new HyperBrewingRecipe(id, result, potion, ingredient) {
            private long lastTime = 0;
            private int lastStep = 0;

            @Override
            public Component steps() {
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
    }
}
