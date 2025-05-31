package com.sakurafuld.hyperdaimc.compat.tconstruct;

import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

public class HyperModifiers {
    public static ModifierDeferredRegister REGISTRY
            = ModifierDeferredRegister.create(HYPERDAIMC);

    public static final StaticModifier<NovelModifier> NOVEL;

    static {

        NOVEL = REGISTRY.register("novel", NovelModifier::new);

    }
}
