package com.sakurafuld.hyperdaimc.addon.botania;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXRegistration;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXRegistry;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXType;
import com.sakurafuld.hyperdaimc.infrastructure.addon.AddonMod;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.BOTANIA;

@AddonMod(BOTANIA)
public class HyperBotania {
    private static VRXType vrxMana;

    public HyperBotania() {
        VRXRegistry.registerRegistrant(this::registerVRX);
    }

    private void registerVRX(VRXRegistration registration) {
        vrxMana = registration.registerType("botania:mana", VRXOneMana::new, VRXOneMana::check, false, 300);
        registration.registerConverter(VRXOneMana::convert);
        registration.registerCollector(VRXOneMana::collect);
    }

    public static VRXType vrxMana() {
        assert vrxMana != null;
        return vrxMana;
    }
}
