package com.sakurafuld.hyperdaimc.content;

import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskBlockEntity;
import com.sakurafuld.hyperdaimc.content.crafting.skull.FumetsuSkullBlockEntity;
import com.sakurafuld.hyperdaimc.content.over.materializer.MaterializerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

public class HyperBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HYPERDAIMC);

    public static final RegistryObject<BlockEntityType<DeskBlockEntity>> DESK;
    public static final RegistryObject<BlockEntityType<MaterializerBlockEntity>> MATERIALIZER;
    public static final RegistryObject<BlockEntityType<FumetsuSkullBlockEntity>> FUMETSU_SKULL;

    static {
        DESK = REGISTRY.register("desk", () -> BlockEntityType.Builder.of(DeskBlockEntity::new, HyperBlocks.DESK.get()).build(null));
        MATERIALIZER = REGISTRY.register("materializer", () -> BlockEntityType.Builder.of(MaterializerBlockEntity::new, HyperBlocks.MATERIALIZER.get()).build(null));

        FUMETSU_SKULL = REGISTRY.register("fumetsu_skull", () -> BlockEntityType.Builder.of(FumetsuSkullBlockEntity::new, HyperBlocks.FUMETSU_SKULL.get(), HyperBlocks.FUMETSU_WALL_SKULL.get(), HyperBlocks.FUMETSU_RIGHT.get(), HyperBlocks.FUMETSU_WALL_RIGHT.get(), HyperBlocks.FUMETSU_LEFT.get(), HyperBlocks.FUMETSU_WALL_LEFT.get()).build(null));
    }
}
