package com.sakurafuld.hyperdaimc.compat.mekanism;

import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXOne;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.sakurafuld.hyperdaimc.helper.Deets.MEKANISM;
import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class HyperMekanism {
    public HyperMekanism(FMLJavaModLoadingContext context) {
        require(MEKANISM).run(() ->
                VRXOneGas.TYPE = VRXOne.Type.register("gas", 20, VRXOneGas::new, VRXOneGas::convert, VRXOneGas::collect, VRXOneGas::check, VRXOneGas::cast));
    }
}
