package com.sakurafuld.hyperdaimc.addon.jei.information;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Set;

public class BugStarInformation extends HyperInformation {
    private static final RandomSource RANDOM = RandomSource.create();
    private Player player;
    private float lastTime = getTime();

    private final List<Element> elements;
    private int current = 0;
    private float totalElapsed = 0;

    private long lastMillis = Util.getMillis();
    private final Set<Star> stars = new ObjectOpenHashSet<>();

    public BugStarInformation(IGuiHelper helper) {
        super(helper, HyperBlocks.FUMETSU_RIGHT.get().asItem(), HyperBlocks.FUMETSU_SKULL.get().asItem(), HyperBlocks.FUMETSU_LEFT.get().asItem(), HyperItems.FUMETSU.get(), HyperItems.BUG_STARS.get(0).get());

        this.elements = List.of(new Logout(), new Death(), new Warp());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(GuiGraphics graphics, double mouseX, double mouseY) {
        if (this.player == null) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer you = mc.player;
            ClientLevel level = mc.level;
            if (you == null || level == null)
                return;

            this.player = new AbstractClientPlayer(level, you.getGameProfile()) {
            };
        }

        Element current = this.elements.get(this.current);


        float time = getTime();
        float elapsed = time - this.lastTime;

        if (1 <= elapsed) {
            if (current.max <= this.totalElapsed) {
                current.collapse(this.player);
                this.current = (this.current + 1) % this.elements.size();
                current = this.elements.get(this.current);
                this.totalElapsed = 0;
            } else {
                current.elapse(elapsed, this.player);
                this.totalElapsed += elapsed;
            }

            this.lastTime = time;
        }

        current.draw(graphics, mouseX, mouseY, this.player);

        PoseStack poseStack = graphics.pose();
        int fill = (int) (this.totalElapsed / current.max * 48);
        Renders.with(poseStack, () -> {
            poseStack.translate(0, 65, 500);
            graphics.renderOutline(0, 0, 50, 4, 0xFF404040);
            poseStack.translate(1, 1, 0);
            graphics.fill(0, 0, fill, 2, 0xFF8B8B8B);
        });

        this.drawFumetsu(graphics, mouseX, mouseY, 75, 60);
        this.drawArrow(graphics, 95, 40);

        long millis = Util.getMillis();
        if (100 < millis - this.lastMillis) {
            this.stars.clear();
            boolean xFlag = RANDOM.nextBoolean();
            for (int i = 0, j = RANDOM.nextInt(5, 9); i < j; i++) {
                int x = RANDOM.nextInt(120, 126) + (xFlag ? 16 : 0) + (i % 2) * RANDOM.nextInt(16, 19) * (xFlag ? -1 : 1);
                int yOffset = 50 - j * 4;
                int y = Math.round(yOffset + i * 6 + RANDOM.nextFloat() * 3);
                this.stars.add(new Star(x, y));
            }
        }

        this.lastMillis = millis;

        this.stars.forEach(star -> star.render(graphics));
    }

    private static class Star {
        private static final ItemStack STAR = new ItemStack(HyperItems.BUG_STARS.get(0).get());
        private final float speed = 0.5f + RANDOM.nextFloat() * 3;
        private final float xRand = rand();
        private final float yRand = rand();
        private final float zRand = rand();
        private final int x;
        private final int y;

