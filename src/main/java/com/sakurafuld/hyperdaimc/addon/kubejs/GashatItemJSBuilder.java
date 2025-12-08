package com.sakurafuld.hyperdaimc.addon.kubejs;

import com.sakurafuld.hyperdaimc.HyperSetup;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class GashatItemJSBuilder extends ItemBuilder {
    final ResourceLocation specialModel;
    boolean halo = false;
    boolean scaling = false;
    boolean coloring = false;
    boolean rotating = false;
    boolean particle = false;

    public GashatItemJSBuilder(ResourceLocation id) {
        super(id);
        this.specialModel = identifier(id.getNamespace(), "special/" + id.getPath());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HyperSetup.specialModels.add(this.specialModel));
    }

    public GashatItemJSBuilder halo() {
        this.halo = true;
        return this;
    }

    public GashatItemJSBuilder scaling() {
        this.scaling = true;
        return this;
    }

    public GashatItemJSBuilder coloring() {
        this.coloring = true;
        return this;
    }

    public GashatItemJSBuilder rotating() {
        this.rotating = true;
        return this;
    }

    public GashatItemJSBuilder particle() {
        this.particle = true;
        return this;
    }

    @Override
    public GashatItemJS createObject() {
        return new GashatItemJS(this);
    }
}
