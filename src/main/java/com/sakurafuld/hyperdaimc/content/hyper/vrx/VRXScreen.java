package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXCloseMenu;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXScrollMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.Collections;
import java.util.Optional;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class VRXScreen extends AbstractContainerScreen<VRXMenu> {
    private static final ResourceLocation BACKGROUND = identifier("textures/gui/container/vrx.png");

    private boolean clicking = false;

    public VRXScreen(VRXMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageHeight = 168;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        InputConstants.Key key = InputConstants.getKey(pKeyCode, pScanCode);
        if (pKeyCode == InputConstants.KEY_ESCAPE || this.getMinecraft().options.keyInventory.isActiveAndMatches(key)) {
            HyperConnection.INSTANCE.sendToServer(new ServerboundVRXCloseMenu(this.getMenu().containerId));
            this.getMinecraft().player.swing(InteractionHand.MAIN_HAND);
            this.onClose();
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0 || pButton == 1) {
            this.clicking = true;
            boolean face = this.isHoveringFaceButton(pMouseX, pMouseY);
            boolean inventory = this.isHoveringInventoryButton(pMouseX, pMouseY);
            if (face || inventory) {
                int masked = ((face ? 0 : 1) << 1) | pButton;
                if (this.getMenu().clickMenuButton(this.getMinecraft().player, masked)) {
                    this.getMinecraft().player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.MASTER, 0.25f, 1);
                    this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, masked);
                    return true;
                }
            }
        }

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        this.clicking = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (this.hoveredSlot instanceof VRXSlot slot) {
            HyperConnection.INSTANCE.sendToServer(new ServerboundVRXScrollMenu(this.getMenu().containerId, ((Slot) slot).index, pDelta, hasShiftDown()));
            return slot.scrolled(pDelta, hasShiftDown());
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
//        RenderSystem.setShaderTexture(0, BACKGROUND);
        pGuiGraphics.blit(BACKGROUND, this.getGuiLeft(), this.getGuiTop(), 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        boolean hoveringInventory = this.isHoveringInventoryButton(pMouseX, pMouseY);
        pGuiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, hoveringInventory ? this.clicking ? 0x202020 : 0x606060 : 0x305050, false);

        int fontWidth = this.font.width(this.title.getString());
        int x = (this.imageWidth / 2) - (fontWidth / 2);
        pGuiGraphics.blitNineSliced(BACKGROUND, x - 2, this.titleLabelY - 1, fontWidth + 4, this.font.lineHeight + 1, 2, 2, 2, 2, 6, 10, 176, 0);
        pGuiGraphics.drawString(this.font, this.title, x, this.titleLabelY, 0xA0F0F0, false);

        MutableComponent component = this.getFaceButton();
        boolean hoveringFace = this.isHoveringFaceButton(pMouseX, pMouseY);
        pGuiGraphics.drawString(this.font, component, 168 - this.font.width(component), this.inventoryLabelY, hoveringFace ? this.clicking ? 0x202020 : 0x606060 : 0x305050, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        if (this.hoveredSlot instanceof VRXSlot slot) {
            slot.getOne().renderTooltip(this, pGuiGraphics, pX, pY);
        } else if (this.isHoveringInventoryButton(pX, pY)) {
            int index = this.getMenu().index + 1;
            int size = this.getMenu().indexes.size();
            ChatFormatting left = index > 1 ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY;
            ChatFormatting right = index < size ? ChatFormatting.GRAY : ChatFormatting.WHITE;
            pGuiGraphics.renderTooltip(this.font, Collections.singletonList(Component.translatable("tooltip.hyperdaimc.vrx.indexes", index, size, Component.translatable("tooltip.hyperdaimc.vrx.left").withStyle(left), Component.translatable("tooltip.hyperdaimc.vrx.right").withStyle(right)).withStyle(ChatFormatting.GRAY)), Optional.empty(), pX, pY);
        } else if (this.isHoveringFaceButton(pX, pY)) {
            pGuiGraphics.renderTooltip(this.font, Collections.singletonList(Component.translatable("tooltip.hyperdaimc.vrx.face", this.getFaceName(), this.title).withStyle(ChatFormatting.GRAY)), this.getMenu().getTooltip(), pX, pY);
        } else {
            super.renderTooltip(pGuiGraphics, pX, pY);
        }
    }


    public String getFaceName() {
        String face = this.getMenu().getCurrentFace() == null ? "null" : this.getMenu().getCurrentFace().name().toLowerCase();
        return I18n.get("tooltip.hyperdaimc.face." + face);
    }

    public MutableComponent getFaceButton() {
        return Component.translatable("container.hyperdaimc.vrx.face", this.getFaceName());
    }

    public boolean isHoveringFaceButton(double x, double y) {
        MutableComponent component = this.getFaceButton();
        int fontWidth = this.font.width(component);
        return this.isHovering(168 - fontWidth, this.inventoryLabelY, fontWidth, this.font.lineHeight, x, y);
    }

    public boolean isHoveringInventoryButton(double x, double y) {
        return this.isHovering(this.inventoryLabelX, this.inventoryLabelY, this.font.width(this.playerInventoryTitle), this.font.lineHeight, x, y);
    }
}
