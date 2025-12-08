package com.sakurafuld.hyperdaimc.content.hyper.paradox;

import com.sakurafuld.hyperdaimc.infrastructure.Calculates;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public record ParadoxTooltip(List<ItemStack> contents) implements TooltipComponent {
    public ParadoxTooltip(List<ItemStack> contents) {
        this.contents = contents.stream().sorted(Calculates.HIGHEST_TO_LOWEST).toList();
    }

    @OnlyIn(Dist.CLIENT)
    public record Client(List<ItemStack> contents) implements ClientTooltipComponent {
        public Client(ParadoxTooltip tooltip) {
            this(tooltip.contents());
        }

        @Override
        public int getHeight() {
            if (this.contents.isEmpty()) {
                return 0;
            } else {
                return this.getGridHeight() * 20 + 2 + 4;
            }
        }

        @Override
        public int getWidth(Font pFont) {
            if (this.contents.isEmpty()) {
                return 0;
            } else {
                return this.getGridWidth() * 18 + 2;
            }
        }

        @Override
        public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
            if (!this.contents.isEmpty()) {
                int width = this.getGridWidth();
                int height = this.getGridHeight();

                int index = 0;
                for (int dx = 0; dx < height; ++dx) {
                    for (int dy = 0; dy < width; ++dy) {
                        int x = pX + dy * 18 + 1;
                        int y = pY + dx * 20 + 1;
                        this.renderSlot(pGuiGraphics, pFont, x, y, index++);
                    }
                }

                this.drawBorder(pGuiGraphics, pX, pY, width, height);
            }
        }

        private void blit(GuiGraphics graphics, int x, int y, ClientBundleTooltip.Texture texture) {
            graphics.blit(ClientBundleTooltip.TEXTURE_LOCATION, x, y, 0, (float) texture.x, (float) texture.y, texture.w, texture.h, 128, 128);
        }

        private void renderSlot(GuiGraphics graphics, Font font, int x, int y, int index) {
            Renders.with(graphics.pose(), () -> {
                if (index >= this.contents.size()) {
                    this.blit(graphics, x, y, ClientBundleTooltip.Texture.SLOT);
                } else {
                    ItemStack stack = this.contents.get(index);
                    this.blit(graphics, x, y, ClientBundleTooltip.Texture.SLOT);
                    if (!stack.isEmpty()) {
                        int sx = x + 1;
                        int sy = y + 1;

                        graphics.renderItem(stack, sx, sy);
                        graphics.renderItemDecorations(font, stack, sx, sy, "");
                        if (stack.getCount() != 1) {
                            graphics.pose().translate(0, 0, 200);
                            if (Screen.hasControlDown())
                                Renders.slotScaledString(graphics, String.valueOf(stack.getCount()), sx, sy);
                            else Renders.itemCount(graphics, stack.getCount(), sx, sy);
                        }
                    }
                }
            });
        }

        private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height) {
            this.blit(graphics, x, y, ClientBundleTooltip.Texture.BORDER_CORNER_TOP);
            this.blit(graphics, x + width * 18 + 1, y, ClientBundleTooltip.Texture.BORDER_CORNER_TOP);

            for (int dx = 0; dx < width; ++dx) {
                this.blit(graphics, x + 1 + dx * 18, y, ClientBundleTooltip.Texture.BORDER_HORIZONTAL_TOP);
                this.blit(graphics, x + 1 + dx * 18, y + height * 20, ClientBundleTooltip.Texture.BORDER_HORIZONTAL_BOTTOM);
            }

            for (int dy = 0; dy < height; ++dy) {
                this.blit(graphics, x, y + dy * 20 + 1, ClientBundleTooltip.Texture.BORDER_VERTICAL);
                this.blit(graphics, x + width * 18 + 1, y + dy * 20 + 1, ClientBundleTooltip.Texture.BORDER_VERTICAL);
            }

            this.blit(graphics, x, y + height * 20, ClientBundleTooltip.Texture.BORDER_CORNER_BOTTOM);
            this.blit(graphics, x + width * 18 + 1, y + height * 20, ClientBundleTooltip.Texture.BORDER_CORNER_BOTTOM);
        }

        private int getGridWidth() {
            return Math.max(1, Mth.ceil(Math.sqrt(this.contents.size())));
        }

        private int getGridHeight() {
            return Mth.ceil(((double) this.contents.size()) / (double) this.getGridWidth());
        }
    }
}
