package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Comparator;
import java.util.List;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

@OnlyIn(Dist.CLIENT)
public class VRXOverlay implements IGuiOverlay {
    private static final ResourceLocation OVERLAY = identifier("textures/gui/vrx_overlay.png");

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() == HitResult.Type.MISS)
            return;
        if (!player.getMainHandItem().is(HyperItems.VRX.get()) && !player.getOffhandItem().is(HyperItems.VRX.get()))
            return;

        List<VRXOne> ones = new ObjectArrayList<>();
        if (hit instanceof BlockHitResult result) {

            List<VRXSavedData.Entry> entries = VRXSavedData.get(mc.level).getEntries(result.getBlockPos());
            if (!entries.isEmpty()) {
                boolean flag = false;
                List<VRXOne> nulls = new ObjectArrayList<>();
                for (VRXSavedData.Entry entry : entries) {
                    if (!flag && entry.face == null) nulls.addAll(entry.contents);
                    else if (entry.face == result.getDirection()) {
                        flag = true;
                        ones.addAll(entry.contents);
                    }
                }

                if (!flag) {
                    ones.clear();
                    ones.addAll(nulls);
                }
//                if (entries.stream().anyMatch(entry -> entry.face == result.getDirection())) {
//                    entries.stream()
//                            .filter(entry -> entry.face == result.getDirection())
//                            .forEach(entry -> ones.addAll(entry.contents));
//                } else {
//                    entries.stream()
//                            .filter(entry -> entry.face == null)
//                            .forEach(entry -> ones.addAll(entry.contents));
//                }
            }
        } else if (hit instanceof EntityHitResult result) {
            Entity entity = result.getEntity();
            if (entity.getPersistentData().getBoolean(VRXHandler.TAG_HAS_VRX))
                entity.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> {
                    if (!vrx.getEntries().isEmpty())
                        vrx.getEntries().forEach(entry -> ones.addAll(entry.contents()));
                });
        }

        if (!ones.isEmpty()) {

            int gridWidth = Math.max(1, Mth.ceil(Math.sqrt(ones.size())));
            int gridHeight = Mth.ceil(((double) ones.size()) / (double) gridWidth);

            int index = 0;
            int left = (width / 2) - (gridWidth * 18 / 2);
            int top = (height / 2) - (gridHeight * 18 / 2);

            ones.sort(Comparator.comparingInt(one -> one.type.priority()));

            for (int dy = 0; dy < gridHeight; ++dy) {
                for (int dx = 0; dx < gridWidth; ++dx) {
                    if (index < ones.size()) {
                        VRXOne one = ones.get(index++);
                        int x = left + dx * 18;
                        int y = top + dy * 18;
                        Renders.with(graphics.pose(), () -> {
                            graphics.blit(OVERLAY, x, y, 0, 0, 18, 18, 18, 18);
                            one.render(graphics, x + 1, y + 1);
                        });
                    }
                }
            }
        }
    }
}