        private Star(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private static float rand() {
            return (1 + RANDOM.nextFloat()) * 4 * (RANDOM.nextBoolean() ? 1 : -1);
        }

        private void render(GuiGraphics graphics) {
            float millis = Util.getMillis() * this.speed;
            float xRot = millis / 400 * this.xRand;
            float yRot = millis / 400 * this.yRand;
            float zRot = millis / 400 * this.zRand;
            PoseStack poseStack = graphics.pose();
            Renders.with(poseStack, () -> {
                poseStack.translate(this.x, this.y, 100);
                poseStack.scale(0.75f, 0.75f, 0.75f);
                poseStack.translate(8, 8, 150);
                poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
                poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
                poseStack.mulPose(Axis.ZP.rotationDegrees(zRot));
                poseStack.translate(-8, -8, -150);

                graphics.renderFakeItem(STAR, 0, 0);
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static abstract class Element {
        protected final int max;

        protected Element(int max) {
            this.max = max;
        }

        abstract void elapse(float elapsed, Player player);

        abstract void draw(GuiGraphics graphics, double mouseX, double mouseY, Player player);

        abstract void collapse(Player player);
    }

    @OnlyIn(Dist.CLIENT)
    private static class Logout extends Element {
        private static final Component RETURN_TO_MENU = Component.translatable("menu.returnToMenu");
        private static final Component DISCONNECT = Component.translatable("menu.disconnect");
        private final Button button;
        private float time = 0;

        protected Logout() {
            super(80);
            Minecraft mc = Minecraft.getInstance();
            Component component = mc.isLocalServer() ? RETURN_TO_MENU : DISCONNECT;
            int width = mc.font.width(component) + 4;
            this.button = Button.builder(component, button -> {
                    })
                    .pos(-width / 4 + 13, 90)
                    .width(width)
                    .build();
        }

        @Override
        void elapse(float elapsed, Player player) {
            if (this.time < 49 && 49 <= this.time + elapsed)
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            this.time += elapsed;
        }

        @Override
        void draw(GuiGraphics graphics, double mouseX, double mouseY, Player player) {
            if (50 <= this.time)
                return;

            int offsetX = 20;
            int offsetY = 60;

            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, offsetX, offsetY, 15, offsetX - (float) mouseX, offsetY - (float) mouseY, player);

            if (15 < this.time) {
                int mx;
                int my;

                if (40 < this.time) {
                    mx = this.button.getX();
                    my = this.button.getY();
                } else {
                    mx = Integer.MIN_VALUE;
                    my = Integer.MIN_VALUE;
                }

                PoseStack poseStack = graphics.pose();
                Renders.with(poseStack, () -> {
                    poseStack.scale(0.5f, 0.5f, 0.5f);
                    poseStack.translate(0, 0, 200);
                    this.button.render(graphics, mx, my, 1);
                });
            }
        }

        @Override
        void collapse(Player player) {
            this.time = 0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class Death extends Element {
        private float lastDead = 0;
        private boolean render = true;

        protected Death() {
            super(45);
        }

        @Override
        void elapse(float elapsed, Player player) {
            if (0 <= this.lastDead && player.deathTime < 20) {
                if (this.lastDead == 0) {
                    player.setHealth(0);
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_DEATH, 1 + (RANDOM.nextFloat() - RANDOM.nextFloat()) * 0.2f));
                }

                player.deathTime += Math.round(elapsed);
            }
            this.lastDead += elapsed;
            this.render = this.lastDead < 25;
        }

        @Override
        void draw(GuiGraphics graphics, double mouseX, double mouseY, Player player) {
            if (this.render) {
                int offsetX = 20;
                int offsetY = 60;

                InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, offsetX, offsetY, 15, offsetX - (float) mouseX, offsetY - (float) mouseY, player);
            }
        }

        @Override
        void collapse(Player player) {
            this.render = true;
            this.lastDead = -10;
            player.setHealth(player.getMaxHealth());
            player.deathTime = 0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class Warp extends Element {
        private float x = 0;

        protected Warp() {
            super(65);
        }

        @Override
        void elapse(float elapsed, Player player) {
            if (this.x == 0)
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PORTAL_TRIGGER, RANDOM.nextFloat() * 0.4f + 0.8f, 0.25f));
            this.x += elapsed * 0.85f;
        }

        @Override
        void draw(GuiGraphics graphics, double mouseX, double mouseY, Player player) {
            if (this.x < 45) {
                int offsetY = 60;

                graphics.pose().pushPose();
                graphics.pose().translate(this.x, 0, 0);
                InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, 0, offsetY, 15, this.x - (float) mouseX, offsetY - (float) mouseY, player);
                graphics.pose().popPose();
            }

            PoseStack poseStack = graphics.pose();
            Renders.with(poseStack, () -> {
                poseStack.translate(48, 38, 50);
                blockTransform(poseStack);
                this.drawPortal(graphics);
                poseStack.translate(0, -1, 0);
                this.drawPortal(graphics);
            });
        }

        @Override
        void collapse(Player player) {
            this.x = 0;
        }

        private void drawPortal(GuiGraphics graphics) {
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, Direction.Axis.Z), graphics.pose(), graphics.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }
    }
}
