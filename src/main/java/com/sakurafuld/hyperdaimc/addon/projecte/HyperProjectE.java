package com.sakurafuld.hyperdaimc.addon.projecte;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXRegistration;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXRegistry;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXType;
import com.sakurafuld.hyperdaimc.infrastructure.addon.AddonMod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.math.BigInteger;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.PROJECT_E;

@AddonMod(PROJECT_E)
public class HyperProjectE {
    private static BigInteger unit = BigInteger.valueOf(Long.MAX_VALUE);
    private static VRXType vrxEmc;

    public HyperProjectE(FMLJavaModLoadingContext context) {
        VRXRegistry.registerRegistrant(this::registerVRX);
        context.getModEventBus().addListener(this::configChanged);
    }

    private void registerVRX(VRXRegistration registration) {
        vrxEmc = registration.registerType("projecte:emc", VRXOneEmc::new, VRXOneEmc::check, false, 600);
        registration.registerConverter(VRXOneEmc::convert);
        registration.registerCollector(VRXOneEmc::collect);
    }

    private void configChanged(ModConfigEvent event) {
        if (HyperCommonConfig.SPEC == event.getConfig().getSpec())
            unit = new BigInteger(HyperCommonConfig.VRX_EMC_VALUE.get());
    }

    public static BigInteger unit() {
        return unit;
    }

    public static VRXType vrxEmc() {
        assert vrxEmc != null;
        return vrxEmc;
    }
}
