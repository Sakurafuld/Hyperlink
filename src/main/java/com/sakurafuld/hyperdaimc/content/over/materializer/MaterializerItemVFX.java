package com.sakurafuld.hyperdaimc.content.over.materializer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import com.sakurafuld.hyperdaimc.api.content.IScreenVFX;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.crafting.desk.DeskDustVFX;
import com.sakurafuld.hyperdaimc.helper.Calculates;
import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import java.util.Random;

public class MaterializerItemVFX implements IScreenVFX {
    private static final Random RANDOM = new Random();
    private static final Vec2 START = new Vec2(46, 57);
    private static final float WALK = START.y - 1;
    private static final Vec2 RESULT = new Vec2(148 + 4, 48 + 4);

    private final MaterializerScreen screen;
    private final ItemStack stack;

    private boolean walk = false;
    private int ticks = 0;
    private int landingTicks = 0;
    private float landingMax = 8;
    private Vec2 position = START;
    private Vec2 oldPosition = this.position;
    private Vec2 movement = Vec2.ZERO;
    private float rot = 0;
    private float oldRot = 0;
    private float xSize = 0;
    private float ySize = 0;
    private int z = 0;
    private Pair<Vec2, Vec2> curve = null;

    public MaterializerItemVFX(MaterializerScreen screen, ItemStack stack) {
        this.screen = screen;
        this.stack = stack;
    }

    @Override
    public boolean tick() {
        ++this.ticks;
        this.oldPosition = this.position;
        this.oldRot = this.rot;
        if (!this.walk) {
            if (this.ticks <= 11) {
                float delta = Math.min(1, this.ticks / 8f);
                this.xSize = (float) Calculates.curve(delta, 0, 0.1, 2, 0.8, 1);
                this.ySize = 1;
            } else {
                this.position = this.position.add(this.movement);
                if (this.ticks == 12) {
                    this.movement = new Vec2(0, -2.5f - RANDOM.nextFloat());
                } else if (this.position.y <= WALK) {
                    if (this.ticks > 16 || this.position.y < START.y - 5) {
                        this.z = 50;
                    }
                    this.movement = new Vec2(this.movement.x, this.movement.y + 0.8f);
                    this.rot = (float) Calculates.curve(Math.min(1, (this.ticks - 13) / 5f), 0, 90, 360);
                } else {
                    this.z = 50;
                    this.position = new Vec2(this.position.x, WALK);
                    this.movement = Vec2.ZERO;
                    this.rot = 0;
                    this.walk = true;
                    this.ticks = 0;
                }
            }
        }
        if (this.walk) {
            if (this.curve == null) {
                this.position = this.position.add(this.movement);

                if (this.position.y >= WALK) {
                    if (this.position.x > 124) {
                        this.jump();
                        return true;
                    }
                    if (this.landingTicks == 0) {
                        this.position = new Vec2(this.position.x, WALK);
                        this.movement = Vec2.ZERO;
                        this.rot = 0;
                        this.landingMax = RANDOM.nextInt(6, 12);
                        this.land();
                    }

                    float delta = this.landingTicks / this.landingMax;
                    delta = (float) Calculates.curve(delta, 0, 0.1, 1);
                    this.rot = (float) Calculates.curve(delta, 0, -1, -2, -28, -22);
                    this.xSize = (float) Calculates.curve(delta, 1.5, 0.5, 1.5, 1);
                    this.ySize = (float) Calculates.curve(delta, 0.625, 1.75, 0.3, 1);
                    ++this.landingTicks;
                    if (this.landingTicks >= this.landingMax) {
                        this.landingTicks = 0;
                        this.movement = new Vec2(1 + RANDOM.nextFloat(), -2.5f - RANDOM.nextFloat(2));
                    }
                } else {
                    this.xSize = 1;
                    this.ySize = 1;
                    this.movement = new Vec2(this.movement.x, this.movement.y + 0.8f);
                }

                this.rot = Math.min(10, this.rot + 3);

                if (this.position.x > 136) {
                    this.jump();
                }
            } else if (this.ticks <= 20) {
                double delta = this.ticks / 20d;
                delta = Calculates.curve(delta, 0, 0.3, 1);
                float x = (float) Calculates.curve(delta, this.curve.getFirst().x, this.curve.getSecond().x, RESULT.x);
                float y = (float) Calculates.curve(delta, this.curve.getFirst().y, this.curve.getSecond().y, RESULT.y);
                this.position = new Vec2(x, y);
                this.rot += (float) Calculates.curve(delta, 24, 40, 120);
            } else {
                float pitch = 1.3f + RANDOM.nextFloat() * 0.2f;
                Minecraft.getInstance().player.playNotifySound(HyperSounds.DESK_POP.get(), SoundSource.MASTER, 0.25f, pitch);
                Minecraft.getInstance().player.playNotifySound(HyperSounds.DESK_POP.get(), SoundSource.MASTER, 0.25f, pitch + 0.25f);
                this.screen.addVFX(new MaterializerTriangleVFX(this.screen, new Vec2(RESULT.x + 4, RESULT.y + 4)));
                return false;
            }
        }

        return true;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        float x = this.screen.getGuiLeft() + Mth.lerp(pPartialTick, this.oldPosition.x, this.position.x);
        float y = this.screen.getGuiTop() + Mth.lerp(pPartialTick, this.oldPosition.y, this.position.y);
        float rot = Mth.rotLerp(pPartialTick, this.oldRot, this.rot);

        PoseStack poseStack = RenderSystem.getModelViewStack();

        Renders.with(poseStack, () -> {
            poseStack.translate(x, y, -50 + this.z);
            poseStack.scale(0.5f, 0.5f, 0.5f);

            if (!this.walk) {
                poseStack.scale(this.xSize, this.ySize, 1);
            }

            poseStack.translate(8, 8, 0);

            if (this.walk) {
                poseStack.translate(0, 8, 0);
                poseStack.scale(this.xSize, this.ySize, 1);
                poseStack.translate(0, -8, 0);
            }

            poseStack.mulPose(Vector3f.ZP.rotationDegrees(rot));
            poseStack.translate(-8, -8, 0);
            Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(this.stack, 0, 0);
        });
        RenderSystem.applyModelViewMatrix();
    }

