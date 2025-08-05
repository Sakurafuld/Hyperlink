package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.helper.Renders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Comparator;
import java.util.List;

import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

@OnlyIn(Dist.CLIENT)
public class VRXOverlay implements IGuiOverlay {
    private static final ResourceLocation OVERLAY = identifier("textures/gui/vrx_overlay.png");

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();


        if (mc.hitResult == null || mc.hitResult.getType() == HitResult.Type.MISS) {
            return;
        }
        if (!mc.player.getMainHandItem().is(HyperItems.VRX.get()) && !mc.player.getOffhandItem().is(HyperItems.VRX.get())) {
            return;
        }


        List<VRXOne> ones = Lists.newArrayList();
        if (mc.hitResult instanceof BlockHitResult hit) {

            List<VRXSavedData.Entry> entries = VRXSavedData.get(mc.level).getEntries(hit.getBlockPos());
            if (!entries.isEmpty()) {

                if (entries.stream().anyMatch(entry -> entry.face == hit.getDirection())) {
                    entries.stream()
                            .filter(entry -> entry.face == hit.getDirection())
                            .forEach(entry -> ones.addAll(entry.contents));
                } else {
                    entries.stream()
                            .filter(entry -> entry.face == null)
                            .forEach(entry -> ones.addAll(entry.contents));
                }
            }
        } else if (mc.hitResult instanceof EntityHitResult hit) {
            hit.getEntity().getCapability(VRXCapability.CAPABILITY).ifPresent(vrx -> {
                if (!vrx.getEntries().isEmpty()) {
                    vrx.getEntries().forEach(entry -> {
                        ones.addAll(entry.contents);
                    });
                }
            });
        }

        if (!ones.isEmpty()) {

            int gridWidth = Math.max(1, Mth.ceil(Math.sqrt(ones.size())));
            int gridHeight = Mth.ceil(((double) ones.size()) / (double) gridWidth);

            int index = 0;
            int left = (width / 2) - (gridWidth * 18 / 2);
            int top = (height / 2) - (gridHeight * 18 / 2);

            ones.sort(Comparator.comparingInt(one -> one.type.getPriority()));

            for (int dy = 0; dy < gridHeight; ++dy) {
                for (int dx = 0; dx < gridWidth; ++dx) {
                    if (index < ones.size()) {
                        VRXOne one = ones.get(index++);
                        int x = left + dx * 18;
                        int y = top + dy * 18;
                        Renders.with(graphics.pose(), () -> {
//                            RenderSystem.setShaderTexture(0, OVERLAY);
                            graphics.blit(OVERLAY, x, y, 0, 0, 18, 18, 18, 18);
                            one.render(graphics, x + 1, y + 1);
                        });
                    }
                }
            }
        }
    }
}
