package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.sakurafuld.hyperdaimc.helper.Calculates;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.antlr.v4.runtime.misc.Triple;

import java.util.Objects;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class DeskItemVFX implements IDeskVFX {
    private final DeskScreen screen;
    private final Slot slot;
    private final Slot result;
    private final Vec2 target;
    private int ticks = 0;
    private Vec2 position;
    private Vec2 oldPosition;
    private Vec2 movement;
    private float rot = 0;
    private float oldRot = 0;
    private Triple<Vec2, Vec2, Vec2> curve = null;

    public DeskItemVFX(DeskScreen screen, Slot slot, Slot result) {
        this.screen = screen;
        this.slot = slot;
        this.position = new Vec2(slot.x, slot.y);
        this.result = result;
        this.oldPosition = this.position;
        this.target = new Vec2(result.x, result.y);
        this.movement = this.target.add(this.position.negated()).normalized().negated().scale(5);
    }

    public Vec2 getPosition() {
        return this.position;
    }

    @Override
    public boolean tick() {
        int max = this.screen.getData().getIndexes().size() * this.screen.getData().getFrequency();
        this.oldPosition = this.position;
        this.oldRot = this.rot;
        if (++this.ticks <= max) {
            Vec2 vector = this.target.add(this.position.negated());
            float rot = (float) Math.toDegrees(-Mth.atan2(vector.x, vector.y));
            float delta = Math.min(1, (float) Calculates.curve(Math.min(1, this.ticks / (double) max), 0, 0.3, 1));
            this.rot = Mth.rotLerp(delta, this.rot, rot);
            this.position = this.position.add(this.movement);
            this.movement = this.movement.scale(0.6f);
        } else {
            if (this.curve == null) {
                Vec2 p1 = this.position;
                Random random = new Random(max + this.screen.getMenu().access.evaluate((level, pos) -> Objects.hash(pos.hashCode(), pos.getX(), pos.getY(), pos.getZ()), 0));
                int xSize = this.screen.getXSize();
                Vec2 p3 = p1.add(this.target.add(p1.negated()).negated().scale(0.75f));
                this.curve = new Triple<>(p1, new Vec2(random.nextInt(xSize), random.nextInt(xSize)), p3/*new Vec2(random.nextInt(xSize), random.nextInt(xSize))*/);
            }

            int index = this.screen.getData().getIndexes().indexOf(this.slot.index);
            boolean last = index == this.screen.getData().getIndexes().size() - 1;
            float delta = (this.ticks - max) / (last ? 22f : 20f);
            if (delta > 1) {
                delta = (float) index / (float) this.screen.getData().getIndexes().size();
                float pitch = (float) Math.pow(2, ((delta * 24) - 12) / 12d);
                if (!last) {
                    Random random = new Random();
                    this.screen.addTriangleFX(random, new Vec2(this.position.x + 8, this.position.y + 8), 0.4f, pitch);
                    this.result.getItem().setPopTime(5);
                }
                return false;
            }

            float x = (float) Calculates.curve(delta, this.curve.a.x, this.curve.b.x, this.curve.c.x, this.target.x);
            float y = (float) Calculates.curve(delta, this.curve.a.y, this.curve.b.y, this.curve.c.y, this.target.y);
            Vec2 vector = this.target.add(this.position.negated());
            this.rot = (float) Math.toDegrees(-Mth.atan2(vector.x, vector.y));
            this.position = new Vec2(x, y);
        }
        return true;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        float x = this.screen.getGuiLeft() + Mth.lerp(partialTick, this.oldPosition.x, this.position.x);
        float y = this.screen.getGuiTop() + Mth.lerp(partialTick, this.oldPosition.y, this.position.y);
        float rot = Mth.rotLerp(partialTick, this.oldRot, this.rot);

        poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();

        poseStack.translate(x, y, 200);
        poseStack.translate(8, 8, 0);
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(rot));
        poseStack.translate(-8, -8, 0);
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(this.slot.getItem(), 0, 0);

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
