package com.sakurafuld.hyperdaimc.datagen;


import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HyperDataGenerator {
    @SubscribeEvent
    public static void generate(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();

        if (event.includeClient()) {
            generator.addProvider(new HyperItemModelProvider(generator, helper));
            generator.addProvider(new HyperEnglishProvider(generator));
            generator.addProvider(new HyperJapaneseProvider(generator));
        }

        if (event.includeServer()) {
            generator.addProvider(new HyperTagsProvider(generator, helper));
            generator.addProvider(new HyperRecipeProvider(generator));
        }
    }
}
