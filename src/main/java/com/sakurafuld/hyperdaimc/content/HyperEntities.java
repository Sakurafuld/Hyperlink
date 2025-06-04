package com.sakurafuld.hyperdaimc.content;

import com.sakurafuld.hyperdaimc.content.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.fumetsu.skull.FumetsuSkull;
import com.sakurafuld.hyperdaimc.content.fumetsu.squall.FumetsuSquall;
import com.sakurafuld.hyperdaimc.content.fumetsu.storm.FumetsuStorm;
import com.sakurafuld.hyperdaimc.content.fumetsu.storm.FumetsuStormSkull;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.hyperdaimc.helper.Deets.*;

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
                        .sized(0.9F, 3.5F)
                        .clientTrackingRange(10)
                        .noSummon()
                        .setCustomClientFactory((packet, level) -> {
                            FumetsuEntity fumetsu = new FumetsuEntity(HyperEntities.FUMETSU.get(), level);
                            fumetsu.setMovable(true);
                            fumetsu.setPacketCoordinates(packet.getPosX(), packet.getPosY(), packet.getPosZ());
                            fumetsu.absMoveTo(packet.getPosX(), packet.getPosY(), packet.getPosZ(), (packet.getYaw() * 360) / 256f, (packet.getPitch() * 360) / 256f);
                            fumetsu.setYHeadRot((packet.getHeadYaw() * 360) / 256f);
                            fumetsu.setYBodyRot((packet.getHeadYaw() * 360) / 256f);
                            fumetsu.setMovable(false);
                            LOG.debug("clientCustomSpawnFumetsu");
                            return fumetsu;
                        })
                        .build(identifier(HYPERDAIMC, "fumetsu").toString()));

        FUMETSU_SKULL = REGISTRY.register("fumetsu_skull", () ->
                EntityType.Builder.of(FumetsuSkull::new, MobCategory.MISC)
                        .sized(0.5f, 0.5f)
                        .clientTrackingRange(4)
                        .updateInterval(10)
                        .noSummon()
                        .setCustomClientFactory((packet, level) -> {
                            FumetsuSkull skull = new FumetsuSkull(HyperEntities.FUMETSU_SKULL.get(), level);
                            skull.setMovable(true);
                            skull.setPacketCoordinates(packet.getPosX(), packet.getPosY(), packet.getPosZ());
                            skull.absMoveTo(packet.getPosX(), packet.getPosY(), packet.getPosZ(), (packet.getYaw() * 360) / 256f, (packet.getPitch() * 360) / 256f);
                            skull.setYHeadRot((packet.getHeadYaw() * 360) / 256f);
                            skull.setYBodyRot((packet.getHeadYaw() * 360) / 256f);
                            skull.setMovable(false);
                            return skull;
                        })
                        .build(identifier(HYPERDAIMC, "fumetsu_skull").toString()));

        FUMETSU_STORM = REGISTRY.register("fumetsu_storm", () ->
                EntityType.Builder.of(FumetsuStorm::new, MobCategory.MISC)
                        .sized(0.5f, 0.5f)
                        .updateInterval(Integer.MAX_VALUE)
                        .noSummon()
                        .setCustomClientFactory((packet, level) -> {
                            FumetsuStorm storm = new FumetsuStorm(HyperEntities.FUMETSU_STORM.get(), level);
                            storm.setMovable(true);
                            storm.setPacketCoordinates(packet.getPosX(), packet.getPosY(), packet.getPosZ());
                            storm.absMoveTo(packet.getPosX(), packet.getPosY(), packet.getPosZ(), (packet.getYaw() * 360) / 256f, (packet.getPitch() * 360) / 256f);
                            storm.setYHeadRot((packet.getHeadYaw() * 360) / 256f);
                            storm.setYBodyRot((packet.getHeadYaw() * 360) / 256f);
                            storm.setMovable(false);
                            return storm;
                        })
                        .build(identifier(HYPERDAIMC, "fumetsu_storm").toString()));

        FUMETSU_STORM_SKULL = REGISTRY.register("fumetsu_storm_skull", () ->
                EntityType.Builder.of(FumetsuStormSkull::new, MobCategory.MISC)
                        .sized(0.5f, 0.5f)
                        .clientTrackingRange(4)
                        .updateInterval(10)
                        .noSummon()
                        .setCustomClientFactory((packet, level) -> {
                            FumetsuStormSkull stormSkull = new FumetsuStormSkull(HyperEntities.FUMETSU_STORM_SKULL.get(), level);
                            stormSkull.setMovable(true);
                            stormSkull.setPacketCoordinates(packet.getPosX(), packet.getPosY(), packet.getPosZ());
                            stormSkull.absMoveTo(packet.getPosX(), packet.getPosY(), packet.getPosZ(), (packet.getYaw() * 360) / 256f, (packet.getPitch() * 360) / 256f);
                            stormSkull.setYHeadRot((packet.getHeadYaw() * 360) / 256f);
                            stormSkull.setYBodyRot((packet.getHeadYaw() * 360) / 256f);
                            stormSkull.setMovable(true);
                            return stormSkull;
                        })
                        .build(identifier(HYPERDAIMC, "fumetsu_storm_skull").toString()));

        FUMETSU_SQUALL = REGISTRY.register("fumetsu_squall", () ->
                EntityType.Builder.of(FumetsuSquall::new, MobCategory.MISC)
                        .sized(0.75f, 0.75f)
                        .clientTrackingRange(4)
                        .updateInterval(10)
                        .noSummon()
                        .setCustomClientFactory((packet, level) -> {
                            FumetsuSquall squall = new FumetsuSquall(HyperEntities.FUMETSU_SQUALL.get(), level);
                            squall.setMovable(true);
                            squall.setPacketCoordinates(packet.getPosX(), packet.getPosY(), packet.getPosZ());
                            squall.absMoveTo(packet.getPosX(), packet.getPosY(), packet.getPosZ(), (packet.getYaw() * 360) / 256f, (packet.getPitch() * 360) / 256f);
                            squall.setYHeadRot((packet.getHeadYaw() * 360) / 256f);
                            squall.setYBodyRot((packet.getHeadYaw() * 360) / 256f);
                            squall.setMovable(false);
                            return squall;
                        })
                        .build(identifier(HYPERDAIMC, "fumetsu_squall").toString()));
    }
}