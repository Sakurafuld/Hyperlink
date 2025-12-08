package com.sakurafuld.hyperdaimc.addon.kubejs;

import com.sakurafuld.hyperdaimc.content.crafting.gameorb.GameOrbRenderer;
import dev.latvian.mods.kubejs.item.custom.BasicItemJS;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class GameOverItemJS extends BasicItemJS {
    public final ResourceLocation model;

    public GameOverItemJS(GameOverItemJSBuilder builder) {
        super(builder);
        this.model = builder.specialModel;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private GameOrbRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new GameOrbRenderer(GameOverItemJS.this.model);
                return this.renderer;
            }
        });
    }
}
