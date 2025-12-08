package com.sakurafuld.hyperdaimc.content.crafting.sigill;

import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.brewing.PotionBrewEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.stream.Stream;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class SigilHandler {
    private static final List<String> LOOT = Stream.of("desert_pyramid", "simple_dungeon", "nether_bridge", "stronghold_corridor", "stronghold_crossing", "end_city_treasure")
            .map(name -> "chests/" + name).toList();

    @SubscribeEvent
    public static void brew(PotionBrewEvent.Post event) {
        if (event.getItem(0).is(HyperBlocks.FUMETSU_SKULL.get().asItem()))
            event.setItem(0, HyperBlocks.FUMETSU_LEFT.get().asItem().getDefaultInstance());
        if (event.getItem(2).is(HyperBlocks.FUMETSU_SKULL.get().asItem()))
            event.setItem(2, HyperBlocks.FUMETSU_RIGHT.get().asItem().getDefaultInstance());
    }

    @SubscribeEvent
    public static void loot(LootTableLoadEvent event) {
        LootTable table = event.getTable();
        if (table.getLootTableId().getNamespace().equals("minecraft")) {
            if (LOOT.contains(table.getLootTableId().getPath())) {
                table.addPool(LootPool.lootPool()
                        .when(LootItemRandomChanceCondition.randomChance(0.125f))
                        .add(LootItem.lootTableItem(HyperItems.GOD_SIGIL.get()))
                        .build());
            }
        }
    }
}
