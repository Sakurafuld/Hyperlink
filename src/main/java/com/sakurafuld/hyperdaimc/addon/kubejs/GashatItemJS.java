package com.sakurafuld.hyperdaimc.addon.kubejs;

import com.sakurafuld.hyperdaimc.infrastructure.render.GashatItemRenderer;
import com.sakurafuld.hyperdaimc.infrastructure.render.ItemColorCommon;
import dev.latvian.mods.kubejs.item.custom.BasicItemJS;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class GashatItemJS extends BasicItemJS {
    public final ResourceLocation model;
    public final ItemColorCommon tint;
    public final boolean halo;
    public final boolean scaling;
    public final boolean coloring;
    public final boolean rotating;
    public final boolean particle;

    public GashatItemJS(GashatItemJSBuilder builder) {
        super(builder);
        this.model = builder.specialModel;
        this.tint = builder.tint == null ? (stack, index) -> 0xFFFFFF : (stack, index) -> builder.tint.getColor(stack, index).getRgbJS();
        this.halo = builder.halo;
        this.scaling = builder.scaling;
        this.coloring = builder.coloring;
        this.rotating = builder.rotating;
        this.particle = builder.particle;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GashatItemRenderer renderer = null;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new GashatItemRenderer(GashatItemJS.this.model, GashatItemJS.this.tint, GashatItemJS.this.scaling, GashatItemJS.this.coloring, GashatItemJS.this.rotating, GashatItemJS.this.particle);
                return this.renderer;
            }
        });
    }
}
