package com.sakurafuld.hyperdaimc.helper;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

public class Writes {
    private Writes() {
    }

    public static final TreeMap<Long, String> FLUID_SUFFIXES = Util.make(new TreeMap<>(), suffixes -> {
        suffixes.put(1L, "mb");
        suffixes.put(1_000L, "B");
        suffixes.put(1_000_000L, "KB");
        suffixes.put(1_000_000_000L, "MB");
        suffixes.put(1_000_000_000_000L, "GB");
        suffixes.put(1_000_000_000_000_000L, "TB");
        suffixes.put(1_000_000_000_000_000_000L, "PB");
    });
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

    public static String fluidAmount(long amount) {
        if (amount == 0) {
            return "0mb";
        } else if (amount < 0) {
            return "-" + fluidAmount(-amount);
        }
        Map.Entry<Long, String> entry = FLUID_SUFFIXES.floorEntry(Math.round(amount * 10d));
        long divide = entry.getKey();
        String suffix = entry.getValue();

        return FLUID_FORMAT.format((double) amount / (double) divide) + suffix;
    }

    public static Component gameOver(String text) {
        MutableComponent component = Component.literal("");

        long millis = Util.getMillis();
        int offset1 = Mth.floor(millis / 150d) % gameOvers.length;
        int offset2 = (Mth.floor(millis / 150d) + 1) % gameOvers.length;
        double delta = (millis % 150d) / 150d;

        for (int index = 0; index < text.length(); index++) {
            char at = text.charAt(index);

            int color1 = gameOvers[(index + gameOvers.length - offset1) % gameOvers.length];
            int color2 = gameOvers[(index + gameOvers.length - offset2) % gameOvers.length];

            int r = mix(delta, (color1 >> 16) & 0xFF, (color2 >> 16) & 0xFF);
            int g = mix(delta, (color1 >> 8) & 0xFF, (color2 >> 8) & 0xFF);
            int b = mix(delta, color1 & 0xFF, color2 & 0xFF);

            int color = (r << 16) | (g << 8) | b;

            component.append(Component.literal(String.valueOf(at)).withStyle(style -> style.withColor(color)));
        }

        return component;
    }

    private static int mix(double delta, int first, int second) {
        return (int) (delta * (double) first + (1d - delta) * (double) second);
    }
}
