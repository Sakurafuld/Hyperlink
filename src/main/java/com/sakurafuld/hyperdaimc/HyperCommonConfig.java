package com.sakurafuld.hyperdaimc;

import net.minecraftforge.common.ForgeConfigSpec;

public class HyperCommonConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue NOVEL_INVERT_SHIFT;
    public static final ForgeConfigSpec.BooleanValue CHRONICLE_INVERT_SHIFT;
    public static final ForgeConfigSpec.BooleanValue PARADOX_INVERT_SHIFT;


    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Hyperlink");
        {
            builder.push("Novel");
            {
                NOVEL_INVERT_SHIFT = builder
                        .comment("Default: false ( = Shift to target a single entity)")
                        .define("Invert Novel control", false);
            }
            builder.pop();

            builder.push("Chronicle");
            {
                CHRONICLE_INVERT_SHIFT = builder
                        .comment("Default: false ( = Shift to target a face-offset block)")
                        .define("Invert Chronicle control", false);
            }
            builder.pop();

            builder.push("Paradox");
            {
                PARADOX_INVERT_SHIFT = builder
                        .comment("Default: false ( = Shift to no continuous target blocks)")
                        .define("Invert Paradox control", false);
            }
            builder.pop();
        }
        builder.pop();

        SPEC = builder.build();
    }
}
