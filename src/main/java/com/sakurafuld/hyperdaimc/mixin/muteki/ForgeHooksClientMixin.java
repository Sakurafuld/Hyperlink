package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.system.NovelHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ForgeHooksClient.class)
@OnlyIn(Dist.CLIENT)
public abstract class ForgeHooksClientMixin {
    @Inject(method = "drawScreenInternal", at = @At("HEAD"), cancellable = true, remap = false)
    private static void drawScreenInternalMuteki(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (screen instanceof DeathScreen) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return;
            if (NovelHandler.novelized(player)) {
                ci.cancel();
//                MinecraftForge.EVENT_BUS.post(new ScreenEvent.Render.Pre(screen, guiGraphics, mouseX, mouseY, partialTick));
                screen.renderWithTooltip(guiGraphics, mouseX, mouseY, partialTick);
//                MinecraftForge.EVENT_BUS.post(new ScreenEvent.Render.Post(screen, guiGraphics, mouseX, mouseY, partialTick));
            } else if (MutekiHandler.muteki(player)) {
                ci.cancel();
                mc.getSoundManager().resume();
                mc.mouseHandler.grabMouse();
            }
        }
    }
}
