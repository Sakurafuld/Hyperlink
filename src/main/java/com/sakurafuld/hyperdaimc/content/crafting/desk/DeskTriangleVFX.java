package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.sakurafuld.hyperdaimc.api.content.IScreenVFX;
import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DeskTriangleVFX implements IScreenVFX {
    private final int max;
    private final float rotation;
    private final int color;
    private int ticks = 0;
    private Vec2 position;
    private Vec2 oldPosition;
    private Vec2 movement;

    public DeskTriangleVFX(Vec2 position, Vec2 movement, int max, float rotation, int color) {
        this.max = max;
        this.rotation = rotation;
        this.color = color;
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

            this.movement = this.movement.scale(0.75f);
            this.movement = new Vec2(this.movement.x, this.movement.y + Mth.square(0.98f));
            this.position = this.position.add(this.movement);
            return true;
        }
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        float x = Mth.lerp(pPartialTick, this.oldPosition.x, this.position.x);
        float y = Mth.lerp(pPartialTick, this.oldPosition.y, this.position.y);

        float delta = (float) this.ticks / this.max;
        float deltaO = (float) (this.ticks - 1) / this.max;

        float size = Mth.lerp(pPartialTick, (1 - deltaO), (1 - delta));

        Renders.with(pPoseStack, () -> {
            pPoseStack.translate(x, y, 400);
            pPoseStack.scale(size, size, size);
            pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(this.rotation * 18 + Mth.rotLerp(pPartialTick, ((this.ticks - 1) % 360f) * this.rotation, (this.ticks % 360f) * this.rotation)));

            Renders.hollowTriangle(pPoseStack.last().pose(), Renders.getBuffer(Renders.Type.LIGHTNING_NO_CULL), 4, 2, this.color);

            Renders.endBatch(Renders.Type.LIGHTNING_NO_CULL);
        });
    }
}