    private void land() {
        for (int count = 0; count < 2; count++) {
            float x = Mth.cos((float) Math.toRadians(RANDOM.nextInt(180) + 90));
            float y = Mth.sin((float) Math.toRadians(RANDOM.nextInt(90) + 180));

            x *= Mth.lerp(RANDOM.nextFloat(), 1, 2);

            this.screen.addVFX(new DeskDustVFX(new Vec2(this.screen.getGuiLeft() + this.position.x + 2, this.screen.getGuiTop() + this.position.y + 6), new Vec2(x, y).scale(3), RANDOM.nextInt(6, 10), RANDOM.nextFloat() * 20));
        }
        float pitch = 1.6f + RANDOM.nextFloat() * 0.4f;
        Minecraft.getInstance().player.playNotifySound(HyperSounds.MATERIALIZER_POP.get(), SoundSource.MASTER, 0.375f, pitch);
    }

    private void jump() {
        this.curve = Pair.of(this.position, new Vec2(RESULT.x - RANDOM.nextFloat(8, 25), RESULT.y - RANDOM.nextFloat(48, 96)));
        this.ticks = 0;

        Minecraft.getInstance().player.playNotifySound(HyperSounds.DESK_RESULT.get(), SoundSource.MASTER, 0.125f, 2);
        this.screen.jump();
    }

    public void clear() {
        for (int count = 0; count < 4; count++) {
            float x = Mth.cos((float) Math.toRadians(RANDOM.nextInt(360)));
            float y = Mth.sin((float) Math.toRadians(RANDOM.nextInt(360)));

            this.screen.addVFX(new DeskDustVFX(new Vec2(this.screen.getGuiLeft() + this.position.x + 4, this.screen.getGuiTop() + this.position.y + 6), new Vec2(x, y).scale(4), RANDOM.nextInt(8, 16), RANDOM.nextFloat() * 20));
        }
        Minecraft.getInstance().player.playNotifySound(HyperSounds.DESK_MINING.get(), SoundSource.MASTER, 0.25f, 2);
    }
}
