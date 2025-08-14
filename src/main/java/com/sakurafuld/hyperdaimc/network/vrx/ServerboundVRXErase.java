package com.sakurafuld.hyperdaimc.network.vrx;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXCapability;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXHandler;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ServerboundVRXErase {
    private final boolean block;

    public ServerboundVRXErase(boolean block) {
        this.block = block;
    }

    public static void encode(ServerboundVRXErase msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.block);
    }

    public static ServerboundVRXErase decode(FriendlyByteBuf buf) {
        return new ServerboundVRXErase(buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            double reach = Math.max(player.getReachDistance(), player.getAttackRange());
            Vec3 position = null;

            if (this.block) {
                if (player.pick(reach, 1, false) instanceof BlockHitResult hit && hit.getType() != HitResult.Type.MISS) {
                    BlockState state = player.getLevel().getBlockState(hit.getBlockPos());
                    if (state.hasBlockEntity()) {
                        VRXSavedData data = VRXSavedData.get(player.getLevel());
                        data.erase(player.getUUID(), hit.getBlockPos(), hit.getDirection());
                        data.sync2Client(PacketDistributor.DIMENSION.with(player.getLevel()::dimension));
                        position = Vec3.atCenterOf(hit.getBlockPos());
                    }
                }
            } else {
                Vec3 start = player.getEyePosition();
                Vec3 vector = player.getViewVector(1).scale(reach);
                Vec3 end = start.add(vector);
                AABB aabb = player.getBoundingBox().expandTowards(vector).inflate(1);
                EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, start, end, aabb, entity -> !entity.isSpectator() && entity.isPickable(), reach * reach);
                if (hit != null && hit.getType() != HitResult.Type.MISS && (HyperCommonConfig.VRX_PLAYER.get() || !(hit.getEntity() instanceof Player))) {
                    hit.getEntity().getCapability(VRXCapability.TOKEN).ifPresent(vrx -> {
                        vrx.erase(player.getUUID());
                        vrx.sync2Client(hit.getEntity().getId(), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(hit::getEntity));
                    });
                    position = hit.getEntity().position();
                }
            }

            if (position != null) {
                VRXHandler.playSound(player.getLevel(), position, false);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
