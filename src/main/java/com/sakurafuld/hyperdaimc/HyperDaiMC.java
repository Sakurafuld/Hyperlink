package com.sakurafuld.hyperdaimc;

import com.sakurafuld.hyperdaimc.content.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

@Mod(HYPERDAIMC)
public class HyperDaiMC {
    @SuppressWarnings("removal")
    public HyperDaiMC() {
        LOG.debug("hyperdaimc Wakeup");
        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
        IEventBus bus = context.getModEventBus();

        bus.register(new HyperSetup());

        HyperEntities.REGISTRY.register(bus);
        HyperItems.REGISTRY.register(bus);
        HyperMenus.REGISTRY.register(bus);
        HyperParticles.REGISTRY.register(bus);
        HyperSounds.REGISTRY.register(bus);
        HyperTabs.REGISTRY.register(bus);

        context.registerConfig(ModConfig.Type.COMMON, HyperCommonConfig.SPEC, HYPERDAIMC + "-common.toml");
        context.registerConfig(ModConfig.Type.SERVER, HyperServerConfig.SPEC, HYPERDAIMC + "-server.toml");
    }
}
