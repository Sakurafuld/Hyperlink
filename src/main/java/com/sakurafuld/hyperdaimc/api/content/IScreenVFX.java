package com.sakurafuld.hyperdaimc.api.content;

import net.minecraft.client.gui.components.Widget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IScreenVFX extends Widget {
    boolean tick();
}
