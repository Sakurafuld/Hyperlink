package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.infrastructure.render.IScreenVFX;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DeskDustVFX implements IScreenVFX {
    private final int max;
    private final float rotation;
    private int ticks = 0;
    private Vec2 position;
    private Vec2 oldPosition;
    private Vec2 movement;

    public DeskDustVFX(Vec2 position, Vec2 movement, int max, float rotation) {
        this.max = max;
        this.rotation = rotation;
        this.position = position;
        this.oldPosition = this.position;
        this.movement = movement;
    }


    @Override
    public boolean tick() {
        if (++this.ticks > this.max) {
            return false;
        } else {
            this.oldPosition = this.position;

            this.movement = new Vec2(this.movement.x, this.movement.y + 1.25f);
            this.position = this.position.add(this.movement);
            return true;
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int x = Math.round(Mth.lerp(pPartialTick, this.oldPosition.x, this.position.x));
        int y = Math.round(Mth.lerp(pPartialTick, this.oldPosition.y, this.position.y));

        float delta = (float) this.ticks / this.max;
        float deltaO = (float) (this.ticks - 1) / this.max;

        float size = Mth.lerp(pPartialTick, (1 - deltaO), (1 - delta));

        Renders.with(pGuiGraphics.pose(), () -> {
            pGuiGraphics.pose().translate(x, y, 300);
            pGuiGraphics.pose().translate(1, 1, 0);
            pGuiGraphics.pose().scale(size, size, size);
            pGuiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(pPartialTick, ((this.ticks - 1) % 360f) * this.rotation, (this.ticks % 360f) * this.rotation)));
            pGuiGraphics.pose().translate(-1, -1, 0);

            pGuiGraphics.fill(0, 0, 2, 2, 0xFFFFFFFF);
        });
    }
}
