package com.sakurafuld.hyperdaimc.infrastructure.render;

import net.minecraft.client.gui.components.Renderable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IScreenVFX extends Renderable {
    boolean tick();
}
