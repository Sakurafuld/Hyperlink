package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import io.redspace.ironsspellbooks.registries.OverlayRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(OverlayRegistry.class)
@OnlyIn(Dist.CLIENT)
public abstract class OverlayRegistryMixin {
    @Redirect(method = "onRegisterOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/event/RegisterGuiOverlaysEvent;registerAbove(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;Lnet/minecraftforge/client/gui/overlay/IGuiOverlay;)V", ordinal = 1, remap = false), remap = false)
    private static void onRegisterOverlaysTicEx(RegisterGuiOverlaysEvent instance, ResourceLocation other, String id, IGuiOverlay overlay) {
        // ちょっとした修正.
        instance.registerBelowAll(id, overlay);
    }
}
