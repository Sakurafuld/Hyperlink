package com.sakurafuld.hyperdaimc.content.hyper.fumetsu.squall;

import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.skull.FumetsuSkullRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FumetsuSquallRenderer extends FumetsuSkullRenderer {
    public FumetsuSquallRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    protected float getSize() {
        return 3;
    }
}
