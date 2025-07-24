package com.sakurafuld.hyperdaimc.content;

import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskBlock;
import com.sakurafuld.hyperdaimc.content.crafting.skull.FumetsuSkullBlock;
import com.sakurafuld.hyperdaimc.content.crafting.skull.FumetsuSkullWallBlock;
import com.sakurafuld.hyperdaimc.content.crafting.soul.SoulBlock;
import com.sakurafuld.hyperdaimc.content.over.materializer.MaterializerBlock;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

public class HyperBlocks {
    public static final DeferredRegister<Block> REGISTRY
            = DeferredRegister.create(ForgeRegistries.BLOCKS, HYPERDAIMC);

    public static final RegistryObject<SoulBlock> SOUL;
    public static final RegistryObject<DeskBlock> DESK;
    public static final RegistryObject<MaterializerBlock> MATERIALIZER;
    public static final RegistryObject<FumetsuSkullBlock> FUMETSU_SKULL;
    public static final RegistryObject<FumetsuSkullWallBlock> FUMETSU_WALL_SKULL;
    public static final RegistryObject<FumetsuSkullBlock> FUMETSU_RIGHT;
    public static final RegistryObject<FumetsuSkullWallBlock> FUMETSU_WALL_RIGHT;
    public static final RegistryObject<FumetsuSkullBlock> FUMETSU_LEFT;
    public static final RegistryObject<FumetsuSkullWallBlock> FUMETSU_WALL_LEFT;

    static {
        SOUL = registerCrafting("soul", () -> new SoulBlock(BlockBehaviour.Properties.of().mapColor(DyeColor.WHITE).sound(SoundType.AMETHYST).strength(0.5f, 600).noOcclusion().lightLevel(state -> 15)), properties -> properties.rarity(Rarity.UNCOMMON));

        DESK = registerCrafting("desk", () -> new DeskBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).sound(SoundType.BONE_BLOCK).strength(1.5f, 0)));
        MATERIALIZER = registerCrafting("materializer", () -> new MaterializerBlock(BlockBehaviour.Properties.of().mapColor(DyeColor.CYAN).sound(SoundType.BONE_BLOCK).strength(1.5f)));

        FUMETSU_LEFT = REGISTRY.register("fumetsu_left_skull", () -> new FumetsuSkullBlock(FumetsuSkullBlock.LEFT, BlockBehaviour.Properties.of().strength(1).pushReaction(PushReaction.DESTROY)));
        FUMETSU_WALL_LEFT = REGISTRY.register("fumetsu_wall_left_skull", () -> new FumetsuSkullWallBlock(FumetsuSkullBlock.LEFT, BlockBehaviour.Properties.of().strength(1).lootFrom(FUMETSU_LEFT).pushReaction(PushReaction.DESTROY)));
        HyperItems.registerCrafting("fumetsu_left_skull", properties -> new StandingAndWallBlockItem(FUMETSU_LEFT.get(), FUMETSU_WALL_LEFT.get(), properties.rarity(Rarity.UNCOMMON), Direction.DOWN));
        FUMETSU_SKULL = REGISTRY.register("fumetsu_skull", () -> new FumetsuSkullBlock(FumetsuSkullBlock.CENTER, BlockBehaviour.Properties.of().strength(1).pushReaction(PushReaction.DESTROY)));
        FUMETSU_WALL_SKULL = REGISTRY.register("fumetsu_wall_skull", () -> new FumetsuSkullWallBlock(FumetsuSkullBlock.CENTER, BlockBehaviour.Properties.of().strength(1).lootFrom(FUMETSU_SKULL).pushReaction(PushReaction.DESTROY)));
        HyperItems.registerCrafting("fumetsu_skull", properties -> new StandingAndWallBlockItem(FUMETSU_SKULL.get(), FUMETSU_WALL_SKULL.get(), properties.rarity(Rarity.RARE), Direction.DOWN));
        FUMETSU_RIGHT = REGISTRY.register("fumetsu_right_skull", () -> new FumetsuSkullBlock(FumetsuSkullBlock.RIGHT, BlockBehaviour.Properties.of().strength(1).pushReaction(PushReaction.DESTROY)));
        FUMETSU_WALL_RIGHT = REGISTRY.register("fumetsu_wall_right_skull", () -> new FumetsuSkullWallBlock(FumetsuSkullBlock.RIGHT, BlockBehaviour.Properties.of().strength(1).lootFrom(FUMETSU_RIGHT).pushReaction(PushReaction.DESTROY)));
        HyperItems.registerCrafting("fumetsu_right_skull", properties -> new StandingAndWallBlockItem(FUMETSU_RIGHT.get(), FUMETSU_WALL_RIGHT.get(), properties.rarity(Rarity.UNCOMMON), Direction.DOWN));
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
