package com.sakurafuld.hyperdaimc.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.HyperSetup;
import com.sakurafuld.hyperdaimc.content.crafting.chemical.ChemicalItem;
import com.sakurafuld.hyperdaimc.content.crafting.gameorb.GameOrbItem;
import com.sakurafuld.hyperdaimc.content.crafting.material.MaterialItem;
import com.sakurafuld.hyperdaimc.content.crafting.sigill.SigilItem;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleItem;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuItem;
import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiItem;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelItem;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxItem;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXItem;
import com.sakurafuld.hyperdaimc.infrastructure.item.AbstractGashatItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class HyperItems {
    public static final DeferredRegister<Item> REGISTRY
            = DeferredRegister.create(ForgeRegistries.ITEMS, HYPERDAIMC);
    public static final List<RegistryObject<Item>> MAIN = Lists.newArrayList();
    public static final List<RegistryObject<Item>> CRAFTING = Lists.newArrayList();

    public static final RegistryObject<Item> MUTEKI;
    public static final RegistryObject<Item> NOVEL;
    public static final RegistryObject<Item> CHRONICLE;
    public static final RegistryObject<Item> PARADOX;
    public static final RegistryObject<Item> VRX;
    public static final RegistryObject<Item> FUMETSU;
    public static final RegistryObject<Item> GAME_ORB;
    public static final RegistryObject<Item> GOD_SIGIL;
    public static final RegistryObject<Item> CHEMICAL_MAX;
    public static final ImmutableList<RegistryObject<Item>> BUG_STARS;
    public static final Object2ObjectOpenHashMap<String, RegistryObject<Item>> MATERIAL = new Object2ObjectOpenHashMap<>();


    static {
        MUTEKI = registerGashat("muteki", MutekiItem::new);
        NOVEL = registerGashat("novel", NovelItem::new);
        CHRONICLE = registerGashat("chronicle", ChronicleItem::new);
        PARADOX = registerGashat("paradox", ParadoxItem::new);
        VRX = registerGashat("vrx", VRXItem::new);
        FUMETSU = registerMain("fumetsu", FumetsuItem::new);

        GAME_ORB = registerCrafting("game_orb", GameOrbItem::new);
        GOD_SIGIL = registerCrafting("god_sigil", SigilItem::new);
        CHEMICAL_MAX = registerCrafting("chemical_max", ChemicalItem::new);

        ImmutableList.Builder<RegistryObject<Item>> bugStars = new ImmutableList.Builder<>();
        bugStars.add(registerCrafting("bug_star", new Item.Properties().rarity(Rarity.UNCOMMON)));
        bugStars.add(registerCrafting("bug_star_zwei", new Item.Properties().rarity(Rarity.RARE)));
        bugStars.add(registerCrafting("bug_star_drei", new Item.Properties().rarity(Rarity.EPIC)));
        BUG_STARS = bugStars.build();

        registerEssence("ground", 0x7F500B);
        registerEssence("crust", 0x7F7F7F);
        registerEssence("mineral", 0x00A0DF);
        registerEssence("herb", 0x7FFF00);
        registerEssence("tree", 0x409040);
        registerEssence("marine", 0x0045FF);
        registerEssence("food", 0xFF4000);
        registerEssence("motion", 0xC0C0C0);
        registerEssence("partition", 0x8B008B);
        registerEssence("light", 0xFFFF8B);
        registerEssence("shadow", 0x303030);
        registerEssence("battle", 0x901700);
        registerEssence("sound", 0xDEFF22);
        registerEssence("work", 0x80A090);
        registerEssence("drawing", 0xC0EF8F);

        registerCore("land", 0xF0BF00, 0x00FF45);
        registerCore("cave", 0x30C0F0, 0x8B8B8B);
        registerCore("forest", 0x40F79D, 0x4DC500);
        registerCore("garden", 0xFFC0E0, 0xFAFFEF);
        registerCore("wind", 0xDDFFF6, 0xFFFFA0);
        registerCore("thunder", 0xFFE100, 0xF6FFEA);
        registerCore("treasure", 0xFFEA00, 0x886000);
        registerCore("flame", 0xF0301A, 0xDA4A00);
        registerCore("frost", 0xF0F8FF, 0x90D0FF);
        registerCore("animal", 0xFF0A6A, 0xFF60EB);
        registerCore("monster", 0xFF0A6A, 0x4A0F80);
        registerCore("amusement", 0xD0DA00, 0xDD0000);
        registerCore("order", 0xEA3EA3, 0x2047FF);
        registerCore("healing", 0xD8FF80, 0xFF60BE);
        registerCore("echo", 0x00903A, 0x000C6C);
        registerCore("death", 0x252564, 0x481697);
        registerCore("wonder", 0x926BFF, 0x00B9FF);

        registerGist("contraption", 0xEDCFB9, 0xF3FF35, 0x8D5400);
        registerGist("sky", 0xBAF5FF, 0x30A0FF, 0xF8FAFF);
        registerGist("love", 0x53FF00, 0xA87000, 0xFF00DF);
        registerGist("fear", 0x2800D0, 0xe0DCFF, 0x060030);
        registerGist("adventure", 0xDAE0FF, 0xFF6900, 0xFF4900);
        registerGist("taint", 0x60509D, 0x6600CC, 0x0200CC);
        registerGist("destruction", 0x9B334B, 0x7B339B, 0x8B6050);
        registerGist("leaping", 0xEAFFA6, 0xDCFF88, 0xA0F7FF);
        registerGist("fairy", 0xB070FF, 0xDF4FFF, 0xFFF4F4);
    }

    public static RegistryObject<Item> register(String name, Item.Properties prop) {
        return REGISTRY.register(name, () -> new Item(prop));
    }

    public static RegistryObject<Item> registerMain(String name, Item.Properties prop) {
        return Util.make(register(name, prop), MAIN::add);
    }

    public static RegistryObject<Item> registerCrafting(String name, Item.Properties prop) {
        return Util.make(register(name, prop), CRAFTING::add);
    }

    public static RegistryObject<Item> registerMain(String name, Function<Item.Properties, ? extends Item> func) {
        return Util.make(REGISTRY.register(name, () -> func.apply(new Item.Properties())), MAIN::add);
    }

    public static RegistryObject<Item> registerCrafting(String name, Function<Item.Properties, ? extends Item> func) {
        return Util.make(REGISTRY.register(name, () -> func.apply(new Item.Properties())), CRAFTING::add);
    }

    public static RegistryObject<Item> registerGashat(String name, BiFunction<String, Item.Properties, ? extends AbstractGashatItem> func) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HyperSetup.specialModels.add(identifier("special/" + name)));
        return Util.make(REGISTRY.register(name, () -> func.apply(name, new Item.Properties())), MAIN::add);
    }

    public static RegistryObject<Item> registerMaterial(String base, String suffix, Consumer<Item.Properties> property, boolean scaling, boolean coloring, boolean rotation, boolean particle, int... tint) {
        String name = base + "_" + suffix;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HyperSetup.specialModels.add(identifier("special/" + name)));
        RegistryObject<Item> object = registerCrafting(name, properties -> new MaterialItem(name, Util.make(properties, property), scaling, coloring, rotation, particle, tint));
        MATERIAL.put(name, object);
        return object;
    }

    public static RegistryObject<Item> registerEssence(String name, int tint0) {
        return registerMaterial(name, "essence", properties -> properties.rarity(Rarity.UNCOMMON), false, false, true, false, tint0);
    }

    public static RegistryObject<Item> registerCore(String name, int tint0, int tint1) {
        return registerMaterial(name, "core", properties -> properties.rarity(Rarity.RARE), true, false, false, false, tint0, tint1);
    }

    public static RegistryObject<Item> registerGist(String name, int tint0, int tint1, int tint2) {
        return registerMaterial(name, "gist", properties -> properties.rarity(Rarity.EPIC), false, true, false, false, tint0, tint1, tint2);
    }

    public static Item getMaterial(String base, String suffix) {
        return ForgeRegistries.ITEMS.getValue(identifier(base + "_" + suffix));
    }

    public static Item getEssence(String name) {
        return getMaterial(name, "essence");
    }

    public static Item getCore(String name) {
        return getMaterial(name, "core");
    }

    public static Item getGist(String name) {
        return getMaterial(name, "gist");
    }
}
