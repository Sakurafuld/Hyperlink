package com.sakurafuld.hyperdaimc.addon.ars_nouveau;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXRegistration;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXRegistry;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXType;
import com.sakurafuld.hyperdaimc.infrastructure.addon.AddonMod;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.ARS_NOUVEAU;

@AddonMod(ARS_NOUVEAU)
public class HyperArsNouveau {
    public static VRXType vrxSource;

    public HyperArsNouveau() {
        VRXRegistry.registerRegistrant(this::registerVRX);
    }

    private void registerVRX(VRXRegistration registration) {
        vrxSource = registration.registerType("ars_nouveau:source", VRXOneSource::new, VRXOneSource::check, false, 400);
        registration.registerConverter(VRXOneSource::convert);
        registration.registerCollector(VRXOneSource::collect);
    }

    public static VRXType vrxSource() {
        assert vrxSource != null;
        return vrxSource;
    }
}
