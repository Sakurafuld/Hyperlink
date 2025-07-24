package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.api.content.IScreenVFX;
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int x = Math.round(Mth.lerp(partialTick, this.oldPosition.x, this.position.x));
        int y = Math.round(Mth.lerp(partialTick, this.oldPosition.y, this.position.y));

        float delta = (float) this.ticks / this.max;
        float deltaO = (float) (this.ticks - 1) / this.max;

        float size = Mth.lerp(partialTick, (1 - deltaO), (1 - delta));

        graphics.pose().translate(x, y, 300);
        graphics.pose().translate(1, 1, 0);
        graphics.pose().scale(size, size, size);
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(partialTick, ((this.ticks - 1) % 360f) * this.rotation, (this.ticks % 360f) * this.rotation)));
        graphics.pose().translate(-1, -1, 0);

        graphics.fill(0, 0, 2, 2, 0xFFFFFFFF);
    }
}
