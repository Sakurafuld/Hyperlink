package com.sakurafuld.hyperdaimc.addon.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.List;

public abstract class HyperInformation {
    private final List<ItemStack> roots;

    protected HyperInformation(ItemLike... items) {
        this.roots = Arrays.stream(items).map(ItemStack::new).toList();
    }

    public final List<ItemStack> getRoots() {
        return this.roots;
    }

    @OnlyIn(Dist.CLIENT)
    public abstract void draw(GuiGraphics graphics, double mouseX, double mouseY);

    private static void drawSoul(GuiGraphics graphics) {
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(HyperBlocks.SOUL.get().defaultBlockState(), graphics.pose(), graphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    protected static void blockTransform(PoseStack poseStack) {
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(14, -14, 14);
        poseStack.mulPose(Axis.XP.rotationDegrees(11.25f));
        poseStack.mulPose(Axis.YP.rotationDegrees(55));
        poseStack.translate(-0.5, -0.5, -0.5);
    }

    protected static void drawSouls(GuiGraphics graphics) {
        PoseStack poseStack = graphics.pose();

        Renders.with(poseStack, () -> {
            RenderSystem.disableCull();
            drawSoul(graphics);
            poseStack.translate(0, 0, 1);
            drawSoul(graphics);
            poseStack.translate(0, 0, 1);
            drawSoul(graphics);
            poseStack.translate(0, -1, -1);
            drawSoul(graphics);
        });
    }
}
