package com.sakurafuld.hyperdaimc.addon.kubejs;

import com.sakurafuld.hyperdaimc.HyperSetup;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class GameOverItemJSBuilder extends ItemBuilder {
    final ResourceLocation specialModel;

    public GameOverItemJSBuilder(ResourceLocation id) {
        super(id);
        this.specialModel = identifier(id.getNamespace(), "special/" + id.getPath());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HyperSetup.specialModels.add(this.specialModel));
    }

    @Override
    public Item createObject() {
        return new GameOverItemJS(this);
    }
}
