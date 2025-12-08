package com.sakurafuld.hyperdaimc.infrastructure;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.Objects;

public class Calculates {
    public static final Comparator<ItemStack> LOWEST_TO_HIGHEST = makeSorter(true);
    public static final Comparator<ItemStack> HIGHEST_TO_LOWEST = makeSorter(false);

    private Calculates() {
    }

    private static Comparator<ItemStack> makeSorter(boolean low2High) {
        return (a, b) -> {
            int aCount = a.getCount();
            int bCount = b.getCount();
            if (aCount == bCount) {
                ResourceLocation aId = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(a.getItem()));
                ResourceLocation bId = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(b.getItem()));
                return aId.compareTo(bId);
            } else return Integer.compare(aCount, bCount) * (low2High ? 1 : -1);
        };
    }

    public static double curve(double delta, double p1, double p2, double p3) {
        return Math.pow(1 - delta, 2) * p1
                +
                2 * (1 - delta) * delta * p2
                +
                Math.pow(delta, 2) * p3;
    }

    public static double curve(double delta, double p1, double p2, double p3, double p4) {
        return Math.pow(1 - delta, 3) * p1
                +
                3 * Math.pow(1 - delta, 2) * delta * p2
                +
                3 * (1 - delta) * Math.pow(delta, 2) * p3
                +
                Math.pow(delta, 3) * p4;
    }

    public static double curve(double delta, double p1, double p2, double p3, double p4, double p5) {
        return Math.pow(1 - delta, 4) * p1
                +
                4 * delta * Math.pow(1 - delta, 3) * p2
                +
                6 * Math.pow(delta, 2) * Math.pow(1 - delta, 2) * p3
                +
                4 * Math.pow(delta, 3) * (1 - delta) * p4
                +
                Math.pow(delta, 4) * p5;
    }

//    public static Direction viewDirection(float xRot, float yRot) {
//        float xRad = (float) Math.toRadians(xRot);
//        float yRad = (float) Math.toRadians(-yRot);
//        float sinX = Mth.sin(xRad);
//        float cosX = Mth.cos(xRad);
//        float sinY = Mth.sin(yRad);
//        float cosY = Mth.cos(yRad);
//        float sinYAbs = Math.abs(sinY);
//        float sinXAbs = Math.abs(sinX);
//        float cosYAbs = Math.abs(cosY);
//        Direction xAxis = sinY > 0 ? Direction.EAST : Direction.WEST;
//        Direction yAxis = sinX < 0 ? Direction.UP : Direction.DOWN;
//        Direction zAxis = cosY > 0 ? Direction.SOUTH : Direction.NORTH;
//        if (sinYAbs > cosYAbs)
//            if (sinXAbs > sinYAbs * cosX)
//                return yAxis;
//            else
//                return xAxis;
//        if (sinXAbs > cosYAbs * cosX)
//            return yAxis;
//        else
//            return zAxis;
//    }
}
