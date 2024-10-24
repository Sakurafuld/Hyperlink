package com.sakurafuld.hyperdaimc.content;

import com.sakurafuld.hyperdaimc.content.muteki.MutekiItem;
import com.sakurafuld.hyperdaimc.content.novel.NovelItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

import static com.sakurafuld.hyperdaimc.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.Deets.TAB;

public class ModItems {

    public static final DeferredRegister<Item> REGISTRY
            = DeferredRegister.create(ForgeRegistries.ITEMS, HYPERDAIMC);

    public static final RegistryObject<Item> MUTEKI;
    public static final RegistryObject<Item> NOVEL;


    static {

        MUTEKI = register("muteki", MutekiItem::new);
        NOVEL = register("novel", NovelItem::new);
    }

    public static RegistryObject<Item> register(String name){
        return register(name, new Item.Properties().tab(TAB));
    }
    public static RegistryObject<Item> register(String name, Item.Properties prop){
        return REGISTRY.register(name, ()-> new Item(prop));
    }
    public static RegistryObject<Item> register(String name, Function<Item.Properties, ? extends Item> func){
        return REGISTRY.register(name, () -> func.apply(new Item.Properties().tab(TAB)));
    }
}
