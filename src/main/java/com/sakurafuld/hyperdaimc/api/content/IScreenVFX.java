package com.sakurafuld.hyperdaimc.api.content;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IScreenVFX {
    boolean tick();

    void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);
}
