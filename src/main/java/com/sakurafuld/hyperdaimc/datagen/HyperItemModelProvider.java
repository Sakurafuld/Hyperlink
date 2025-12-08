package com.sakurafuld.hyperdaimc.datagen;

import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.crafting.material.MaterialItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

public class HyperItemModelProvider extends ItemModelProvider {
    public HyperItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, HYPERDAIMC, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (Object2ObjectMap.Entry<String, RegistryObject<Item>> entry : HyperItems.MATERIAL.object2ObjectEntrySet()) {
            if (entry.getValue().get() instanceof MaterialItem material) {
                String suffix = entry.getKey().substring(entry.getKey().lastIndexOf('_') + 1);

                this.getBuilder(entry.getKey())
                        .parent(new ModelFile.UncheckedModelFile("builtin/entity"))
                        .guiLight(BlockModel.GuiLight.FRONT).texture("particle", this.modLoc("item/material/" + suffix + 0));

                ItemModelBuilder builder = this.getBuilder("special/" + entry.getKey())
                        .parent(new ModelFile.UncheckedModelFile("item/generated"));
                for (int index = 0; index < material.layerCount; index++) {
                    builder.texture("layer" + index, this.modLoc("item/material/" + suffix + index));
                }

            }
        }
    }
}
