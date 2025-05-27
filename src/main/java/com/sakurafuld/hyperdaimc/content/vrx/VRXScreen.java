package com.sakurafuld.hyperdaimc.content.vrx;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sakurafuld.hyperdaimc.network.PacketHandler;
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

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

public class VRXScreen extends AbstractContainerScreen<VRXMenu> {
    private static final ResourceLocation BACKGROUND = identifier(HYPERDAIMC, "textures/gui/container/vrx.png");

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
            PacketHandler.INSTANCE.sendToServer(new ServerboundVRXCloseMenu(this.getMenu().containerId));
            this.getMinecraft().player.swing(InteractionHand.MAIN_HAND);
            this.onClose();
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        this.clicking = true;
        if (this.isHoveringFaceButton(pMouseX, pMouseY) && this.getMenu().clickMenuButton(this.getMinecraft().player, pButton)) {
            this.getMinecraft().player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.MASTER, 0.25f, 1);
            this.getMinecraft().gameMode.handleInventoryButtonClick(this.getMenu().containerId, pButton);
            return true;
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
            PacketHandler.INSTANCE.sendToServer(new ServerboundVRXScrollMenu(this.getMenu().containerId, ((Slot) slot).index, pDelta, hasShiftDown()));
            return slot.scrolled(this.getMenu(), pDelta, hasShiftDown());
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
        pGuiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
        int fontWidth = this.font.width(this.title.getString());
        int x = (this.imageWidth / 2) - (fontWidth / 2);
        pGuiGraphics.blitNineSliced(BACKGROUND, x - 2, this.titleLabelY - 1, fontWidth + 4, this.font.lineHeight + 1, 2, 2, 2, 2, 6, 10, 176, 0);
        pGuiGraphics.drawString(this.font, this.title, x, this.titleLabelY, 0xA0F0F0, false);

        MutableComponent component = this.getFaceButton();
        boolean hovering = this.isHoveringFaceButton(pMouseX, pMouseY);
        pGuiGraphics.drawString(this.font, component, 168 - this.font.width(component), this.inventoryLabelY, hovering ? this.clicking ? 0x202020 : 0x606060 : 0x305050, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        if (this.hoveredSlot instanceof VRXSlot slot) {
            slot.getOne().renderTooltip(this, pGuiGraphics, pX, pY);
        } else if (this.isHoveringFaceButton(pX, pY)) {
            pGuiGraphics.renderTooltip(this.font, Collections.singletonList(Component.translatable("tooltip.hyperdaimc.vrx_face", this.getFaceName()).withStyle(ChatFormatting.GRAY)), this.getMenu().getTooltip(), pX, pY);
        } else {
            super.renderTooltip(pGuiGraphics, pX, pY);
        }
    }


    public String getFaceName() {
        String face = this.getMenu().face == null ? "null" : this.getMenu().face.name().toLowerCase();
        return I18n.get("tooltip.hyperdaimc.face_" + face);
    }

    public MutableComponent getFaceButton() {
        return Component.translatable("container.hyperdaimc.vrx_face", this.getFaceName());
    }

    public boolean isHoveringFaceButton(double x, double y) {
        MutableComponent component = this.getFaceButton();
        int fontWidth = this.font.width(component);
        return this.isHovering(168 - fontWidth, this.inventoryLabelY, fontWidth, this.font.lineHeight, x, y);
    }
}
