package com.sakurafuld.hyperdaimc.addon.jei.information;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
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
    private final IDrawable arrow;
    private FumetsuEntity fumetsu;

    protected HyperInformation(IGuiHelper helper, ItemLike... items) {
        this.roots = Arrays.stream(items).map(ItemStack::new).toList();
        this.arrow = helper.getRecipeArrowFilled();
    }

    public final List<ItemStack> getRoots() {
        return this.roots;
    }

    @OnlyIn(Dist.CLIENT)
    public abstract void draw(GuiGraphics graphics, double mouseX, double mouseY);

    protected void drawArrow(GuiGraphics graphics, int x, int y) {
        this.arrow.draw(graphics, x, y);
    }

    private void drawSoul(GuiGraphics graphics) {
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(HyperBlocks.SOUL.get().defaultBlockState(), graphics.pose(), graphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    protected static void blockTransform(PoseStack poseStack) {
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(14, -14, 14);
        poseStack.mulPose(Axis.XP.rotationDegrees(11.25f));
        poseStack.mulPose(Axis.YP.rotationDegrees(55));
        poseStack.translate(-0.5, -0.5, -0.5);
    }

    protected void drawSouls(GuiGraphics graphics) {
        PoseStack poseStack = graphics.pose();

        Renders.with(poseStack, () -> {
            RenderSystem.disableCull();
            this.drawSoul(graphics);
            poseStack.translate(0, 0, 1);
            this.drawSoul(graphics);
            poseStack.translate(0, 0, 1);
            this.drawSoul(graphics);
            poseStack.translate(0, -1, -1);
            this.drawSoul(graphics);
        });
    }

    protected void drawFumetsu(GuiGraphics graphics, double mouseX, double mouseY, int x, int y) {
        if (this.fumetsu == null)
            this.fumetsu = FumetsuEntity.drawOnly();

        this.fumetsu.setMovable(true);
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x, y, 9, x - (float) mouseX, y - (float) mouseY, this.fumetsu);
        this.fumetsu.setMovable(false);
    }

    protected static float getTime() {
        return Math.round(Util.getMillis() / 50f);
    }
}
