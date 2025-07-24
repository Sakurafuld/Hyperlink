package com.sakurafuld.hyperdaimc.datagen;

import com.sakurafuld.hyperdaimc.content.HyperItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

public class HyperTagsProvider extends ItemTagsProvider {

    public HyperTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, pLookupProvider, pBlockTags, HYPERDAIMC, existingFileHelper);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addTags(HolderLookup.Provider pProvider) {
        Object2ObjectOpenHashMap<String, IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item>> material = new Object2ObjectOpenHashMap<>();
        for (Object2ObjectMap.Entry<String, RegistryObject<Item>> entry : HyperItems.MATERIAL.object2ObjectEntrySet()) {
            int separator = entry.getKey().lastIndexOf('_');
            String suffix = entry.getKey().substring(separator + 1);
            material.computeIfAbsent(suffix, s -> this.tag(s + "s")).add(entry.getValue().get());
        }

        this.tag("essence/ground")
                .add(Items.GRASS_BLOCK, Items.DIRT, Items.COARSE_DIRT, Items.PODZOL, Items.ROOTED_DIRT, Items.MYCELIUM)
                .add(Items.SAND, Items.RED_SAND, Items.GRAVEL)
                .add(Items.SNOW, Items.SNOW_BLOCK, Items.CLAY, Items.MUD, Items.MUDDY_MANGROVE_ROOTS)
                .addTag(ItemTags.TERRACOTTA)
                .add(Items.CRIMSON_NYLIUM, Items.WARPED_NYLIUM);
        this.tag("essence/crust")
                .add(Items.STONE, Items.COBBLESTONE, Items.MOSSY_COBBLESTONE)
                .add(Items.DEEPSLATE, Items.COBBLED_DEEPSLATE)
                .add(Items.SANDSTONE, Items.RED_SANDSTONE)
                .add(Items.ANDESITE, Items.DIORITE, Items.GRANITE, Items.SMOOTH_BASALT, Items.CALCITE, Items.TUFF, Items.DRIPSTONE_BLOCK)
                .addTag(Tags.Items.NETHERRACK)
                .add(Items.BLACKSTONE, Items.BASALT)
                .addTag(Tags.Items.END_STONES);
        this.tag("essence/mineral")
                .addTags(Tags.Items.ORES)
                .addTags(Tags.Items.STORAGE_BLOCKS_RAW_COPPER, Tags.Items.STORAGE_BLOCKS_RAW_GOLD, Tags.Items.STORAGE_BLOCKS_RAW_IRON)
                .addTags(Tags.Items.STORAGE_BLOCKS_AMETHYST, Tags.Items.STORAGE_BLOCKS_COAL, Tags.Items.STORAGE_BLOCKS_COPPER, Tags.Items.STORAGE_BLOCKS_DIAMOND, Tags.Items.STORAGE_BLOCKS_EMERALD, Tags.Items.STORAGE_BLOCKS_GOLD, Tags.Items.STORAGE_BLOCKS_IRON, Tags.Items.STORAGE_BLOCKS_LAPIS, Tags.Items.STORAGE_BLOCKS_QUARTZ, Tags.Items.STORAGE_BLOCKS_REDSTONE, Tags.Items.STORAGE_BLOCKS_NETHERITE);
        this.tag("essence/herb")
                .add(Items.GRASS, Items.FERN, Items.DEAD_BUSH)
                .addTags(ItemTags.SMALL_FLOWERS, ItemTags.TALL_FLOWERS)
                .add(Items.SPORE_BLOSSOM, Items.MOSS_CARPET, Items.MOSS_BLOCK, Items.HANGING_ROOTS, Items.SMALL_DRIPLEAF, Items.BIG_DRIPLEAF)
                .add(Items.SUGAR_CANE, Items.BAMBOO, Items.CACTUS, Items.VINE, Items.LILY_PAD)
                .addTag(Tags.Items.MUSHROOMS)
                .add(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS)
                .add(Items.WHEAT, Items.COCOA_BEANS)
                .add(Items.CRIMSON_ROOTS, Items.WEEPING_VINES, Items.WARPED_ROOTS, Items.TWISTING_VINES);
        this.tag("essence/marine")
                .add(Items.WATER_BUCKET)
                .add(Items.SEAGRASS, Items.KELP)
                .add(Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH)
                .add(Items.TURTLE_EGG, Items.SCUTE)
                .add(Items.TUBE_CORAL, Items.BRAIN_CORAL, Items.BUBBLE_CORAL, Items.FIRE_CORAL, Items.HORN_CORAL)
                .add(Items.TUBE_CORAL_BLOCK, Items.BRAIN_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK, Items.FIRE_CORAL_BLOCK, Items.HORN_CORAL_BLOCK)
                .addTags(Tags.Items.GEMS_PRISMARINE, Tags.Items.DUSTS_PRISMARINE)
                .add(Items.PRISMARINE, Items.DARK_PRISMARINE)
                .add(Items.CONDUIT);
        this.tag("essence/tree")
                .addTags(ItemTags.SAPLINGS)
                .add(Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS)
                .add(Items.OAK_LOG, Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.JUNGLE_LOG, Items.ACACIA_LOG, Items.DARK_OAK_LOG, Items.CRIMSON_STEM, Items.WARPED_STEM)
                .addTag(ItemTags.LEAVES)
                .add(Items.NETHER_WART_BLOCK, Items.WARPED_WART_BLOCK);
        this.tag("essence/food")
                .add(Items.APPLE, Items.BREAD)
                .add(Items.PORKCHOP, Items.BEEF, Items.CHICKEN, Items.MUTTON, Items.RABBIT)
                .add(Items.COOKED_PORKCHOP, Items.COOKED_BEEF, Items.COOKED_CHICKEN, Items.COOKED_MUTTON, Items.COOKED_RABBIT)
                .add(Items.DRIED_KELP, Items.COOKED_COD, Items.COOKED_SALMON)
                .add(Items.CARROT, Items.POTATO, Items.BEETROOT, Items.MELON, Items.PUMPKIN, Items.SWEET_BERRIES/*, Items.GLOW_BERRIES*/)
                .add(Items.BAKED_POTATO, Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP)
                .add(Items.COOKIE, Items.PUMPKIN_PIE)
                .add(Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.POISONOUS_POTATO, Items.CHORUS_FRUIT)
                .add(Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.CAKE)
                .addOptionalTag((identifier("forge", "foods")));
        this.tag("essence/motion")
                .add(Items.FEATHER, Items.COMPASS, Items.MAP, Items.ICE, Items.PACKED_ICE, Items.BLUE_ICE, Items.LADDER)
                .addTag(ItemTags.BOATS)
                .add(Items.MINECART, Items.CHEST_MINECART, Items.FURNACE_MINECART, Items.HOPPER_MINECART, Items.TNT_MINECART)
                .addTag(ItemTags.RAILS)
                .add(Items.PISTON, Items.STICKY_PISTON, Items.SLIME_BLOCK, Items.HONEY_BLOCK)
                .add(Items.ENDER_PEARL, Items.ENDER_EYE);
        this.tag("essence/partition")
                .addTags(ItemTags.FENCES, ItemTags.WALLS, ItemTags.DOORS, ItemTags.TRAPDOORS)
                .addTags(Tags.Items.GLASS_COLORLESS, Tags.Items.STAINED_GLASS);
        this.tag("essence/light")
                .add(Items.TORCH, Items.REDSTONE_TORCH, Items.SOUL_TORCH, Items.LANTERN, Items.SOUL_LANTERN)
                .add(Items.LAVA_BUCKET)
                .addTag(ItemTags.COALS)
                .addTag(ItemTags.CANDLES)
                .add(Items.FLINT_AND_STEEL, Items.GLOW_INK_SAC, Items.GLOW_ITEM_FRAME, Items.GLOW_LICHEN, Items.GLOW_BERRIES, Items.AMETHYST_CLUSTER, Items.SEA_PICKLE)
                .add(Items.JACK_O_LANTERN, Items.REDSTONE_LAMP, Items.SEA_LANTERN, Items.MAGMA_BLOCK, Items.GLOWSTONE, Items.SHROOMLIGHT, Items.END_ROD)
                .add(Items.CRYING_OBSIDIAN, Items.ENCHANTING_TABLE, Items.BEACON);
        this.tag("essence/shadow")
                .addTag(ItemTags.BEDS)
                .add(Items.PHANTOM_MEMBRANE, Items.GHAST_TEAR, Items.WITHER_SKELETON_SKULL, Items.SOUL_SAND, Items.SOUL_SOIL)
                .addTag(Tags.Items.GLASS_TINTED)
                .add(Items.NETHER_BRICKS, Items.STONE_BRICKS, Items.MOSSY_STONE_BRICKS, Items.IRON_BARS, Items.PURPUR_BLOCK, Items.OBSIDIAN)
                .add(Items.EXPERIENCE_BOTTLE, Items.FERMENTED_SPIDER_EYE, Items.TOTEM_OF_UNDYING, Items.END_CRYSTAL)
                .add(Items.SCULK_VEIN, Items.SCULK, Items.SCULK_SENSOR, Items.SCULK_SHRIEKER, Items.SCULK_SHRIEKER);
        this.tag("essence/battle")
                .add(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD)
                .add(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE)
                .add(Items.BOW, Items.CROSSBOW, Items.ARROW, Items.SPECTRAL_ARROW, Items.TRIDENT, Items.TNT)
                .add(Items.LEATHER_HELMET, Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET, Items.TURTLE_HELMET)
                .add(Items.LEATHER_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE)
                .add(Items.LEATHER_LEGGINGS, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS)
                .add(Items.LEATHER_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS)
                .add(Items.SHIELD);
        this.tag("essence/sound")
                .add(Items.NOTE_BLOCK, Items.JUKEBOX)
                .addTag(ItemTags.CREEPER_DROP_MUSIC_DISCS);
        this.tag("essence/work")
                .add(Items.CRAFTING_TABLE, Items.FURNACE, Items.BLAST_FURNACE, Items.SMOKER)
                .add(Items.CAULDRON, Items.BREWING_STAND, Items.COMPOSTER, Items.STONECUTTER, Items.GRINDSTONE, Items.LOOM, Items.FLETCHING_TABLE, Items.CARTOGRAPHY_TABLE, Items.SMITHING_TABLE)
                .addTag(ItemTags.ANVIL)
                .add(Items.CHEST, Items.TRAPPED_CHEST, Items.BARREL)
                .add(Items.SHULKER_BOX, Items.BLACK_SHULKER_BOX, Items.BLUE_SHULKER_BOX, Items.BROWN_SHULKER_BOX, Items.CYAN_SHULKER_BOX, Items.GRAY_SHULKER_BOX, Items.GREEN_SHULKER_BOX, Items.LIGHT_BLUE_SHULKER_BOX, Items.LIGHT_GRAY_SHULKER_BOX, Items.LIME_SHULKER_BOX, Items.MAGENTA_SHULKER_BOX, Items.ORANGE_SHULKER_BOX, Items.PINK_SHULKER_BOX, Items.PURPLE_SHULKER_BOX, Items.RED_SHULKER_BOX, Items.WHITE_SHULKER_BOX, Items.YELLOW_SHULKER_BOX)
                .add(Items.HOPPER, Items.DROPPER, Items.DISPENSER);
        this.tag("essence/drawing")
                .addTag(Tags.Items.DYES)
                .add(Items.WHITE_GLAZED_TERRACOTTA, Items.ORANGE_GLAZED_TERRACOTTA, Items.MAGENTA_GLAZED_TERRACOTTA, Items.LIGHT_BLUE_GLAZED_TERRACOTTA, Items.YELLOW_GLAZED_TERRACOTTA, Items.LIME_GLAZED_TERRACOTTA, Items.PINK_GLAZED_TERRACOTTA, Items.GRAY_GLAZED_TERRACOTTA, Items.LIGHT_GRAY_GLAZED_TERRACOTTA, Items.CYAN_GLAZED_TERRACOTTA, Items.PURPLE_GLAZED_TERRACOTTA, Items.BLUE_GLAZED_TERRACOTTA, Items.BROWN_GLAZED_TERRACOTTA, Items.GREEN_GLAZED_TERRACOTTA, Items.RED_GLAZED_TERRACOTTA, Items.BLACK_GLAZED_TERRACOTTA)
                .add(Items.FIREWORK_ROCKET, Items.FIREWORK_STAR)
                .add(Items.NAME_TAG, Items.WRITABLE_BOOK, Items.BOOKSHELF, Items.LECTERN)
                .add(Items.FLOWER_BANNER_PATTERN, Items.CREEPER_BANNER_PATTERN, Items.SKULL_BANNER_PATTERN, Items.MOJANG_BANNER_PATTERN);
    }

    private IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> tag(String name) {
        return this.tag(ItemTags.create(identifier(HYPERDAIMC, name)));
    }
}
