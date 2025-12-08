package com.sakurafuld.hyperdaimc.addon.irons_spellbooks;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXRegistration;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXRegistry;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXType;
import com.sakurafuld.hyperdaimc.infrastructure.addon.AddonMod;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.IRONS_SPELLS_N_SPELLBOOKS;

@AddonMod(IRONS_SPELLS_N_SPELLBOOKS)
public class HyperIronsSpellbooks {
    private static VRXType vrxMana;

    public HyperIronsSpellbooks() {
        VRXRegistry.registerRegistrant(this::registerVRX);
    }

    private void registerVRX(VRXRegistration registration) {
        vrxMana = registration.registerType("irons_spellbooks:mana", VRXOneMana::new, VRXOneMana::check, false, 500);
        registration.registerConverter(VRXOneMana::convert);
        registration.registerCollector(VRXOneMana::collect);
    }

    public static VRXType vrxMana() {
        assert vrxMana != null;
        return vrxMana;
    }
}
