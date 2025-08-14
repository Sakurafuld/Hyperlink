package com.sakurafuld.hyperdaimc;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class HyperCommonConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_MUTEKI;
    public static final ForgeConfigSpec.BooleanValue MUTEKI_NOVEL;
    public static final ForgeConfigSpec.BooleanValue MUTEKI_SELECTOR;

    public static final ForgeConfigSpec.BooleanValue ENABLE_NOVEL;
    public static final ForgeConfigSpec.BooleanValue NOVEL_VULNERABILIZATION;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> NOVEL_IGNORE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> NOVEL_SPECIAL;
    public static final ForgeConfigSpec.BooleanValue NOVEL_INVERT_SHIFT;

    public static final ForgeConfigSpec.BooleanValue ENABLE_CHRONICLE;
    public static final ForgeConfigSpec.BooleanValue CHRONICLE_VULNERABILIZATION;
    public static final ForgeConfigSpec.BooleanValue CHRONICLE_PARADOX;
    public static final ForgeConfigSpec.BooleanValue CHRONICLE_OWNER;
    public static final ForgeConfigSpec.BooleanValue CHRONICLE_INTERACT;
    public static final ForgeConfigSpec.IntValue CHRONICLE_SIZE;
    public static final ForgeConfigSpec.BooleanValue CHRONICLE_INVERT_SHIFT;

    public static final ForgeConfigSpec.BooleanValue ENABLE_PARADOX;
    public static final ForgeConfigSpec.BooleanValue PARADOX_HIT_FLUID;
    public static final ForgeConfigSpec.BooleanValue PARADOX_INVERT_SHIFT;

    public static final ForgeConfigSpec.BooleanValue ENABLE_VRX;
    public static final ForgeConfigSpec.BooleanValue VRX_VULNERABILIZATION;
    public static final ForgeConfigSpec.BooleanValue VRX_KEEP;
    public static final ForgeConfigSpec.BooleanValue VRX_PLAYER;
    public static final ForgeConfigSpec.BooleanValue VRX_JEI;
    public static final ForgeConfigSpec.BooleanValue VRX_SEAL_HYPERLINK;

    public static final ForgeConfigSpec.BooleanValue FUMETSU_RECIPE;
    public static final ForgeConfigSpec.BooleanValue FUMETSU_SUMMON;
    public static final ForgeConfigSpec.IntValue FUMETSU_HEALTH;
    public static final ForgeConfigSpec.IntValue FUMETSU_RANGE;
    public static final ForgeConfigSpec.BooleanValue FUMETSU_UNDERGROUND;

    public static final ForgeConfigSpec.IntValue MATERIALIZER_TIME;
    public static final ForgeConfigSpec.BooleanValue MATERIALIZER_STACK_INGREDIENTS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MATERIALIZER_RECIPE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MATERIALIZER_FUEL;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MATERIALIZER_RECIPE_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MATERIALIZER_TAG_BLACKLIST;


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
                NOVEL_VULNERABILIZATION = builder
                        .comment("Enable vulnerability of Novel",
                                "Default: false")
                        .define("Novel vulnerabilization", false);
                NOVEL_IGNORE = builder
                        .comment("Specific entities that ignore Novelize")
                        .defineList("Ignore entities", List.of("minecraft:item", "minecraft:experience_orb", "hyperdaimc:fumetsu_skull", "hyperdaimc:fumetsu_storm", "hyperdaimc:fumetsu_storm_skull"),
                                object -> object instanceof String string && ResourceLocation.isValidResourceLocation(string));
                NOVEL_SPECIAL = builder
                        .comment("Specific entities not interrupted in the death process by Novel ( e.g. entities with a death animation )")
                        .defineList("Special entities", List.of("minecraft:ender_dragon", "draconicevolution:draconic_guardian", "cataclysm:ender_guardian", "cataclysm:netherite_monstrosity", "cataclysm:ignis", "cataclysm:the_harbinger", "cataclysm:the_prowler", "cataclysm:coralssus", "cataclysm:amethyst_crab", "cataclysm:ancient_remnant", "cataclysm:wadjet", "cataclysm:maledictus", "cataclysm:aptrgangr"),
                                object -> object instanceof String string && ResourceLocation.isValidResourceLocation(string));
                NOVEL_INVERT_SHIFT = builder
                        .comment("Default: false ( = Shift to target a single entity)")
                        .define("Invert Novel control", false);
            }
            builder.pop();

            builder.push("Chronicle");
            {
                ENABLE_CHRONICLE = builder
                        .comment("Enable behavior of Chronicle",
                                "Default: true")
                        .define("Enable Chronicle", true);
                CHRONICLE_VULNERABILIZATION = builder
                        .comment("Enable vulnerability of Chronicle",
                                "Default: false")
                        .define("Chronicle vulnerabilization", false);
                CHRONICLE_PARADOX = builder
                        .comment("Paradox may not Perfect Knockout Chronicle",
                                "Default: false")
                        .define("Hyper Chronicle", false);
                CHRONICLE_OWNER = builder
                        .comment("Owner may not block action in Chronicle",
                                "Default: false")
                        .define("Pause owner", false);
                CHRONICLE_INTERACT = builder
                        .comment("Cannot interact with blocks in Chronicle ( Like in Adventure Mode )",
                                "Default: false")
                        .define("Pause interaction", false);
                CHRONICLE_SIZE = builder
                        .comment("Max selection size of Chronicle",
                                "Default: 16384")
                        .defineInRange("Selection size", 16384, 1, Integer.MAX_VALUE);
                CHRONICLE_INVERT_SHIFT = builder
                        .comment("Default: false ( = Shift to target a face-offset block)")
                        .define("Invert Chronicle control", false);
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
                PARADOX_INVERT_SHIFT = builder
                        .comment("Default: false ( = Shift to no continuous target blocks)")
                        .define("Invert Paradox control", false);
            }
            builder.pop();

            builder.push("VRX");
            {
                ENABLE_VRX = builder
                        .comment("Enable behavior of VRX",
                                "Default: true")
                        .define("Enable VRX", true);
                VRX_VULNERABILIZATION = builder
                        .comment("Enable vulnerability of VRX",
                                "Default: false")
                        .define("VRX vulnerabilization", false);
                VRX_KEEP = builder
                        .comment("Keep VRX on yourself after death",
                                "Default: true")
                        .define("Keep VRX", true);
                VRX_PLAYER = builder
                        .comment("Can VRX be set to other players",
                                "Default: false")
                        .define("Create for others", false);
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
                FUMETSU_RECIPE = builder
                        .comment("Enable Chemical-MAX and Fumetsu Skull brewing recipes",
                                "Default: true")
                        .define("Enable recipes", true);
                FUMETSU_SUMMON = builder
                        .comment("Enable Fumetsu summoning by assembling blocks",
                                "Default: true")
                        .define("Enable summoning", true);
                FUMETSU_HEALTH = builder
                        .comment("Default: 20")
                        .defineInRange("Max health", 20, 20, Integer.MAX_VALUE);
                FUMETSU_RANGE = builder
                        .comment("The range for which Fumetsu sets targets",
                                "Default: 128")
                        .defineInRange("Search range", 128, 64, Integer.MAX_VALUE);
                FUMETSU_UNDERGROUND = builder
                        .comment("Fumetsu will try to define the underground opponent as a target",
                                "Default: false")
                        .define("Search underground", false);
            }
            builder.pop();

            builder.push("Materializer");
            {
                MATERIALIZER_TIME = builder
                        .comment("Materializer processing time",
                                "Default: 6000")
                        .defineInRange("Process time", 6000, 1, Integer.MAX_VALUE);
                MATERIALIZER_STACK_INGREDIENTS = builder
                        .comment("If the same ingredients appear in recipes, stack them",
                                "Default: false ( = Even if the same material exists, only one item of each type will be extracted )")
                        .define("Stack same ingredients", false);
                MATERIALIZER_RECIPE = builder
                        .comment("Recipe types searched by Materializer",
                                "Default: [ minecraft:crafting, minecraft:smelting, minecraft:blasting, minecraft:smoking, minecraft:campfire_cooking, minecraft:smithing, hyperdaimc:desk, avaritia:crafting_table_recipe, avaritia:compressor_recipe, avaritia:extreme_smithing_recipe ]")
                        .defineList("Materializer recipe types", List.of("minecraft:crafting", "minecraft:smelting", "minecraft:blasting", "minecraft:smoking", "minecraft:campfire_cooking", "minecraft:smithing", "hyperdaimc:desk", "avaritia:crafting_table_recipe", "avaritia:compressor_recipe", "avaritia:extreme_smithing_recipe"), object -> object instanceof String string && ResourceLocation.isValidResourceLocation(string));
                MATERIALIZER_FUEL = builder
                        .comment("Specify the items that can be used as fuel for the materializer, along with the number of times they can be used",
                                "Write the item ID on the left side of the = and the number of times on the right side ( e.g., hyperdaimc:god_sigil=64 )",
                                "The item ID can also be used as a tag ID by adding a # at the beginning")
                        .defineList("Materializer fuels", List.of("hyperdaimc:god_sigil=64"), object -> {
                            if (object instanceof String string) {
                                if (string.startsWith("#")) {
                                    string = string.substring(1);
                                }

                                String[] split = string.split("=");
                                return ResourceLocation.isValidResourceLocation(split[0]) && split[1].chars().allMatch(Character::isDigit);
                            } else {
                                return false;
                            }
                        });
                MATERIALIZER_RECIPE_BLACKLIST = builder
                        .comment("Specify recipes to ignore when searching recipes")
                        .defineList("Recipe blacklist", List.of("hyperdaimc:nether_star", "hyperdaimc:desk", "hyperdaimc:game_orb", "hyperdaimc:hyper/muteki", "hyperdaimc:hyper/novel", "hyperdaimc:hyper/chronicle", "hyperdaimc:hyper/paradox", "hyperdaimc:hyper/vrx"), object -> object instanceof String string && ResourceLocation.isValidResourceLocation(string));
                MATERIALIZER_TAG_BLACKLIST = builder
                        .comment("Specify tags to ignore when searching recipes")
                        .defineList("Tag blacklist", List.of("forge:ingots", "forge:gems", "forge:storage_blocks", "forge:nuggets", "hyperdaimc:essences", "hyperdaimc:cores", "hyperdaimc:gists", "tconstruct:anvil_metal"), object -> object instanceof String string && ResourceLocation.isValidResourceLocation(string));
            }
            builder.pop();
        }
        builder.pop();

        SPEC = builder.build();
    }
}
