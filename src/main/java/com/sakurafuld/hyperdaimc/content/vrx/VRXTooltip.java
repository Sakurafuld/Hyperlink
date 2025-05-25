package com.sakurafuld.hyperdaimc.content.vrx;

import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.Comparator;
import java.util.List;

public class VRXTooltip implements TooltipComponent {
    private final List<VRXOne> contents;

    public VRXTooltip(List<VRXOne> contents) {
        this.contents = contents.stream().sorted(Comparator.comparingInt(one -> one.type.getPriority())).toList();
    }

    @OnlyIn(Dist.CLIENT)
    public record Client(VRXTooltip tooltip) implements ClientTooltipComponent {
        private static final Component EMPTY = Component.translatable("tooltip.hyperdaimc.vrx_face_empty").withStyle(ChatFormatting.DARK_GRAY);

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
                pFont.drawInBatch(EMPTY, pX, pY + 2, -1, true, pMatrix4f, pBufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            }
        }

        @Override
        public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
            if (!this.tooltip().contents.isEmpty()) {
                int width = this.getGridWidth();
                int height = this.getGridHeight();

                int index = 0;
                for (int dx = 0; dx < height; ++dx) {
                    for (int dy = 0; dy < width; ++dy) {
                        int x = pX + dy * 18 + 1;
                        int y = pY + dx * 20 + 1;
                        this.renderSlot(pGuiGraphics, x, y, index++);
                    }
                }

                this.drawBorder(pGuiGraphics, pX, pY, width, height);
            }
        }

        private void blit(GuiGraphics graphics, int x, int y, Texture texture) {
            graphics.blit(ClientBundleTooltip.TEXTURE_LOCATION, x, y, 0, (float) texture.x, (float) texture.y, texture.w, texture.h, 128, 128);
        }

        private void renderSlot(GuiGraphics graphics, int x, int y, int index) {
            Renders.with(graphics.pose(), () -> {
                if (index >= this.tooltip().contents.size()) {
                    this.blit(graphics, x, y, Texture.SLOT);
                } else {
                    VRXOne one = this.tooltip.contents.get(index);
                    this.blit(graphics, x, y, Texture.SLOT);
                    if (!one.isEmpty()) {
                        graphics.pose().translate(0, 0, 0);
                        one.render(graphics, x + 1, y + 1);
                    }
                }
            });
        }

        private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height) {
            this.blit(graphics, x, y, Texture.BORDER_CORNER_TOP);
            this.blit(graphics, x + width * 18 + 1, y, Texture.BORDER_CORNER_TOP);

            for (int dx = 0; dx < width; ++dx) {
                this.blit(graphics, x + 1 + dx * 18, y, Texture.BORDER_HORIZONTAL_TOP);
                this.blit(graphics, x + 1 + dx * 18, y + height * 20, Texture.BORDER_HORIZONTAL_BOTTOM);
            }

            for (int dy = 0; dy < height; ++dy) {
                this.blit(graphics, x, y + dy * 20 + 1, Texture.BORDER_VERTICAL);
                this.blit(graphics, x + width * 18 + 1, y + dy * 20 + 1, Texture.BORDER_VERTICAL);
            }

            this.blit(graphics, x, y + height * 20, Texture.BORDER_CORNER_BOTTOM);
            this.blit(graphics, x + width * 18 + 1, y + height * 20, Texture.BORDER_CORNER_BOTTOM);
        }

        private int getGridWidth() {
            return Math.max(1, Mth.ceil(Math.sqrt(this.tooltip().contents.size())));
        }

        private int getGridHeight() {
            return Mth.ceil(((double) this.tooltip().contents.size()) / (double) this.getGridWidth());
        }

        enum Texture {
            SLOT(0, 0, 18, 20),
            BLOCKED_SLOT(0, 40, 18, 20),
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
