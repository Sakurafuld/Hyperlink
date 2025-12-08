package com.sakurafuld.hyperdaimc.addon.tconstruct;

import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

public class HyperModifiers {
    public static final StaticModifier<NovelModifier> NOVEL;
    public static final StaticModifier<ParadoxModifier> PARADOX;
    public static ModifierDeferredRegister REGISTRY
            = ModifierDeferredRegister.create(HYPERDAIMC);

    static {

        NOVEL = REGISTRY.register("novel", NovelModifier::new);
        PARADOX = REGISTRY.register("paradox", ParadoxModifier::new);

    }
}
