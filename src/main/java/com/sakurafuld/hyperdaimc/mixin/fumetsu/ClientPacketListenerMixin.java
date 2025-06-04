package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.content.fumetsu.FumetsuHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
@OnlyIn(Dist.CLIENT)
public abstract class ClientPacketListenerMixin {
    @Shadow
    private ClientLevel level;

    @Inject(method = "handleMoveEntity", at = @At("HEAD"))
    private void handleMoveEntityFumetsu$HEAD(ClientboundMoveEntityPacket pPacket, CallbackInfo ci) {
        if (this.level != null) {
            if (pPacket.getEntity(this.level) instanceof IFumetsu fumetsu) {
                fumetsu.setMovable(true);
            }
        }
    }

    @Inject(method = "handleMoveEntity", at = @At("RETURN"))
    private void handleMoveEntityFumetsu$RETURN(ClientboundMoveEntityPacket pPacket, CallbackInfo ci) {
        if (this.level != null) {
            if (pPacket.getEntity(this.level) instanceof IFumetsu fumetsu) {
                fumetsu.setMovable(false);
            }
        }
    }

    @Inject(method = "handleRotateMob", at = @At("HEAD"))
    private void handleRotateMobFumetsu$HEAD(ClientboundRotateHeadPacket pPacket, CallbackInfo ci) {
        if (this.level != null) {
            if (pPacket.getEntity(this.level) instanceof IFumetsu fumetsu) {
                fumetsu.setMovable(true);
            }
        }
    }

    @Inject(method = "handleRotateMob", at = @At("RETURN"))
    private void handleRotateMobFumetsu$RETURN(ClientboundRotateHeadPacket pPacket, CallbackInfo ci) {
        if (this.level != null) {
            if (pPacket.getEntity(this.level) instanceof IFumetsu fumetsu) {
                fumetsu.setMovable(false);
            }
        }
    }

    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"))
    private void handleSetEntityMotionFumetsu$HEAD(ClientboundSetEntityMotionPacket pPacket, CallbackInfo ci) {
        if (this.level != null) {
            if (this.level.getEntity(pPacket.getId()) instanceof IFumetsu fumetsu) {
                fumetsu.setMovable(true);
            }
        }
    }

    @Inject(method = "handleSetEntityMotion", at = @At("RETURN"))
    private void handleSetEntityMotionFumetsu$RETURN(ClientboundSetEntityMotionPacket pPacket, CallbackInfo ci) {
        if (this.level != null) {
            if (this.level.getEntity(pPacket.getId()) instanceof IFumetsu fumetsu) {
                fumetsu.setMovable(false);
            }
        }
    }

    @Inject(method = "handleRemoveEntities", at = @At("HEAD"))
    private void handleRemoveEntitiesFumetsu$HEAD(ClientboundRemoveEntitiesPacket pPacket, CallbackInfo ci) {
        FumetsuHandler.clientSpecialRemove = true;
    }

    @Inject(method = "handleRemoveEntities", at = @At("RETURN"))
    private void handleRemoveEntitiesFumetsu$RETURN(ClientboundRemoveEntitiesPacket pPacket, CallbackInfo ci) {
        FumetsuHandler.clientSpecialRemove = false;
    }
}
