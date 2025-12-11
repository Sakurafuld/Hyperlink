package com.sakurafuld.hyperdaimc.mixin.muteki;

import com.sakurafuld.hyperdaimc.content.hyper.muteki.MutekiHandler;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
@OnlyIn(Dist.CLIENT)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Nullable
    public Screen screen;

    @Shadow
    @Final
    private SoundManager soundManager;

    @Shadow
    @Final
    public MouseHandler mouseHandler;

    @Shadow
    public abstract void updateTitle();

    @Shadow
    @Nullable
    public ClientLevel level;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void setScreenMuteki(Screen pGuiScreen, CallbackInfo ci) {
        if (pGuiScreen instanceof DeathScreen || (pGuiScreen == null && this.level != null && this.player != null && this.player.isDeadOrDying())) {
            boolean novelized = NovelHandler.novelized(this.player);
            if (novelized && this.screen instanceof DeathScreen) ci.cancel();
            else if (!novelized && MutekiHandler.muteki(this.player)) {
                ci.cancel();
                for (float i = 1; this.player.getHealth() <= 0 && i <= 512; i++)
                    this.player.setHealth(i);
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Inject(method = "tick", at = @At("RETURN"))
    private void tickMuteki(CallbackInfo ci) {
        if (this.screen instanceof DeathScreen && this.player != null && MutekiHandler.muteki(this.player) && !NovelHandler.novelized(this.player)) {
            ForgeHooksClient.clearGuiLayers((Minecraft) (Object) this);
            this.screen = null;
            this.soundManager.resume();
            this.mouseHandler.grabMouse();
            this.updateTitle();
            for (float i = 1; this.player.getHealth() <= 0 && i <= 512; i++)
                this.player.setHealth(i);
        }
    }
}
