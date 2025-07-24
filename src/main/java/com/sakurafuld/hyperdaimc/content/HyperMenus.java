package com.sakurafuld.hyperdaimc.content;

import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskMenu;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

public class HyperMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY
            = DeferredRegister.create(ForgeRegistries.CONTAINERS, HYPERDAIMC);

    public static final RegistryObject<MenuType<VRXMenu>> VRX;
    public static final RegistryObject<MenuType<DeskMenu>> DESK;

    static {
        VRX = register("vrx", VRXMenu::new);
        DESK = register("desk", DeskMenu::new);
    }

    public static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> register(String id, IContainerFactory<T> factory) {
        return REGISTRY.register(id, () -> IForgeMenuType.create(factory));
    }
}
