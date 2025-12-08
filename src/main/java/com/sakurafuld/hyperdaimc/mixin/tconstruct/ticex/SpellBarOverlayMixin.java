package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sakurafuld.hyperdaimc.addon.tconstruct.HyperModifiers;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatItemRenderer;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.gui.overlays.ManaBarOverlay;
import io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.model.EmptyModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.List;
import java.util.Random;
import java.util.Set;

@Pseudo
@Mixin(SpellBarOverlay.class)
@OnlyIn(Dist.CLIENT)
public abstract class SpellBarOverlayMixin {
    @Unique
    private static final Random RANDOM = new Random();
    @Unique
    private static final Set<GashatItemRenderer.Particle> PARTICLES = Sets.newHashSet();
    @Unique
    private boolean gashatting = false;

    @Inject(locals = LocalCapture.CAPTURE_FAILSOFT, method = "render", at = @At(value = "INVOKE", target = "Lio/redspace/ironsspellbooks/api/magic/SpellSelectionManager;getGlobalSelectionIndex()I", shift = At.Shift.AFTER, remap = false), remap = false)
    private void renderTicEx$0(ForgeGui gui, GuiGraphics guiHelper, float partialTick, int screenWidth, int screenHeight, CallbackInfo ci, Player player, ManaBarOverlay.Display displayMode, SpellSelectionManager ssm, int centerX, int centerY, int configOffsetY, int configOffsetX, SpellBarOverlay.Anchor anchor, List<SpellData> spells, int spellbookCount, List<Vec2> locations, int approximateWidth) {
        ItemStack spellbook = Utils.getPlayerSpellbookStack(player);
        if (spellbook != null && !spellbook.isEmpty() && spellbook.getItem() instanceof IModifiable) {
            ToolStack tool = ToolStack.from(spellbook);
            if (this.isGashat(tool)) {
                PoseStack poseStack = guiHelper.pose();
                this.gashatting = true;
                poseStack.pushPose();
                try {
                    poseStack.translate(centerX, centerY, 0);
                    int halfWidth = approximateWidth * 5;
                    for (int count = 0; count < 10; count++)
                        if (RANDOM.nextInt(200) == 0) {
                            float x = RANDOM.nextFloat() * halfWidth * 2;
                            float y = RANDOM.nextFloat() * halfWidth * 2;
                            PARTICLES.add(new GashatItemRenderer.Particle(spellbook, () -> EmptyModel.BAKED, p -> {
                                p.translate(-halfWidth, -halfWidth, 0);
                                p.translate(x, y, 0);
                                p.scale(32, 32, 1);
                            }));
                        }
                    Renders.with(poseStack, () -> PARTICLES.removeIf(particle -> particle.render(spellbook, ItemDisplayContext.NONE, poseStack, Renders.bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY)));

                    float cos = Mth.cos(Util.getMillis() / 800f);
                    poseStack.mulPose(Axis.ZP.rotationDegrees(cos * 8));
                    poseStack.mulPose(Axis.XP.rotationDegrees(cos * 2));
                    poseStack.mulPose(Axis.YP.rotationDegrees(cos * 2));
                    poseStack.translate(-centerX, -centerY, 0);
                } catch (Throwable ignored) {
                    poseStack.popPose();
                    this.gashatting = false;
                }
            }
        }
    }

    @Inject(method = "render", at = @At("RETURN"), remap = false)
    private void renderTicEx$RETURN(ForgeGui gui, GuiGraphics guiHelper, float partialTick, int screenWidth, int screenHeight, CallbackInfo ci) {
        if (this.gashatting) {
            guiHelper.pose().popPose();
            this.gashatting = false;
        }
    }

    @Unique
    private boolean isGashat(ToolStack tool) {
        return !tool.isBroken() && (tool.getModifierLevel(HyperModifiers.NOVEL.getId()) > 0 || tool.getModifierLevel(HyperModifiers.PARADOX.getId()) > 0);
    }
}
