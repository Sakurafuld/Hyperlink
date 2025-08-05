package com.sakurafuld.hyperdaimc.content.over.materializer;

import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.api.content.IScreenVFX;
import com.sakurafuld.hyperdaimc.helper.Calculates;
import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;

import java.util.Random;

public class MaterializerTriangleVFX implements IScreenVFX {
    private static final Random RANDOM = new Random();

    private final MaterializerScreen screen;
    private final Vec2 position;
    private final int color;
    private final float rot;

    private int ticks = 0;
    private float size = 0;
    private int alpha = 0xFF;

    public MaterializerTriangleVFX(MaterializerScreen screen, Vec2 position) {
        this.screen = screen;
        this.position = position;
        this.color = RANDOM.nextInt(0xFFFFFF);
        this.rot = RANDOM.nextInt(360);
    }

    @Override
    public boolean tick() {
        if (++this.ticks > 10) {
            return false;
        } else {
            float delta = this.ticks / 10f;
            this.size = (float) Calculates.curve(delta, 0, 14, 24, 18);
            this.alpha = (int) ((1 - delta) * 0XFF);
            return true;
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        Renders.with(pGuiGraphics.pose(), () -> {
            pGuiGraphics.pose().translate(this.screen.getGuiLeft() + this.position.x, this.screen.getGuiTop() + this.position.y, 0);
            pGuiGraphics.pose().scale(this.size, this.size, 1);
            pGuiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(this.rot));
            Renders.hollowTriangle(pGuiGraphics.pose().last().pose(), Renders.getBuffer(Renders.Type.LIGHTNING_NO_CULL), 1, 0.3f, (this.alpha << 24) | this.color);
            Renders.endBatch(Renders.Type.LIGHTNING_NO_CULL);
        });
    }
}
