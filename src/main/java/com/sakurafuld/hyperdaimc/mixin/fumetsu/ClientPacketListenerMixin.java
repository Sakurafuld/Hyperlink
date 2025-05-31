package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow
    private ClientLevel level;

    @Inject(method = "handleMoveEntity", at = @At("HEAD"))
    private void handleMoveEntityFumetsu$HEAD(ClientboundMoveEntityPacket pPacket, CallbackInfo ci) {
        if (pPacket.getEntity(this.level) instanceof IFumetsu fumetsu) {
            fumetsu.setMovable(true);
        }
    }

    @Inject(method = "handleMoveEntity", at = @At("RETURN"))
    private void handleMoveEntityFumetsu$RETURN(ClientboundMoveEntityPacket pPacket, CallbackInfo ci) {
        if (pPacket.getEntity(this.level) instanceof IFumetsu fumetsu) {
            fumetsu.setMovable(false);
        }
    }

    @Inject(method = "handleRotateMob", at = @At("HEAD"))
    private void handleRotateMobFumetsu$HEAD(ClientboundRotateHeadPacket pPacket, CallbackInfo ci) {
        if (pPacket.getEntity(this.level) instanceof IFumetsu fumetsu) {
            fumetsu.setMovable(true);
        }
    }

    @Inject(method = "handleRotateMob", at = @At("RETURN"))
    private void handleRotateMobFumetsu$RETURN(ClientboundRotateHeadPacket pPacket, CallbackInfo ci) {
        if (pPacket.getEntity(this.level) instanceof IFumetsu fumetsu) {
            fumetsu.setMovable(false);
        }
    }

    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"))
    private void handleSetEntityMotionFumetsu$HEAD(ClientboundSetEntityMotionPacket pPacket, CallbackInfo ci) {
        if (this.level.getEntity(pPacket.getId()) instanceof IFumetsu fumetsu) {
            fumetsu.setMovable(true);
        }
    }

    @Inject(method = "handleSetEntityMotion", at = @At("RETURN"))
    private void handleSetEntityMotionFumetsu$RETURN(ClientboundSetEntityMotionPacket pPacket, CallbackInfo ci) {
        if (this.level.getEntity(pPacket.getId()) instanceof IFumetsu fumetsu) {
            fumetsu.setMovable(false);
        }
    }
}
