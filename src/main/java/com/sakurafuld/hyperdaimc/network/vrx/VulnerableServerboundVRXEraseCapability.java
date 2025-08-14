package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXCapability;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

@Deprecated
public class VulnerableServerboundVRXEraseCapability {
    private final int id;

    public VulnerableServerboundVRXEraseCapability(int id) {
        this.id = id;
    }

    public static void encode(VulnerableServerboundVRXEraseCapability msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.id);
    }

    public static VulnerableServerboundVRXEraseCapability decode(FriendlyByteBuf buf) {
        return new VulnerableServerboundVRXEraseCapability(buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        if (!HyperCommonConfig.VRX_VULNERABILIZATION.get()) {
            return;
        }
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            Entity entity = player.getLevel().getEntity(this.id);
            if (entity != null) {
                entity.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> {
                    vrx.erase(player.getUUID());
                    vrx.sync2Client(entity.getId(), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity));
                    VRXHandler.playSound(player.getLevel(), entity.position(), false);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
