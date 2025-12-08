package com.sakurafuld.hyperdaimc.addon.tconstruct;

import com.sakurafuld.hyperdaimc.infrastructure.addon.AddonMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.TINKERSCONSTRUCT;

@AddonMod(TINKERSCONSTRUCT)
public class HyperTConstruct {
    public HyperTConstruct(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();

        HyperModifiers.REGISTRY.register(bus);
    }
}
