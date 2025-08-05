package com.sakurafuld.hyperdaimc.content.over.materializer;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.api.content.IScreenVFX;
import com.sakurafuld.hyperdaimc.content.HyperBlockEntities;
import com.sakurafuld.hyperdaimc.helper.Renders;
import com.sakurafuld.hyperdaimc.helper.Writes;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

@OnlyIn(Dist.CLIENT)
public class MaterializerScreen extends AbstractContainerScreen<MaterializerMenu> {
    private static final Random RANDOM = new Random();
    private static final ResourceLocation BACKGROUND = identifier("textures/gui/container/materializer.png");

    private final Set<IScreenVFX> temporaryEffects = Sets.newHashSet();
    private int processTicks = 0;
    private int jump = 0;

    public MaterializerScreen(MaterializerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    public void jump() {
        this.jump = 6;
    }

    public void addVFX(IScreenVFX vfx) {
        this.temporaryEffects.add(vfx);
    }

    @Override
    protected void containerTick() {
        this.getMenu().access.execute(((level, pos) -> level.getBlockEntity(pos, HyperBlockEntities.MATERIALIZER.get()).ifPresent(materializer -> {
            ItemStack stack = materializer.getProcessItem();
            if (!stack.isEmpty() && this.getMenu().getFuelRemaining() > 0) {
                if (--this.processTicks <= 0) {
                    this.processTicks = RANDOM.nextInt(64);
                    this.renderables.add(0, new MaterializerItemVFX(this, stack));
                    this.getMenu().getSlot(0).getItem().setPopTime(3);
                    this.getMinecraft().player.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.MASTER, 0.15f, 0.01f);
                }
            } else {
                this.processTicks = 0;
                this.renderables.removeIf(widget -> {
                    if (widget instanceof MaterializerItemVFX vfx) {
                        vfx.clear();
                        return true;
                    } else {
                        return false;
                    }
                });
            }
        })));


        this.renderables.removeIf(widget -> widget instanceof IScreenVFX vfx && !vfx.tick());
        this.renderables.addAll(this.temporaryEffects);
        this.temporaryEffects.clear();

        --this.jump;

        ItemStack stack = this.getMenu().getSlot(0).getItem();
        if (stack.getPopTime() > 0) {
            stack.setPopTime(stack.getPopTime() - 1);
        }
    }

    @Override
    protected void clearWidgets() {
        List<Widget> vfx = this.renderables.stream()
                .filter(widget -> widget instanceof IScreenVFX)
                .toList();
        super.clearWidgets();
        this.renderables.addAll(vfx);
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
        blit(pPoseStack, this.getGuiLeft(), this.getGuiTop(), 0, 0, this.imageWidth, this.imageHeight);
        int color = (0xFF << 24) | Writes.gameOver("A").getSiblings().get(0).getStyle().getColor().getValue();
        int fuel = this.getMenu().getFuelGauge();
        if (fuel > 0) {
            fill(pPoseStack, this.getGuiLeft() + 12, this.getGuiTop() + 37, this.getGuiLeft() + 12 + fuel, this.getGuiTop() + 39, color);
        }
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, BACKGROUND);

        Renders.with(pPoseStack, () -> {
            pPoseStack.translate(0, 0, 50);
            fill(pPoseStack, 46, 62, 131, 65, 0xFF8B8B8B);
            int progress = this.getMenu().getProcessGauge();
            if (progress > 0) {
                blit(pPoseStack, 46, 56, 0, 166, progress, 9);
            }
            if (this.jump > 0) {
                int partial = Math.round(Mth.lerp(this.jump / 6f, 0x8B, 0xFF));
                int color = (partial << 24) | (partial << 16) | (partial << 8) | partial;
                vLine(pPoseStack, 132, 55, 65, color);
                vLine(pPoseStack, 133, 56, 65, color);
                vLine(pPoseStack, 134, 57, 65, color);
                vLine(pPoseStack, 135, 58, 65, color);
                vLine(pPoseStack, 136, 59, 65, color);
                vLine(pPoseStack, 137, 60, 65, color);
                vLine(pPoseStack, 138, 61, 65, color);
                vLine(pPoseStack, 139, 62, 65, color);
            }
        });

        super.renderLabels(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY) {
        super.renderTooltip(pPoseStack, pX, pY);
        if (isHovering(12, 37, 152, 2, pX, pY)) {
            String fuel = this.getMenu().getFuelMax() == 0 ? "-" : this.getMenu().getFuelRemaining() + " / " + this.getMenu().getFuelMax();
            this.renderTooltip(pPoseStack, List.of(Writes.gameOver(fuel)), Optional.empty(), pX, pY);
        }
    }
}