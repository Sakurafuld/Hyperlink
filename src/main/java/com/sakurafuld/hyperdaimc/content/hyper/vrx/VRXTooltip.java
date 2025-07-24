package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Comparator;
import java.util.List;

public class VRXTooltip implements TooltipComponent {
    private final List<VRXOne> contents;

    public VRXTooltip(List<VRXOne> contents) {
        this.contents = contents.stream().sorted(Comparator.comparingInt(one -> one.type.getPriority())).toList();
    }

    @OnlyIn(Dist.CLIENT)
    public record Client(VRXTooltip tooltip) implements ClientTooltipComponent {
        private static final Component EMPTY = new TranslatableComponent("tooltip.hyperdaimc.vrx.face.empty").withStyle(ChatFormatting.DARK_GRAY);

        @Override
        public int getHeight() {
            if (this.tooltip().contents.isEmpty()) {
                return 14;
            } else {
                return this.getGridHeight() * 20 + 2 + 4;
            }
        }

        @Override
        public int getWidth(Font pFont) {
            if (this.tooltip().contents.isEmpty()) {
                return pFont.width(EMPTY);
            } else {
                return this.getGridWidth() * 18 + 2;
            }
        }

        @Override
        public void renderText(Font pFont, int pX, int pY, Matrix4f pMatrix4f, MultiBufferSource.BufferSource pBufferSource) {
            if (this.tooltip().contents.isEmpty()) {
                pFont.drawInBatch(EMPTY, pX, pY + 2, -1, true, pMatrix4f, pBufferSource, false, 0, LightTexture.FULL_BRIGHT);
            }
        }

        @Override
        public void renderImage(Font pFont, int pMouseX, int pMouseY, PoseStack pPoseStack, ItemRenderer pItemRenderer, int pBlitOffset) {
            if (!this.tooltip().contents.isEmpty()) {
                int width = this.getGridWidth();
                int height = this.getGridHeight();

                int index = 0;
                for (int dx = 0; dx < height; ++dx) {
                    for (int dy = 0; dy < width; ++dy) {
                        int x = pMouseX + dy * 18 + 1;
                        int y = pMouseY + dx * 20 + 1;
                        this.renderSlot(pPoseStack, x, y, index++, pBlitOffset);
                    }
                }

                this.drawBorder(pPoseStack, pMouseX, pMouseY, width, height, pBlitOffset);
            }
        }

        private void blit(PoseStack poseStack, int x, int y, int blitOffset, Texture texture) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setShaderTexture(0, ClientBundleTooltip.TEXTURE_LOCATION);
            GuiComponent.blit(poseStack, x, y, blitOffset, (float) texture.x, (float) texture.y, texture.w, texture.h, 128, 128);
        }

        private void renderSlot(PoseStack poseStack, int x, int y, int index, int blitOffset) {
            Renders.with(poseStack, () -> {
                if (index >= this.tooltip().contents.size()) {
                    this.blit(poseStack, x, y, blitOffset, Texture.SLOT);
                } else {
                    VRXOne one = this.tooltip.contents.get(index);
                    this.blit(poseStack, x, y, blitOffset, Texture.SLOT);
                    if (!one.isEmpty()) {
                        poseStack.translate(0, 0, blitOffset);
                        one.render(poseStack, x + 1, y + 1);
                    }
                }
            });
        }

        private void drawBorder(PoseStack poseStack, int x, int y, int width, int height, int blitOffset) {
            this.blit(poseStack, x, y, blitOffset, Texture.BORDER_CORNER_TOP);
            this.blit(poseStack, x + width * 18 + 1, y, blitOffset, Texture.BORDER_CORNER_TOP);

            for (int dx = 0; dx < width; ++dx) {
                this.blit(poseStack, x + 1 + dx * 18, y, blitOffset, Texture.BORDER_HORIZONTAL_TOP);
                this.blit(poseStack, x + 1 + dx * 18, y + height * 20, blitOffset, Texture.BORDER_HORIZONTAL_BOTTOM);
            }

            for (int dy = 0; dy < height; ++dy) {
                this.blit(poseStack, x, y + dy * 20 + 1, blitOffset, Texture.BORDER_VERTICAL);
                this.blit(poseStack, x + width * 18 + 1, y + dy * 20 + 1, blitOffset, Texture.BORDER_VERTICAL);
            }

            this.blit(poseStack, x, y + height * 20, blitOffset, Texture.BORDER_CORNER_BOTTOM);
            this.blit(poseStack, x + width * 18 + 1, y + height * 20, blitOffset, Texture.BORDER_CORNER_BOTTOM);
        }

        private int getGridWidth() {
            return Math.max(1, Mth.ceil(Math.sqrt(this.tooltip().contents.size())));
        }

        private int getGridHeight() {
            return Mth.ceil(((double) this.tooltip().contents.size()) / (double) this.getGridWidth());
        }

        enum Texture {
            SLOT(0, 0, 18, 20),
            BORDER_VERTICAL(0, 18, 1, 20),
            BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
            BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
            BORDER_CORNER_TOP(0, 20, 1, 1),
            BORDER_CORNER_BOTTOM(0, 60, 1, 1);

            public final int x;
            public final int y;
            public final int w;
            public final int h;

            Texture(int pX, int pY, int pW, int pH) {
                this.x = pX;
                this.y = pY;
                this.w = pW;
                this.h = pH;
            }
        }
    }
}
