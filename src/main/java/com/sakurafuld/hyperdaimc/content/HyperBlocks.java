package com.sakurafuld.hyperdaimc.content;

import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskBlock;
import com.sakurafuld.hyperdaimc.content.crafting.skull.FumetsuSkullBlock;
import com.sakurafuld.hyperdaimc.content.crafting.skull.FumetsuSkullWallBlock;
import com.sakurafuld.hyperdaimc.content.crafting.soul.SoulBlock;
import net.minecraft.Util;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

public class HyperBlocks {
    public static final DeferredRegister<Block> REGISTRY
            = DeferredRegister.create(ForgeRegistries.BLOCKS, HYPERDAIMC);

    public static final RegistryObject<DeskBlock> DESK;
    public static final RegistryObject<SoulBlock> SOUL;
    public static final RegistryObject<FumetsuSkullBlock> FUMETSU_SKULL;
    public static final RegistryObject<FumetsuSkullWallBlock> FUMETSU_WALL_SKULL;
    public static final RegistryObject<FumetsuSkullBlock> FUMETSU_RIGHT;
    public static final RegistryObject<FumetsuSkullWallBlock> FUMETSU_WALL_RIGHT;
    public static final RegistryObject<FumetsuSkullBlock> FUMETSU_LEFT;
    public static final RegistryObject<FumetsuSkullWallBlock> FUMETSU_WALL_LEFT;

    static {
        DESK = registerCrafting("desk", () -> new DeskBlock(BlockBehaviour.Properties.of(DeskBlock.MATERIAL).sound(SoundType.BONE_BLOCK).strength(1.5f, 0)));
        SOUL = registerCrafting("soul", () -> new SoulBlock(BlockBehaviour.Properties.of(Material.SAND).sound(SoundType.AMETHYST).strength(0.5f, 600).noOcclusion().lightLevel(state -> 15)), properties -> properties.rarity(Rarity.UNCOMMON));

        FUMETSU_LEFT = REGISTRY.register("fumetsu_left_skull", () -> new FumetsuSkullBlock(FumetsuSkullBlock.LEFT, BlockBehaviour.Properties.of(Material.DECORATION).strength(1)));
        FUMETSU_WALL_LEFT = REGISTRY.register("fumetsu_wall_left_skull", () -> new FumetsuSkullWallBlock(FumetsuSkullBlock.LEFT, BlockBehaviour.Properties.of(Material.DECORATION).strength(1).lootFrom(FUMETSU_LEFT)));
        HyperItems.registerCrafting("fumetsu_left_skull", properties -> new StandingAndWallBlockItem(FUMETSU_LEFT.get(), FUMETSU_WALL_LEFT.get(), properties.rarity(Rarity.UNCOMMON)));
        FUMETSU_SKULL = REGISTRY.register("fumetsu_skull", () -> new FumetsuSkullBlock(FumetsuSkullBlock.CENTER, BlockBehaviour.Properties.of(Material.DECORATION).strength(1)));
        FUMETSU_WALL_SKULL = REGISTRY.register("fumetsu_wall_skull", () -> new FumetsuSkullWallBlock(FumetsuSkullBlock.CENTER, BlockBehaviour.Properties.of(Material.DECORATION).strength(1).lootFrom(FUMETSU_SKULL)));
        HyperItems.registerCrafting("fumetsu_skull", properties -> new StandingAndWallBlockItem(FUMETSU_SKULL.get(), FUMETSU_WALL_SKULL.get(), properties.rarity(Rarity.RARE)));
        FUMETSU_RIGHT = REGISTRY.register("fumetsu_right_skull", () -> new FumetsuSkullBlock(FumetsuSkullBlock.RIGHT, BlockBehaviour.Properties.of(Material.DECORATION).strength(1)));
        FUMETSU_WALL_RIGHT = REGISTRY.register("fumetsu_wall_right_skull", () -> new FumetsuSkullWallBlock(FumetsuSkullBlock.RIGHT, BlockBehaviour.Properties.of(Material.DECORATION).strength(1).lootFrom(FUMETSU_RIGHT)));
        HyperItems.registerCrafting("fumetsu_right_skull", properties -> new StandingAndWallBlockItem(FUMETSU_RIGHT.get(), FUMETSU_WALL_RIGHT.get(), properties.rarity(Rarity.UNCOMMON)));
    }

    public static <T extends Block> RegistryObject<T> registerMain(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = REGISTRY.register(name, block);
        HyperItems.registerMain(name, properties -> new BlockItem(toReturn.get(), properties));
        return toReturn;
    }

    public static <T extends Block> RegistryObject<T> registerCrafting(String name, Supplier<T> block, Consumer<Item.Properties> prop) {
        RegistryObject<T> toReturn = REGISTRY.register(name, block);
        HyperItems.registerCrafting(name, properties -> new BlockItem(toReturn.get(), Util.make(properties, prop)));
        return toReturn;
    }

    public static <T extends Block> RegistryObject<T> registerCrafting(String name, Supplier<T> block) {
        return registerCrafting(name, block, properties -> {
        });
    }
}
