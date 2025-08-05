package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXCloseMenu;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXScrollMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.GuiUtils;

import java.util.Collections;

import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

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
        this.clicking = true;
        if (this.isHoveringFaceButton(pMouseX, pMouseY) && this.getMenu().clickMenuButton(this.getMinecraft().player, pButton)) {
            this.getMinecraft().player.playNotifySound(SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, 0.25f, 1);
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
            HyperConnection.INSTANCE.sendToServer(new ServerboundVRXScrollMenu(this.getMenu().containerId, ((Slot) slot).index, pDelta, hasShiftDown()));
            return slot.scrolled(this.getMenu(), pDelta, hasShiftDown());
        }
        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        this.blit(pPoseStack, this.getGuiLeft(), this.getGuiTop(), 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        this.font.draw(pPoseStack, this.playerInventoryTitle, (float) this.inventoryLabelX, (float) this.inventoryLabelY, 0x404040);
        int fontWidth = this.font.width(this.title.getString());
        int x = (this.imageWidth / 2) - (fontWidth / 2);
        GuiUtils.drawContinuousTexturedBox(pPoseStack, BACKGROUND, x - 2, this.titleLabelY - 1, 176, 0, fontWidth + 4, this.font.lineHeight + 1, 6, 10, 2, 2, 2, 2, 0);
        this.font.drawShadow(pPoseStack, this.title, x, this.titleLabelY, 0xA0F0F0);

        TranslatableComponent component = this.getFaceButton();
        boolean hovering = this.isHoveringFaceButton(pMouseX, pMouseY);
        this.font.draw(pPoseStack, component, 168 - this.font.width(component), this.inventoryLabelY, hovering ? this.clicking ? 0x202020 : 0x606060 : 0x305050);
    }

    @Override
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY) {
        if (this.hoveredSlot instanceof VRXSlot slot) {
            slot.getOne().renderTooltip(this, pPoseStack, pX, pY);
        } else if (this.isHoveringFaceButton(pX, pY)) {
            this.renderTooltip(pPoseStack, Collections.singletonList(new TranslatableComponent("tooltip.hyperdaimc.vrx.face", this.getFaceName()).withStyle(ChatFormatting.GRAY)), this.getMenu().getTooltip(), pX, pY);
        } else {
            super.renderTooltip(pPoseStack, pX, pY);
        }
    }

    public void renderItemTooltip(PoseStack poseStack, int x, int y, ItemStack stack) {
        super.renderTooltip(poseStack, stack, x, y);
    }

    public String getFaceName() {
        String face = this.getMenu().face == null ? "null" : this.getMenu().face.name().toLowerCase();
        return I18n.get("tooltip.hyperdaimc.face." + face);
    }

    public TranslatableComponent getFaceButton() {
        return new TranslatableComponent("container.hyperdaimc.vrx.face", this.getFaceName());
    }

    public boolean isHoveringFaceButton(double x, double y) {
        TranslatableComponent component = this.getFaceButton();
        int fontWidth = this.font.width(component);
        return this.isHovering(168 - fontWidth, this.inventoryLabelY, fontWidth, this.font.lineHeight, x, y);
    }
}
