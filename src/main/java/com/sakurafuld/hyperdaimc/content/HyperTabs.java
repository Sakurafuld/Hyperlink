package com.sakurafuld.hyperdaimc.content;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

public class HyperTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY
            = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HYPERDAIMC);

    public static final RegistryObject<CreativeModeTab> MAIN;

    static {

        MAIN = REGISTRY.register("main", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.hyperdaimc.main"))
                .icon(HyperItems.MUTEKI.lazyMap(ItemStack::new))
                .displayItems(((pParameters, pOutput) -> HyperItems.REGISTRY.getEntries().forEach(object -> pOutput.accept(object.get()))))
                .build());

    }
}
