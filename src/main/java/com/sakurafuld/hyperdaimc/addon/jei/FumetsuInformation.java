package com.sakurafuld.hyperdaimc.addon.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.crafting.skull.FumetsuSkullBlock;
import com.sakurafuld.hyperdaimc.content.crafting.skull.FumetsuSkullBlockEntityRenderer;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.infrastructure.Calculates;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.RegistryObject;

public class FumetsuInformation extends HyperInformation {
    private static final RandomSource RANDOM = RandomSource.create();
    private final FumetsuEntity fumetsu;
    private final IDrawable sigil;
    private final IDrawable arrow;
    private double lastDelta;

    public FumetsuInformation(IGuiHelper helper) {
        super(HyperItems.GOD_SIGIL.get(), HyperBlocks.SOUL.get().asItem(), HyperBlocks.FUMETSU_RIGHT.get().asItem(), HyperBlocks.FUMETSU_SKULL.get().asItem(), HyperBlocks.FUMETSU_LEFT.get().asItem(), HyperItems.BUG_STARS.get(0).get());

        this.fumetsu = FumetsuEntity.drawOnly();
        this.sigil = helper.createDrawableItemLike(HyperItems.GOD_SIGIL.get());
        this.arrow = helper.getRecipeArrowFilled();
    }

    @Override
    public void draw(GuiGraphics graphics, double mouseX, double mouseY) {
        PoseStack poseStack = graphics.pose();

        double delta = Math.min(1, Util.getMillis() * 3 % 2250d / 1000d);
        if (this.lastDelta < 0.9 && 0.9 < delta && RANDOM.nextFloat() < 0.5f)
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(HyperSounds.FUMETSU_AMBIENT.get(), (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2f + 1));
        this.lastDelta = delta;

        float rot = (float) Calculates.curve(delta, 0, -45, 100);
        Renders.with(poseStack, () -> {
            poseStack.translate(10, 30, 0);
            poseStack.translate(5, 20, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(rot));
            poseStack.translate(-5, -20, 0);
            this.sigil.draw(graphics);
        });

        this.arrow.draw(graphics, 83, 40);

        Renders.with(poseStack, () -> {
            int x = 130;
            int y = 60;

            this.fumetsu.setMovable(true);
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, x, y, 9, x - (float) mouseX, y - (float) mouseY, this.fumetsu);
            this.fumetsu.setMovable(false);
        });

        Renders.with(poseStack, () -> {
            poseStack.translate(40, 40, 100);
            blockTransform(poseStack);
            drawSouls(graphics);
            poseStack.translate(0, 1, 0);
            drawSkulls(graphics);
        });
    }

    private static void drawSkulls(GuiGraphics graphics) {
        PoseStack poseStack = graphics.pose();

        Renders.with(poseStack, () -> {
            drawSkull(graphics, HyperBlocks.FUMETSU_RIGHT);
            poseStack.translate(0, 0, 1);
            drawSkull(graphics, HyperBlocks.FUMETSU_SKULL);
            poseStack.translate(0, 0, 1);
            drawSkull(graphics, HyperBlocks.FUMETSU_LEFT);
        });
    }

    private static void drawSkull(GuiGraphics graphics, RegistryObject<FumetsuSkullBlock> skull) {
        FumetsuSkullBlockEntityRenderer.render(graphics.pose(), graphics.bufferSource(), LightTexture.FULL_BRIGHT, skull.get().defaultBlockState()
                .setValue(BlockStateProperties.ROTATION_16, 12));
    }
}
