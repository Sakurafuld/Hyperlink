package com.sakurafuld.hyperdaimc;

import com.sakurafuld.hyperdaimc.content.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

@Mod(HYPERDAIMC)
public class HyperDaiMC {
    public HyperDaiMC() {
        LOG.debug("hyperdaimc Wakeup");
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext ctx = ModLoadingContext.get();

        bus.register(new HyperSetup());

        HyperEntities.REGISTRY.register(bus);
        HyperItems.REGISTRY.register(bus);
        HyperMenus.REGISTRY.register(bus);
        HyperParticles.REGISTRY.register(bus);
        HyperSounds.REGISTRY.register(bus);

        ctx.registerConfig(ModConfig.Type.COMMON, HyperCommonConfig.SPEC, HYPERDAIMC + "-common.toml");
        ctx.registerConfig(ModConfig.Type.SERVER, HyperServerConfig.SPEC, HYPERDAIMC + "-server.toml");
    }
}
