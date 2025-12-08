package com.sakurafuld.hyperdaimc.infrastructure.render;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemColorCommon {
    int getTint(ItemStack stack, int index);
}
