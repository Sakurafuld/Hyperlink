package com.sakurafuld.hyperdaimc.infrastructure;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.MutableInt;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

public class Writes {
    public static final TreeMap<Long, String> ITEM_SUFFIXES = Util.make(new TreeMap<>(), suffixes -> {
        suffixes.put(1L, "");
        suffixes.put(1_000L, "K");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    });
    public static final TreeMap<Long, String> FLUID_SUFFIXES = Util.make(new TreeMap<>(), suffixes -> {
        suffixes.put(1L, "mb");
        suffixes.put(1_000L, "B");
        suffixes.put(1_000_000L, "KB");
        suffixes.put(1_000_000_000L, "MB");
        suffixes.put(1_000_000_000_000L, "GB");
        suffixes.put(1_000_000_000_000_000L, "TB");
        suffixes.put(1_000_000_000_000_000_000L, "PB");
    });
    public static final DecimalFormat ITEM_FORMAT = Util.make(new DecimalFormat("0.#"), format -> format.setRoundingMode(RoundingMode.FLOOR));
    public static final DecimalFormat FLUID_FORMAT = Util.make(new DecimalFormat("0.##"), format -> format.setRoundingMode(RoundingMode.FLOOR));
    private static final int[] gameOvers = new int[]{
            0x805050,
            0x906060,
            0xA07070,
            0xB08080,
            0xC09090,

            0xC0A0A0,
            0xB0A0A0,
            0xA0A0A0,
            0xA0A0A0,
            0xA0A0A0,
            0xA0B0B0,
            0xA0C0C0,

            0x90C0C0,
            0x80B0B0,
            0x70A0A0,
            0x609090,
            0x508080,
            0x609090,
            0x70A0A0,
            0x80B0B0,
            0x90C0C0,

            0xA0C0C0,
            0xA0B0B0,
            0xA0A0A0,
            0xA0A0A0,
            0xA0A0A0,
            0xB0A0A0,
            0xC0A0A0,

            0xC09090,
            0xB08080,
            0xA07070,
            0x906060
    };

    private Writes() {
    }

    public static String itemCount(long count) {
        if (count == 0) return "0";
        else if (count < 0) return "-" + itemCount(-count);

        Map.Entry<Long, String> entry = ITEM_SUFFIXES.floorEntry(count);
        double divide = entry.getKey();
        String suffix = entry.getValue();

        return ITEM_FORMAT.format(count / divide) + suffix;
    }

    public static String fluidAmount(long amount) {
        if (amount == 0) return "0mb";
        else if (amount < 0) return "-" + fluidAmount(-amount);

        Map.Entry<Long, String> entry = FLUID_SUFFIXES.floorEntry(Math.round(amount * 10d));
        double divide = entry.getKey();
        String suffix = entry.getValue();

        return FLUID_FORMAT.format(amount / divide) + suffix;
    }

    public static Component gameOver(String text) {
        MutableComponent component = Component.empty();
        for (MutableInt index = new MutableInt(); index.intValue() < text.length(); index.increment()) {
            char at = text.charAt(index.intValue());
            component.append(Component.literal(String.valueOf(at))
                    .withStyle(style -> style.withColor(gameOver(index.intValue()))));
        }
        return component;
    }

    public static int gameOver(int index) {
        long millis = Util.getMillis();
        int offset1 = Mth.floor(millis / 150d) % gameOvers.length;
        int offset2 = (Mth.floor(millis / 150d) + 1) % gameOvers.length;
        double delta = (millis % 150d) / 150d;
        int color1 = gameOvers[(index + gameOvers.length - offset1) % gameOvers.length];
        int color2 = gameOvers[(index + gameOvers.length - offset2) % gameOvers.length];
        return FastColor.ARGB32.lerp((float) delta, color1, color2);
    }
}
