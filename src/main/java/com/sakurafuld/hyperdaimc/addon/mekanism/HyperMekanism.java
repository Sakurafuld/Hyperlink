package com.sakurafuld.hyperdaimc.addon.mekanism;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXRegistration;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXRegistry;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXType;
import com.sakurafuld.hyperdaimc.infrastructure.addon.AddonMod;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.MEKANISM;

@AddonMod(MEKANISM)
public class HyperMekanism {
    private static VRXType vrxGas;

    public HyperMekanism() {
        VRXRegistry.registerRegistrant(this::registerVRX);
    }

    private void registerVRX(VRXRegistration event) {
        vrxGas = event.registerType("mekanism:gas", VRXOneGas::new, VRXOneGas::check, false, 200);
        event.registerConverter(VRXOneGas::convert);
        event.registerCollector(VRXOneGas::collect);
        event.registerCaster(VRXOneGas::cast);
    }

    public static VRXType vrxGas() {
        assert vrxGas != null;
        return vrxGas;
    }
}
