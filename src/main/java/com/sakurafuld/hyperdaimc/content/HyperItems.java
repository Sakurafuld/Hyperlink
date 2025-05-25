package com.sakurafuld.hyperdaimc.content;

import com.sakurafuld.hyperdaimc.HyperSetup;
import com.sakurafuld.hyperdaimc.api.content.AbstractGashatItem;
import com.sakurafuld.hyperdaimc.content.chronicle.ChronicleItem;
import com.sakurafuld.hyperdaimc.content.fumetsu.FumetsuItem;
import com.sakurafuld.hyperdaimc.content.gameorb.GameOrbItem;
import com.sakurafuld.hyperdaimc.content.muteki.MutekiItem;
import com.sakurafuld.hyperdaimc.content.novel.NovelItem;
import com.sakurafuld.hyperdaimc.content.paradox.ParadoxItem;
import com.sakurafuld.hyperdaimc.content.vrx.VRXItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.sakurafuld.hyperdaimc.helper.Deets.*;

public class HyperItems {
    public static final DeferredRegister<Item> REGISTRY
            = DeferredRegister.create(ForgeRegistries.ITEMS, HYPERDAIMC);

    public static final RegistryObject<Item> GAME_ORB;

    public static final RegistryObject<Item> MUTEKI;
    public static final RegistryObject<Item> NOVEL;
    public static final RegistryObject<Item> CHRONICLE;
    public static final RegistryObject<Item> PARADOX;
    public static final RegistryObject<Item> VRX;

    public static final RegistryObject<Item> FUMETSU;

    static {

        GAME_ORB = register("game_orb", GameOrbItem::new);

        MUTEKI = registerGashat("muteki", MutekiItem::new);
        NOVEL = registerGashat("novel", NovelItem::new);
        CHRONICLE = registerGashat("chronicle", ChronicleItem::new);
        PARADOX = registerGashat("paradox", ParadoxItem::new);
        VRX = registerGashat("vrx", VRXItem::new);

        FUMETSU = register("fumetsu", FumetsuItem::new);
    }

    public static RegistryObject<Item> register(String name) {
        return register(name, new Item.Properties());
    }

    public static RegistryObject<Item> register(String name, Item.Properties prop) {
        return REGISTRY.register(name, () -> new Item(prop));
    }

    public static RegistryObject<Item> register(String name, Function<Item.Properties, ? extends Item> func) {
        return REGISTRY.register(name, () -> func.apply(new Item.Properties()));
    }

    public static RegistryObject<Item> registerGashat(String name, BiFunction<String, Item.Properties, ? extends AbstractGashatItem> func) {
        require(LogicalSide.CLIENT).run(() ->
                HyperSetup.specialModels.add(identifier(HYPERDAIMC, "special/" + name)));
        return REGISTRY.register(name, () -> func.apply(name, new Item.Properties()));
    }
}
