package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IDeskVFX {
    boolean tick();

    void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick);
}
