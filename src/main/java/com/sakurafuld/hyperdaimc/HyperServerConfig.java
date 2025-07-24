package com.sakurafuld.hyperdaimc;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class HyperServerConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_MUTEKI;
    public static final ForgeConfigSpec.BooleanValue MUTEKI_NOVEL;
    public static final ForgeConfigSpec.BooleanValue MUTEKI_SELECTOR;

    public static final ForgeConfigSpec.BooleanValue ENABLE_NOVEL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> NOVEL_IGNORE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> NOVEL_SPECIAL;

    public static final ForgeConfigSpec.BooleanValue ENABLE_CHRONICLE;
    public static final ForgeConfigSpec.BooleanValue CHRONICLE_PARADOX;
    public static final ForgeConfigSpec.BooleanValue CHRONICLE_OWNER;
    public static final ForgeConfigSpec.BooleanValue CHRONICLE_INTERACT;
    public static final ForgeConfigSpec.IntValue CHRONICLE_SIZE;

    public static final ForgeConfigSpec.BooleanValue ENABLE_PARADOX;
    public static final ForgeConfigSpec.BooleanValue PARADOX_HIT_FLUID;

    public static final ForgeConfigSpec.BooleanValue ENABLE_VRX;
    public static final ForgeConfigSpec.BooleanValue VRX_KEEP;
    public static final ForgeConfigSpec.BooleanValue VRX_JEI;
    public static final ForgeConfigSpec.BooleanValue VRX_SEAL_HYPERLINK;

    public static final ForgeConfigSpec.IntValue FUMETSU_HEALTH;
    public static final ForgeConfigSpec.IntValue FUMETSU_RANGE;
    public static final ForgeConfigSpec.BooleanValue FUMETSU_UNDERGROUND;


    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Hyperlink");
        {
            builder.push("Muteki");
            {
                ENABLE_MUTEKI = builder
                        .comment("Enable behavior of Muteki",
                                "Default: true")
                        .define("Enable Muteki", true);
                MUTEKI_NOVEL = builder
                        .comment("Novel may not Novelize Muteki",
                                "Default: false")
                        .define("Hyper Muteki", false);
                MUTEKI_SELECTOR = builder
                        .comment("Muteki is not selected by command selector ( e.g. not included in @e or @a )",
                                "Default: true")
                        .define("Muteki command", true);
            }
            builder.pop();

            builder.push("Novel");
            {
                ENABLE_NOVEL = builder
                        .comment("Enable behavior of Novel",
                                "Default: true")
                        .define("Enable Novel", true);
                NOVEL_IGNORE = builder
                        .comment("Specific entities that ignore Novelize")
                        .defineList("Ignore entities", Lists.newArrayList("minecraft:item", "minecraft:experience_orb", "hyperdaimc:fumetsu_skull", "hyperdaimc:fumetsu_storm", "hyperdaimc:fumetsu_storm_skull"),
                                object -> object instanceof String string && ResourceLocation.isValidResourceLocation(string));
                NOVEL_SPECIAL = builder
                        .comment("Specific entities not interrupted in the death process by Novel ( e.g. entities with a death animation )")
                        .defineList("Special entities", Lists.newArrayList("minecraft:ender_dragon", "draconicevolution:draconic_guardian", "cataclysm:ender_guardian", "cataclysm:netherite_monstrosity", "cataclysm:ignis", "cataclysm:the_harbinger", "cataclysm:the_prowler", "cataclysm:coralssus", "cataclysm:amethyst_crab", "cataclysm:ancient_remnant", "cataclysm:wadjet", "cataclysm:maledictus", "cataclysm:aptrgangr"),
                                object -> object instanceof String string && ResourceLocation.isValidResourceLocation(string));
            }
            builder.pop();

            builder.push("Chronicle");
            {
                ENABLE_CHRONICLE = builder
                        .comment("Enable behavior of Chronicle",
                                "Default: true")
                        .define("Enable Chronicle", true);
                CHRONICLE_PARADOX = builder
                        .comment("Paradox may not Perfect Knockout Chronicle",
                                "Default: false")
                        .define("Hyper Chronicle", false);
                CHRONICLE_OWNER = builder
                        .comment("Owner may not block action in Chronicle",
                                "Default: false")
                        .define("Paused owner", false);
                CHRONICLE_INTERACT = builder
                        .comment("Cannot interact with blocks in Chronicle ( Like in Adventure Mode )",
                                "Default: false")
                        .define("Interact Chronicle", false);
                CHRONICLE_SIZE = builder
                        .comment("Max selection size of Chronicle",
                                "Default: 16384")
                        .defineInRange("Selection size", 16384, 1, Integer.MAX_VALUE);
            }
            builder.pop();

            builder.push("Paradox");
            {
                ENABLE_PARADOX = builder
                        .comment("Enable behavior of Paradox",
                                "Default: true")
                        .define("Enable Paradox", true);
                PARADOX_HIT_FLUID = builder
                        .comment("Paradox may Perfect Knockout liquid blocks",
                                "Default: true")
                        .define("Fluid Paradox", true);
            }
            builder.pop();

            builder.push("VRX");
            {
                ENABLE_VRX = builder
                        .comment("Enable behavior of VRX",
                                "Default: true")
                        .define("Enable VRX", true);
                VRX_KEEP = builder
                        .comment("Keep VRX after death",
                                "Default: false")
                        .define("Keep VRX", false);
                VRX_JEI = builder
                        .comment("Can configure VRX using JEI",
                                "Default: true")
                        .define("Just Enough VRX", true);
                VRX_SEAL_HYPERLINK = builder
                        .comment("Unable to set Hyperlink items in VRX",
                                "Default: true")
                        .define("Seal Hyperlink", true);
            }
            builder.pop();

            builder.push("Fumetsu");
            {
                FUMETSU_HEALTH = builder
                        .comment("Maximum health of Fumetsu Wither",
                                "Default: 20")
                        .worldRestart()
                        .defineInRange("Fumetsu health", 20, 20, Integer.MAX_VALUE);
                FUMETSU_RANGE = builder
                        .comment("The range for which Fumetsu sets targets",
                                "Default: 128")
                        .defineInRange("Fumetsu range", 128, 64, Integer.MAX_VALUE);
                FUMETSU_UNDERGROUND = builder
                        .comment("Fumetsu will try to define the underground opponent as a target",
                                "Default: false")
                        .define("Fumetsu underground", false);
            }
            builder.pop();
        }
        builder.pop();

        SPEC = builder.build();
    }
}
