package com.sakurafuld.hyperdaimc.content;

import com.sakurafuld.hyperdaimc.content.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.fumetsu.squall.FumetsuSquall;
import com.sakurafuld.hyperdaimc.content.fumetsu.storm.FumetsuStorm;
import com.sakurafuld.hyperdaimc.content.fumetsu.skull.FumetsuSkull;
import com.sakurafuld.hyperdaimc.content.fumetsu.storm.FumetsuStormSkull;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

public class HyperEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.ENTITIES, HYPERDAIMC);

    public static final RegistryObject<EntityType<FumetsuEntity>> FUMETSU;
    public static final RegistryObject<EntityType<FumetsuSkull>> FUMETSU_SKULL;
    public static final RegistryObject<EntityType<FumetsuStorm>> FUMETSU_STORM;
    public static final RegistryObject<EntityType<FumetsuStormSkull>> FUMETSU_STORM_SKULL;
    public static final RegistryObject<EntityType<FumetsuSquall>> FUMETSU_SQUALL;

    static {
        FUMETSU = REGISTRY.register("fumetsu", () ->
                EntityType.Builder.of(FumetsuEntity::new, MobCategory.MONSTER)
                        .fireImmune()
                        .immuneTo(Blocks.WITHER_ROSE, Blocks.SWEET_BERRY_BUSH, Blocks.CACTUS, Blocks.POWDER_SNOW)
                        .sized(0.9F, 3.5F)
                        .clientTrackingRange(10)
                        .build(identifier(HYPERDAIMC, "fumetsu").toString()));

        FUMETSU_SKULL = REGISTRY.register("fumetsu_skull", () ->
                EntityType.Builder.of(FumetsuSkull::new, MobCategory.MISC)
                        .sized(0.5f, 0.5f)
                        .clientTrackingRange(4)
                        .updateInterval(10)
                        .noSummon()
                        .build(identifier(HYPERDAIMC, "fumetsu_skull").toString()));

        FUMETSU_STORM = REGISTRY.register("fumetsu_storm", () ->
                EntityType.Builder.of(FumetsuStorm::new, MobCategory.MISC)
                        .sized(0.5f, 0.5f)
                        .updateInterval(Integer.MAX_VALUE)
                        .noSummon()
                        .build(identifier(HYPERDAIMC, "fumetsu_storm").toString()));

        FUMETSU_STORM_SKULL = REGISTRY.register("fumetsu_storm_skull", () ->
                EntityType.Builder.of(FumetsuStormSkull::new, MobCategory.MISC)
                        .sized(0.5f, 0.5f)
                        .clientTrackingRange(4)
                        .updateInterval(10)
                        .noSummon()
                        .build(identifier(HYPERDAIMC, "fumetsu_storm_skull").toString()));

        FUMETSU_SQUALL = REGISTRY.register("fumetsu_squall", () ->
                EntityType.Builder.of(FumetsuSquall::new, MobCategory.MISC)
                        .sized(0.75f, 0.75f)
                        .clientTrackingRange(4)
                        .updateInterval(10)
                        .noSummon()
                        .build(identifier(HYPERDAIMC, "fumetsu_squall").toString()));
    }
}
