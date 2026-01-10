package com.sakurafuld.hyperdaimc.addon.jei.information;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.crafting.skull.FumetsuSkullBlock;
import com.sakurafuld.hyperdaimc.content.crafting.skull.FumetsuSkullBlockEntityRenderer;
import com.sakurafuld.hyperdaimc.infrastructure.Calculates;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class FumetsuInformation extends HyperInformation {
    private static final RandomSource RANDOM = RandomSource.create();
    private final IDrawable sigil;
    private double lastDelta;
    private final Set<Particle> particles = new ObjectOpenHashSet<>();

    public FumetsuInformation(IGuiHelper helper) {
        super(helper, HyperItems.GOD_SIGIL.get(), HyperBlocks.SOUL.get().asItem(), HyperBlocks.FUMETSU_RIGHT.get().asItem(), HyperBlocks.FUMETSU_SKULL.get().asItem(), HyperBlocks.FUMETSU_LEFT.get().asItem(), HyperItems.FUMETSU.get());

        this.sigil = helper.createDrawableItemLike(HyperItems.GOD_SIGIL.get());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(GuiGraphics graphics, double mouseX, double mouseY) {
        PoseStack poseStack = graphics.pose();

        double delta = Math.min(1, Util.getMillis() * 3 % 2250d / 1000d);
        if (this.lastDelta <= 0.9 && 0.9 < delta) {
            int offsetX = 47;
            int offsetY = 52;
            for (int i = 0; i < 6; i++)
                this.particles.add(new Particle(offsetX + (RANDOM.nextFloat() * 2 - 1) * 4, offsetY + (RANDOM.nextFloat() * 2 - 1) * 4));
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(HyperSounds.DESK_POP.get(), 1 + RANDOM.nextFloat() * 0.2f));
        }
        this.lastDelta = delta;

        if (!this.particles.isEmpty())
            this.particles.removeIf(particle -> !particle.render(graphics));

        float rot = (float) Calculates.curve(delta, 0, -45, 100);
        Renders.with(poseStack, () -> {
            poseStack.translate(10, 30, 0);
            poseStack.translate(5, 20, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(rot));
            poseStack.translate(-5, -20, 0);
            this.sigil.draw(graphics);
        });

        this.drawArrow(graphics, 83, 40);
        this.drawFumetsu(graphics, mouseX, mouseY, 130, 60);

        Renders.with(poseStack, () -> {
            poseStack.translate(40, 40, 100);
            blockTransform(poseStack);
            this.drawSouls(graphics);
            poseStack.translate(0, 1, 0);
            this.drawSkulls(graphics);
        });
    }

    private void drawSkulls(GuiGraphics graphics) {
        PoseStack poseStack = graphics.pose();

        Renders.with(poseStack, () -> {
            drawSkull(graphics, HyperBlocks.FUMETSU_RIGHT);
            poseStack.translate(0, 0, 1);
            drawSkull(graphics, HyperBlocks.FUMETSU_SKULL);
            poseStack.translate(0, 0, 1);
            drawSkull(graphics, HyperBlocks.FUMETSU_LEFT);
        });
    }

    private void drawSkull(GuiGraphics graphics, RegistryObject<FumetsuSkullBlock> skull) {
        FumetsuSkullBlockEntityRenderer.render(graphics.pose(), graphics.bufferSource(), LightTexture.FULL_BRIGHT, skull.get().defaultBlockState()
                .setValue(SkullBlock.ROTATION, 12));
    }

    private static class Particle {
        private static final long DURATION = 500;

        private final long made = Util.getMillis();
        private final int color = 0xFF << 24 | RANDOM.nextInt(0xFFFFFF);
        private final boolean invert;
        private final float size;
        private Vec2 pos;
        private Vec2 move;
        private float rot;
        private float lastTime = getTime();

        private Particle(float x, float y) {
            this.invert = RANDOM.nextBoolean();
            this.size = 0.5f + RANDOM.nextFloat() * 0.8f;
            this.pos = new Vec2(x, y);
            this.move = new Vec2((RANDOM.nextFloat() * 2 - 1) * 4, RANDOM.nextFloat() * -6);
            this.rot = RANDOM.nextInt(360);
        }

        private boolean render(GuiGraphics graphics) {
            if (DURATION < Util.getMillis() - this.made) {
                return false;
            } else {
                PoseStack poseStack = graphics.pose();

                Renders.with(poseStack, () -> {
                    poseStack.translate(this.pos.x, this.pos.y, 500);
                    poseStack.scale(this.size, this.size, this.size);
                    poseStack.mulPose(Axis.YP.rotationDegrees(55));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(this.rot));

                    Renders.hollowTriangle(poseStack.last().pose(), graphics.bufferSource().getBuffer(Renders.Type.LIGHTNING_NO_CULL), 3, 1.2f, this.color);
                });

                float time = getTime();
                float elapsed = time - this.lastTime;
                if (1 <= elapsed) {
                    float dx = this.move.x * 0.9f;
                    float dy = this.move.y + 0.9f;
                    this.move = new Vec2(this.move.x + (dx - this.move.x) * elapsed, this.move.y + (dy - this.move.y) * elapsed);
                    this.pos = this.pos.add(this.move);
                    this.rot += Mth.wrapDegrees(20 * elapsed) * (this.invert ? -1 : 1);
                    this.lastTime = time;
                }

                return true;
            }
        }
    }
}
