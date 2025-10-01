package com.sakurafuld.hyperdaimc.compat.mekanism;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXOne;

import static com.sakurafuld.hyperdaimc.helper.Deets.MEKANISM;
import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class HyperMekanism {
    public HyperMekanism() {
        require(MEKANISM).run(() ->
                VRXOneGas.TYPE = VRXOne.Type.register("gas", 20, false, VRXOneGas::new, VRXOneGas::convert, VRXOneGas::collect, VRXOneGas::check, VRXOneGas::cast));
    }
}
