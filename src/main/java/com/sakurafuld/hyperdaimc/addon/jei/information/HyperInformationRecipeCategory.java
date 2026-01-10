package com.sakurafuld.hyperdaimc.addon.jei.information;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.addon.jei.AbstractHyperRecipeCategory;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.common.Internal;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;
import java.util.Set;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class HyperInformationRecipeCategory extends AbstractHyperRecipeCategory<HyperInformation> {
    public static final RecipeType<HyperInformation> TYPE = new RecipeType<>(identifier("information"), HyperInformation.class);

    public HyperInformationRecipeCategory() {
        super(TYPE, Component.translatable("recipe.hyperdaimc.information"), Icon.INSTANCE, 150, 70);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(HyperInformation recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        recipe.draw(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HyperInformation recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.getRoots().size(); i++) {
            ItemStack stack = recipe.getRoots().get(i);
            builder.addInputSlot(i * 18, 0)
                    .addItemStack(stack);
        }

        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                .addItemStacks(recipe.getRoots());
    }

    private enum Icon implements IDrawable {
        INSTANCE;

        static final Random RANDOM = new Random();
        static final Set<Particle> PARTICLES = new ObjectOpenHashSet<>();
        static final IDrawableStatic INFO_ICON = Internal.getTextures().getInfoIcon();

        @Override
        public int getWidth() {
            return INFO_ICON.getWidth();
        }

        @Override
        public int getHeight() {
            return INFO_ICON.getHeight();
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
            PoseStack poseStack = guiGraphics.pose();
            Renders.with(poseStack, () -> {
                poseStack.translate(xOffset, yOffset, 100);
                poseStack.translate(this.getWidth() / 2d, this.getHeight() / 2d, 0);
                long millis = Util.getMillis();
                double time = millis % 20000;
                if (time <= 200 || (6000 < time && time <= 6200) || (10000 < time && (time <= 10300)) || (10400 < time && (time <= 10450)))
                    poseStack.scale(RANDOM.nextFloat(0.5f, 1.75f), RANDOM.nextFloat(0.5f, 1.75f), RANDOM.nextFloat(0.5f, 1.75f));
                float cos = Mth.cos(millis / 800f);
                poseStack.mulPose(Axis.ZP.rotationDegrees(cos * 8));
                poseStack.mulPose(Axis.XP.rotationDegrees(cos * 2));
                poseStack.mulPose(Axis.YP.rotationDegrees(cos * 2));
                poseStack.translate(this.getWidth() / -2d, this.getHeight() / -2d, 0);
                INFO_ICON.draw(guiGraphics);
            });

            for (int count = 0; count < 3; count++)
                if (RANDOM.nextInt(400) == 0)
                    PARTICLES.add(new Particle());

            Renders.with(poseStack, () -> {
                poseStack.translate(xOffset, yOffset, 150);
                poseStack.scale(16, 16, 16);
                PARTICLES.removeIf(particle -> particle.render(poseStack));
            });
        }

        static class Particle {
            private final long made;
            private final float age;
            private final long delay;
            private final int color;
            private final double x;
            private final double y;
            private final float xRot;
            private final float yRot;

            Particle() {
                this.made = Util.getMillis();
                this.age = RANDOM.nextInt(500, 1000);
                this.delay = RANDOM.nextLong(0, 10000000);

                this.color = RANDOM.nextInt(0xFFFFFF);

                double angle = Math.toRadians(RANDOM.nextInt(360));
                this.x = Math.cos(angle) / 3;
                this.y = Math.sin(angle) / 3;
                this.xRot = RANDOM.nextFloat(-45, 45);
                this.yRot = RANDOM.nextFloat(-45, 45);
            }

            @OnlyIn(Dist.CLIENT)
            public boolean render(PoseStack poseStack) {
                if (Util.getMillis() - this.made >= this.age)
                    return true;

                Renders.with(poseStack, () -> {
                    poseStack.translate(0.5, 0.5, 0.5);
                    poseStack.translate(this.x, this.y, 0);

                    float cos = Mth.cos((this.delay + Util.getMillis()) / 200f);
                    poseStack.mulPose(Axis.ZP.rotationDegrees(cos * 20));

                    poseStack.mulPose(Axis.XP.rotationDegrees(this.xRot));
                    poseStack.mulPose(Axis.YP.rotationDegrees(this.yRot));

                    float t = (this.age - (Util.getMillis() - this.made)) / (this.age - 100);
                    poseStack.scale(t, t, 0);

                    Renders.hollowTriangle(poseStack.last().pose(), Renders.getBuffer(Renders.Type.LIGHTNING_NO_CULL), 0.3f, 0.15f, 0x7F << 24 | this.color);
                });
                return false;
            }
        }
    }
}
