package com.sakurafuld.hyperdaimc.content.crafting.material;

import com.sakurafuld.hyperdaimc.infrastructure.render.GashatItemRenderer;
import com.sakurafuld.hyperdaimc.infrastructure.render.ItemColorCommon;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class MaterialItem extends Item {
    public final ItemColorCommon tint;
    public final int layerCount;
    protected final ResourceLocation model;
    protected final boolean scaling;
    protected final boolean coloring;
    protected final boolean rotating;
    protected final boolean particle;

    public MaterialItem(String name, Properties pProperties, boolean scaling, boolean coloring, boolean rotating, boolean particle, int... tint) {
        super(pProperties);
        this.model = identifier("special/" + name);
        this.scaling = scaling;
        this.coloring = coloring;
        this.rotating = rotating;
        this.particle = particle;
        this.tint = (stack, index) -> tint[index];
        this.layerCount = tint.length;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GashatItemRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new GashatItemRenderer(MaterialItem.this.model, MaterialItem.this.tint, MaterialItem.this.scaling, MaterialItem.this.coloring, MaterialItem.this.rotating, MaterialItem.this.particle);
                return this.renderer;
            }
        });
    }
}
