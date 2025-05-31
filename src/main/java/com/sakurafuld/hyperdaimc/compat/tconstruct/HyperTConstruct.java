package com.sakurafuld.hyperdaimc.compat.tconstruct;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.sakurafuld.hyperdaimc.helper.Deets.TINKERSCONSTRUCT;
import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class HyperTConstruct {
    public HyperTConstruct(FMLJavaModLoadingContext context) {
        require(TINKERSCONSTRUCT).run(() -> {
            IEventBus bus = context.getModEventBus();

            HyperModifiers.REGISTRY.register(bus);
        });
    }
}
