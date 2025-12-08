package com.sakurafuld.hyperdaimc.addon.jei;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.infrastructure.Writes;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class HyperBrewingRecipeCategory extends AbstractRecipeCategory<HyperBrewingRecipe> {
    public static final RecipeType<HyperBrewingRecipe> TYPE = new RecipeType<>(identifier("brewing"), HyperBrewingRecipe.class);

    private final IDrawable background;
    private final IDrawableAnimated arrow;
    private final IDrawableAnimated bubbles;
    private final IDrawableStatic blazeHeat;

    public HyperBrewingRecipeCategory(IGuiHelper helper) {
        super(TYPE, Writes.gameOver(I18n.get("recipe.hyperdaimc.brewing")), Icon.INSTANCE, 114, 61);

        Textures textures = Internal.getTextures();
        this.background = textures.getBrewingStandBackground();
        this.blazeHeat = textures.getBrewingStandBlazeHeat();
        this.arrow = helper.createAnimatedDrawable(textures.getBrewingStandArrow(), 400, IDrawableAnimated.StartDirection.TOP, false);

        record BubblesTimer(ITickTimer internalTimer) implements ITickTimer {
            private static final int[] BUBBLE_LENGTHS = new int[]{29, 23, 18, 13, 9, 5, 0};

            BubblesTimer(IGuiHelper internalTimer) {
                this(internalTimer.createTickTimer(14, BUBBLE_LENGTHS.length - 1, false));
            }

            @Override
            public int getValue() {
                int timerValue = this.internalTimer.getValue();
                return BUBBLE_LENGTHS[timerValue];
            }

            @Override
            public int getMaxValue() {
                return BUBBLE_LENGTHS[0];
            }
        }

        this.bubbles = helper.createAnimatedDrawable(textures.getBrewingStandBubbles(), new BubblesTimer(helper), IDrawableAnimated.StartDirection.BOTTOM);
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(HyperBrewingRecipe recipe) {
        return recipe.id();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(HyperBrewingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.background.draw(guiGraphics, 0, 1);
        this.blazeHeat.draw(guiGraphics, 5, 30);
        this.bubbles.draw(guiGraphics, 9, 1);
        this.arrow.draw(guiGraphics, 43, 3);
        Font font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, Component.translatable("gui.jei.category.brewing.steps", recipe.steps().get()), 70, 28, 0x808080, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, HyperBrewingRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 37)
                .addItemStacks(recipe.potion());
        builder.addInputSlot(24, 44)
                .addItemStacks(recipe.potion());
        builder.addInputSlot(47, 37)
                .addItemStacks(recipe.potion());
        builder.addInputSlot(24, 3)
                .addItemStack(recipe.ingredient());
        builder.addOutputSlot(81, 3)
                .addItemStacks(recipe.result())
                .setStandardSlotBackground();
    }

    private static class Icon implements IDrawable {
        static final Icon INSTANCE = new Icon();
        static final Random RANDOM = new Random();
        static final Set<Particle> PARTICLES = new ObjectOpenHashSet<>();

        @Override
        public int getWidth() {
            return 16;
        }

        @Override
        public int getHeight() {
            return 16;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
            PoseStack poseStack = guiGraphics.pose();
            this.renderModel(guiGraphics, xOffset, yOffset);
            for (int count = 0; count < 3; count++)
                if (RANDOM.nextInt(400) == 0)
                    PARTICLES.add(new Particle());

            poseStack.pushPose();
            poseStack.translate(xOffset, yOffset, 150);
            poseStack.scale(16, 16, 16);
            PARTICLES.removeIf(particle -> particle.render(poseStack));
            poseStack.popPose();
        }

        @OnlyIn(Dist.CLIENT)
        public void renderModel(GuiGraphics graphics, int x, int y) {
            PoseStack poseStack = graphics.pose();
            Renders.with(poseStack, () -> {
                BakedModel model = getModel();
                poseStack.translate(x, y + 16, 150);
                poseStack.mulPoseMatrix(new Matrix4f().scaling(1, -1, 1));
                poseStack.scale(16, 16, 16);
                boolean flag = !model.usesBlockLight();
                if (flag)
                    Lighting.setupForFlatItems();

                poseStack.translate(0.5, 0.5, 0.5);

                Function<BakedQuad, Integer> colorizer = quad -> 0xFFFFFFFF;
                long millis = Util.getMillis();
                double time = millis % 20000;
                if (time <= 200 || (6000 < time && time <= 6200) || (10000 < time && (time <= 10300)) || (10400 < time && (time <= 10450))) {
                    poseStack.scale(RANDOM.nextFloat(0.5f, 1.75f), RANDOM.nextFloat(0.5f, 1.75f), RANDOM.nextFloat(0.5f, 1.75f));
                    if (10000 < time)
                        colorizer = quad -> (0xFF000000) | RANDOM.nextInt(0xFFFFFF);
                }

                float cos = Mth.cos(millis / 800f);

                poseStack.mulPose(Axis.ZP.rotationDegrees(cos * 8));
                poseStack.mulPose(Axis.XP.rotationDegrees(cos * 2));
                poseStack.mulPose(Axis.YP.rotationDegrees(cos * 2));

                //noinspection UnstableApiUsage
                model = ForgeHooksClient.handleCameraTransforms(poseStack, model, ItemDisplayContext.GUI, false);
                poseStack.translate(-0.5, -0.5, -0.5);

                Renders.model(model, poseStack, Renders.getBuffer(Sheets.translucentItemSheet()), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, colorizer);

                graphics.flush();
                if (flag)
                    Lighting.setupFor3DItems();
            });
        }

        @OnlyIn(Dist.CLIENT)
        private static BakedModel getModel() {
            return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(Items.BREWING_STAND);
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
                this.delay = Math.round(Mth.lerp(Math.random(), 0, 10000));

                this.color = RANDOM.nextInt(0xFFFFFF);

                double angle = Math.toRadians(RANDOM.nextInt(360));
                this.x = Math.cos(angle) / 3;
                this.y = Math.sin(angle) / 3;
                this.xRot = RANDOM.nextFloat(-22.5f, 22.5f);
                this.yRot = RANDOM.nextFloat(-22.5f, 22.5f);
            }

            @OnlyIn(Dist.CLIENT)
            public boolean render(PoseStack poseStack) {
                if (Util.getMillis() - this.made >= this.age)
                    return true;

                Renders.with(poseStack, () -> {
                    poseStack.translate(0.5, 0.5, 0.5);

                    //noinspection UnstableApiUsage
                    ForgeHooksClient.handleCameraTransforms(poseStack, getModel(), ItemDisplayContext.GUI, false);
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
