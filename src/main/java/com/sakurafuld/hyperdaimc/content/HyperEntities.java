package com.sakurafuld.hyperdaimc.content;

import com.sakurafuld.hyperdaimc.content.crafting.chemical.ChemicalEntity;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.skull.FumetsuSkull;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.squall.FumetsuSquall;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.storm.FumetsuStorm;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.storm.FumetsuStormSkull;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

public class HyperEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HYPERDAIMC);

    public static final RegistryObject<EntityType<FumetsuEntity>> FUMETSU;
    public static final RegistryObject<EntityType<FumetsuSkull>> FUMETSU_SKULL;
    public static final RegistryObject<EntityType<FumetsuStorm>> FUMETSU_STORM;
    public static final RegistryObject<EntityType<FumetsuStormSkull>> FUMETSU_STORM_SKULL;
    public static final RegistryObject<EntityType<FumetsuSquall>> FUMETSU_SQUALL;
    public static final RegistryObject<EntityType<ChemicalEntity>> CHEMICAL_MAX;

    static {
        FUMETSU = register("fumetsu", () ->
                EntityType.Builder.of(FumetsuEntity::new, MobCategory.MONSTER)
                        .sized(0.9F, 3.5F)
                        .clientTrackingRange(10)
                        .noSummon()
                        .setCustomClientFactory((packet, level) -> {
                            FumetsuEntity fumetsu = new FumetsuEntity(HyperEntities.FUMETSU.get(), level);
                            fumetsu.setMovable(true);
                            fumetsu.syncPacketPositionCodec(packet.getPosX(), packet.getPosY(), packet.getPosZ());
                            fumetsu.absMoveTo(packet.getPosX(), packet.getPosY(), packet.getPosZ(), (packet.getYaw() * 360) / 256f, (packet.getPitch() * 360) / 256f);
                            fumetsu.setYHeadRot((packet.getHeadYaw() * 360) / 256f);
                            fumetsu.setYBodyRot((packet.getHeadYaw() * 360) / 256f);
                            fumetsu.setMovable(false);
                            return fumetsu;
                        }));

        FUMETSU_SKULL = register("fumetsu_skull", () ->
                EntityType.Builder.of(FumetsuSkull::new, MobCategory.MISC)
                        .sized(0.5f, 0.5f)
                        .clientTrackingRange(4)
                        .updateInterval(10)
                        .noSummon()
                        .setCustomClientFactory((packet, level) -> {
                            FumetsuSkull skull = new FumetsuSkull(HyperEntities.FUMETSU_SKULL.get(), level);
                            skull.setMovable(true);
                            skull.syncPacketPositionCodec(packet.getPosX(), packet.getPosY(), packet.getPosZ());
                            skull.absMoveTo(packet.getPosX(), packet.getPosY(), packet.getPosZ(), (packet.getYaw() * 360) / 256f, (packet.getPitch() * 360) / 256f);
                            skull.setYHeadRot((packet.getHeadYaw() * 360) / 256f);
                            skull.setYBodyRot((packet.getHeadYaw() * 360) / 256f);
                            skull.setMovable(false);
                            return skull;
                        }));

        FUMETSU_STORM = register("fumetsu_storm", () ->
                EntityType.Builder.of(FumetsuStorm::new, MobCategory.MISC)
                        .sized(0.5f, 0.5f)
                        .updateInterval(Integer.MAX_VALUE)
                        .noSummon()
                        .setCustomClientFactory((packet, level) -> {
                            FumetsuStorm storm = new FumetsuStorm(HyperEntities.FUMETSU_STORM.get(), level);
                            storm.setMovable(true);
                            storm.syncPacketPositionCodec(packet.getPosX(), packet.getPosY(), packet.getPosZ());
                            storm.absMoveTo(packet.getPosX(), packet.getPosY(), packet.getPosZ(), (packet.getYaw() * 360) / 256f, (packet.getPitch() * 360) / 256f);
                            storm.setYHeadRot((packet.getHeadYaw() * 360) / 256f);
                            storm.setYBodyRot((packet.getHeadYaw() * 360) / 256f);
                            storm.setMovable(false);
                            return storm;
                        }));

        FUMETSU_STORM_SKULL = register("fumetsu_storm_skull", () ->
                EntityType.Builder.of(FumetsuStormSkull::new, MobCategory.MISC)
                        .sized(0.5f, 0.5f)
                        .clientTrackingRange(4)
                        .updateInterval(10)
                        .noSummon()
                        .setCustomClientFactory((packet, level) -> {
                            FumetsuStormSkull stormSkull = new FumetsuStormSkull(HyperEntities.FUMETSU_STORM_SKULL.get(), level);
                            stormSkull.setMovable(true);
                            stormSkull.syncPacketPositionCodec(packet.getPosX(), packet.getPosY(), packet.getPosZ());
                            stormSkull.absMoveTo(packet.getPosX(), packet.getPosY(), packet.getPosZ(), (packet.getYaw() * 360) / 256f, (packet.getPitch() * 360) / 256f);
                            stormSkull.setYHeadRot((packet.getHeadYaw() * 360) / 256f);
                            stormSkull.setYBodyRot((packet.getHeadYaw() * 360) / 256f);
                            stormSkull.setMovable(true);
                            return stormSkull;
                        }));

        FUMETSU_SQUALL = register("fumetsu_squall", () ->
                EntityType.Builder.of(FumetsuSquall::new, MobCategory.MISC)
                        .sized(0.75f, 0.75f)
                        .clientTrackingRange(4)
                        .updateInterval(10)
                        .noSummon()
                        .setCustomClientFactory((packet, level) -> {
                            FumetsuSquall squall = new FumetsuSquall(HyperEntities.FUMETSU_SQUALL.get(), level);
                            squall.setMovable(true);
                            squall.syncPacketPositionCodec(packet.getPosX(), packet.getPosY(), packet.getPosZ());
                            squall.absMoveTo(packet.getPosX(), packet.getPosY(), packet.getPosZ(), (packet.getYaw() * 360) / 256f, (packet.getPitch() * 360) / 256f);
                            squall.setYHeadRot((packet.getHeadYaw() * 360) / 256f);
                            squall.setYBodyRot((packet.getHeadYaw() * 360) / 256f);
                            squall.setMovable(false);
                            return squall;
                        }));
        CHEMICAL_MAX = register("chemical_max", () -> EntityType.Builder.of(ChemicalEntity::new, MobCategory.MISC)
                .sized(0.25f, 0.25f)
                .clientTrackingRange(4)
                .updateInterval(10));
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, Supplier<EntityType.Builder<T>> type) {
        return REGISTRY.register(name, () -> type.get().build(HYPERDAIMC + ":" + name));
    }
}
