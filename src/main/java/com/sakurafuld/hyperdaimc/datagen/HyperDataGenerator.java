package com.sakurafuld.hyperdaimc.datagen;


import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HyperDataGenerator {
    @SubscribeEvent
    public static void generate(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new HyperItemModelProvider(output, helper));
        generator.addProvider(event.includeClient(), new HyperEnglishProvider(output));
        generator.addProvider(event.includeClient(), new HyperJapaneseProvider(output));

        BlockTagsProvider dummyBlockTags = new BlockTagsProvider(output, event.getLookupProvider(), HYPERDAIMC, helper) {
            @Override
            protected void addTags(HolderLookup.Provider pProvider) {
            }
        };
        generator.addProvider(event.includeServer(), dummyBlockTags);
        generator.addProvider(event.includeServer(), new HyperTagsProvider(output, event.getLookupProvider(), dummyBlockTags.contentsGetter(), helper));
        generator.addProvider(event.includeServer(), new HyperRecipeProvider(output));
    }
}
