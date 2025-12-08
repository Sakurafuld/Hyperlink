package com.sakurafuld.hyperdaimc.datagen;

import com.google.common.collect.Maps;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.crafting.skull.FumetsuSkullWallBlock;
import net.minecraft.Util;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

public class HyperEnglishProvider extends LanguageProvider {
    private static final Map<String, String> MAP = Util.make(Maps.newHashMap(), map -> {
        map.put("zwei", "II");
        map.put("drei", "III");
        map.put("vrx", "VRX");
    });

    public HyperEnglishProvider(PackOutput output) {
        super(output, HYPERDAIMC, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add("itemGroup.hyperdaimc.main", "Hyperlink");
        this.add("itemGroup.hyperdaimc.crafting", "Hyperlink-Crafting");

        HyperItems.REGISTRY.getEntries().stream()
                .filter(item -> !(item.get() instanceof BlockItem))
                .forEach(item -> this.addItem(item, defaultName(item.get(), ForgeRegistries.ITEMS.getKey(item.get()))));
        HyperBlocks.REGISTRY.getEntries().stream()
                .filter(block -> !(block.get() instanceof FumetsuSkullWallBlock))
                .forEach(block -> this.addBlock(block, defaultName(block.get(), ForgeRegistries.BLOCKS.getKey(block.get()))));
        HyperEntities.REGISTRY.getEntries()
                .forEach(entity -> this.addEntityType(entity, defaultName(entity.get(), ForgeRegistries.ENTITY_TYPES.getKey(entity.get()))));

        this.add("subtitles.hyperdaimc.muteki_equip", "Pakkaaan!!!!");
        this.add("subtitles.hyperdaimc.novelize", "Critical Destiny!!!");
        this.add("subtitles.hyperdaimc.chronicle_select", "Pause Select");
        this.add("subtitles.hyperdaimc.chronicle_pause", "ZA WARUDO!!!");
        this.add("subtitles.hyperdaimc.chronicle_restart", "SOSHITETOKIHAUGOKIDASU!!!");
        this.add("subtitles.hyperdaimc.perfect_knockout", "Combo!!!");
        this.add("subtitles.hyperdaimc.paradox_select", "Select combo chain");
        this.add("subtitles.hyperdaimc.paradox_chain", "Chained");
        this.add("subtitles.hyperdaimc.paradox_unchain", "Unchained");
        this.add("subtitles.hyperdaimc.vrx_open", "Open V.R.X. menu");
        this.add("subtitles.hyperdaimc.vrx_create", "V!R!X!");
        this.add("subtitles.hyperdaimc.vrx_erase", "X!R!V!");
        this.add("subtitles.hyperdaimc.fumetsu_ambient", "Fumetsu Wither angers");
        this.add("subtitles.hyperdaimc.fumetsu_hurt", "Fumetsu Wither hurts");
        this.add("subtitles.hyperdaimc.fumetsu_shoot", "Fumetsu Wither attacks");
        this.add("subtitles.hyperdaimc.fumetsu_storm", "Fumetsu Storm explodes");
        this.add("subtitles.hyperdaimc.chemical_maximization", "Maximize soul");
        this.add("subtitles.hyperdaimc.soul", "Soul pulse");

        this.add("tooltip.hyperdaimc.show_description", "Hold [§fShift§7] for description");
        this.add("tooltip.hyperdaimc.muteki", "Hyper Muteki");
        this.add("tooltip.hyperdaimc.muteki.description", "§ePlace§7 this on §ehotbar§7 to become §6invincible");
        this.add("tooltip.hyperdaimc.muteki.curios_description", "§ePlace§7 this on §ehotbar§7 or §ecurios slot§7 to become §6invincible");
        this.add("tooltip.hyperdaimc.muteki.description.theft", "§eProtects§7 you from §eitem theft");
        this.add("tooltip.hyperdaimc.muteki.description.command", "§eProtects§7 you from §ecommand selectors");
        this.add("tooltip.hyperdaimc.muteki.description.novel", "Fully immune to Novelcalibur");
        this.add("tooltip.hyperdaimc.novel", "Mighty Novel");
        this.add("tooltip.hyperdaimc.novel.description", "§eL-Click§7 to §6kill§7 all nearby entities within your field of view");
        this.add("tooltip.hyperdaimc.novel.description.sneaking", "While §esneaking§7, click to target a §esingle entity");
        this.add("tooltip.hyperdaimc.novel.description_inverted", "§eL-Click§7 to §6kill§7 an entity you are looking at");
        this.add("tooltip.hyperdaimc.novel.description_inverted.sneaking", "While §esneaking§7, click to target §eall nearby entities§7 within your field of view");
        this.add("tooltip.hyperdaimc.novel.description.work", "Allow §erare loot§7 and §eexperience§7 drops, and §eEnchantment§7 abilities works");
        this.add("tooltip.hyperdaimc.novel.description.tconstruct", "Provides the §emodifier§7 for §9§oTinker's Construct§7 mod");
        this.add("tooltip.hyperdaimc.chronicle", "Rider Chronicle");
        this.add("tooltip.hyperdaimc.chronicle.description", "§eR-Click§7 at a start point and an end point to §6protect§7 that area");
        this.add("tooltip.hyperdaimc.chronicle.description.restart", "§eL-Click§7 at a protected area to unprotect it");
        this.add("tooltip.hyperdaimc.chronicle.description.free_owner", "§eOnly§7 the owner §ecan§7 interfere with protected areas");
        this.add("tooltip.hyperdaimc.chronicle.description.paused_owner", "§eEven§7 the owner §ecannot§7 interfere with protected areas");
        this.add("tooltip.hyperdaimc.chronicle.description.paradox", "Fully immune to Pickdox");
        this.add("tooltip.hyperdaimc.paradox", "Perfect Knockout");
        this.add("tooltip.hyperdaimc.paradox.r_click_to_clear", "§eR-Click§7 under the mouse to clear the internal items");
        this.add("tooltip.hyperdaimc.paradox.description", "§eL-Click§7 to §6destroy§7 blocks");
        this.add("tooltip.hyperdaimc.paradox.description.control", "While §esneaking§7, click to destroy blocks §eone by one");
        this.add("tooltip.hyperdaimc.paradox.description.control_inverted", "While §esneaking§7, click to §econtinuously§7 destroy blocks");
        this.add("tooltip.hyperdaimc.paradox.description.drop", "§eDropped items (or experiences)§7 go into your §einventory§7. if not, they go §einside§7 this item");
        this.add("tooltip.hyperdaimc.paradox.description.transport", "§eHold§7 and §ethrow§7 this to §etransport§7 the internal items to the targeted §eStorage block");
        this.add("tooltip.hyperdaimc.paradox.description.chain", "§eR-Click§7 at a start point and an end point to create §eCombo chain");
        this.add("tooltip.hyperdaimc.paradox.description.unchain", "§eR-Click§7 while §esneaking§7 at a §ecombo chain§7 to remove it");
        this.add("tooltip.hyperdaimc.vrx", "Creator VRX");
        this.add("tooltip.hyperdaimc.vrx.description", "§eR-CLick§7 on a block or a entity to open gui");
        this.add("tooltip.hyperdaimc.vrx.description.close", "§eClose§7 the gui to set §6V.R.X§7 as the target");
        this.add("tooltip.hyperdaimc.vrx.description.configurable", "Configurable: [%s]");
        this.add("tooltip.hyperdaimc.vrx.face", "Contents in %s of %s");
        this.add("tooltip.hyperdaimc.vrx.face.empty", " -Empty");
        this.add("tooltip.hyperdaimc.vrx.indexes", "%3$s %1$s / %2$s %4$s");
        this.add("tooltip.hyperdaimc.vrx.left", "←L-Click");
        this.add("tooltip.hyperdaimc.vrx.right", "R-Click→");
        this.add("tooltip.hyperdaimc.face.null", "None");
        this.add("tooltip.hyperdaimc.face.down", "Down");
        this.add("tooltip.hyperdaimc.face.up", "Up");
        this.add("tooltip.hyperdaimc.face.north", "North");
        this.add("tooltip.hyperdaimc.face.south", "South");
        this.add("tooltip.hyperdaimc.face.west", "West");
        this.add("tooltip.hyperdaimc.face.east", "East");
        this.add("tooltip.hyperdaimc.vrx.player", "Current V.R.X. contents");
        this.add("tooltip.hyperdaimc.vrx.energy", "Infinite Energy");
        this.add("tooltip.hyperdaimc.vrx.energy.description", "ForgeEnergy, RedstoneFlux, Joule, and more");
        this.add("tooltip.hyperdaimc.desk.minecrafting", "Mine to Craft!");
        this.add("tooltip.hyperdaimc.desk.lock", "Click to lock the current recipe");
        this.add("tooltip.hyperdaimc.desk.unlock", "Shift-click to clear the locked recipe");
        this.add("tooltip.hyperdaimc.desk.animation", "Hold Ctrl+Alt to disable animation");
        this.add("tooltip.hyperdaimc.god_sigil", "God loves you!");
        this.add("tooltip.hyperdaimc.chemical_max", "Dangerous for zombies!");
        this.add("tooltip.hyperdaimc.materializer.fuel", "Uses: %s");

        this.add("container.hyperdaimc.vrx.face", "Direction: %s");

        this.add("chat.hyperdaimc.config_warning", "In version 2.0, the hyperdaimc-server.toml config file has been integrated into hyperdaimc-common.toml\nThis warning can be disabled in hyperdaimc-common.toml");
        this.add("chat.hyperdaimc.chronicle.conflict", "Selection already exists");
        this.add("chat.hyperdaimc.chronicle.too_large", "Selection is too large");

        this.add("death.attack.novel.0", "%s was gone");
        this.add("death.attack.novel.1", "%s became an ant and was trampled");
        this.add("death.attack.novel.2", "%s became a dust and was blown away");
        this.add("death.attack.novel.3", "%s became a leaf and was torn up");
        this.add("death.attack.novel.4", "%s became a glass and was smashed up");
        this.add("death.attack.novel.5", "%s became a fire and burned out");
        this.add("death.attack.novel.6", "%s become empty");

        this.add("argument.muteki.notfound", "No §enon§c-§6Muteki entity was found");

        this.add("permissions.requires.muteki", "A §enon§c-§6Muteki entity is required to run this command here");

        // Integration.
        this.add("tooltip.hyperdaimc.vrx.botania_mana", "Infinite Mana");
        this.add("tooltip.hyperdaimc.vrx.botania_mana.description", "For Pools, Tablets in inventory, etc");
        this.add("tooltip.hyperdaimc.vrx.source", "Infinite Source");
        this.add("tooltip.hyperdaimc.vrx.source.description", "For Players, Jars, etc");
        this.add("tooltip.hyperdaimc.vrx.irons_spellbooks_mana", "Infinite Mana");
        this.add("tooltip.hyperdaimc.vrx.irons_spellbooks_mana.description", "For living entities");
        this.add("tooltip.hyperdaimc.vrx.emc", "Infinite EMC");
        this.add("tooltip.hyperdaimc.vrx.emc.description", "For Players, Condensers, Klein Stars in inventory, etc");

        this.add("curios.identifier.maximum", "Maximum");

        this.add("recipe.hyperdaimc.desk", "Gamacrafting");
        this.add("recipe.hyperdaimc.materializer", "Materialization");
        this.add("recipe.hyperdaimc.brewing", "Hyper Brewing");

        this.add("information.hyperdaimc.fumetsu_wither.0", """
                Place %2$s, %3$s, %4$s, and %5$s in the same way as summoning a Wither, and you can summon %7$s by holding %1$s and right-clicking while sneaking.
                %7$s is basically neutral and will not become hostile unless it takes damage (and must never become hostile).
                If you log out of the game, move to another dimension, or die, %7$s will leave %6$s behind and disappear from this world.""");


        this.add("modifier.hyperdaimc.novel", "Mighty Novel X");
        this.add("modifier.hyperdaimc.novel.flavor", "Critical Destiny!");
        this.add("modifier.hyperdaimc.novel.description", "All attacks kill the target instantly.");
        this.add("modifier.hyperdaimc.paradox", "Perfect-Knockout99");
        this.add("modifier.hyperdaimc.paradox.flavor", "50+50=99!");
        this.add("modifier.hyperdaimc.paradox.description", "Can instantly destroy any block.");
    }

    @Nullable
    private <T> String specialize(T entry, ResourceLocation name) {
        if (entry == HyperBlocks.DESK.get())
            return "Gamacrafter";
        if (entry == HyperBlocks.SOUL.get())
            return "Born Soul";
        if (entry == HyperItems.CHEMICAL_MAX.get() || entry == HyperEntities.CHEMICAL_MAX.get())
            return "Chemical-MAX";
        if (entry == HyperItems.MUTEKI.get())
            return "Muteki Star";
        if (entry == HyperItems.NOVEL.get())
            return "Novelcalibur";
        if (entry == HyperItems.CHRONICLE.get())
            return "Chroniclock";
        if (entry == HyperItems.PARADOX.get())
            return "Pickdox";
        if (entry == HyperItems.VRX.get())
            return "V.R.X.";
        if (entry == HyperItems.FUMETSU.get())
            return "Fumetsu Wither Spawn Egg";
        if (entry == HyperEntities.FUMETSU.get())
            return "Fumetsu Wither";
        if (entry == HyperBlocks.FUMETSU_SKULL.get())
            return "Center Fumetsu Skull";
        if (entry == HyperBlocks.FUMETSU_RIGHT.get())
            return "Right Fumetsu Skull";
        if (entry == HyperBlocks.FUMETSU_LEFT.get())
            return "Left Fumetsu Skull";
        if (entry == HyperBlocks.MATERIALIZER.get())
            return "Z-MAX Materializer";

        return null;
    }

    private <T> String defaultName(T entry, ResourceLocation name) {
        String special = specialize(entry, name);
        if (special != null)
            return special;

        return Arrays.stream(name.getPath().split("_")).map(s -> {
            if (MAP.containsKey(s))
                return MAP.get(s);
            char splinter = Character.toUpperCase(s.charAt(0));
            return splinter + s.substring(1);
        }).collect(Collectors.joining(" "));
    }
}
