package com.sakurafuld.hyperdaimc.addon.kubejs;

import com.sakurafuld.hyperdaimc.infrastructure.Deets;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.BindingsEvent;

public class HyperKubeJS extends KubeJSPlugin {
    @Override
    public void init() {
        Deets.LOG.debug("HyperKubeJS init");
        RegistryInfo.ITEM.addType("hyperdaimc:gashat", GashatItemJSBuilder.class, GashatItemJSBuilder::new);
        RegistryInfo.ITEM.addType("hyperdaimc:game_over", GameOverItemJSBuilder.class, GameOverItemJSBuilder::new);
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("HyperText", WritesJS.class);
    }
}
