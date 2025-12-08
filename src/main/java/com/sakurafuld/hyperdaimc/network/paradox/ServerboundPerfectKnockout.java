package com.sakurafuld.hyperdaimc.network.paradox;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerboundPerfectKnockout(boolean cluster) {
    public static void encode(ServerboundPerfectKnockout msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.cluster());
    }

    public static ServerboundPerfectKnockout decode(FriendlyByteBuf buf) {
        return new ServerboundPerfectKnockout(buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
//            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
//            if (this.cluster()) {
//                ItemStack mainHand = player.getMainHandItem();
//                if (mainHand.is(HyperItems.PARADOX.get())) {
//                    player.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradoxPlayer -> mainHand.getCapability(ParadoxCapabilityItem.TOKEN).ifPresent(paradoxItem -> {
//                        ParadoxChainCluster cluster = paradoxItem.getCluster();
//                        if (cluster != null) {
//                            double reach = Math.max(player.getBlockReach(), player.getEntityReach());
//                            BlockPos at = Boxes.getCursor(player, HyperCommonConfig.PARADOX_HIT_FLUID.get(), true);
//                            Direction direction;
//                            if (player.pick(reach, 1, HyperCommonConfig.PARADOX_HIT_FLUID.get()) instanceof BlockHitResult result && result.getType() != HitResult.Type.MISS)
//                                direction = result.getDirection().getOpposite();
//                            else
//                                direction = Direction.orderedByNearest(player)[0];
//
////                            if (!ParadoxHandler.canPerfectKnockout(player, player.serverLevel(), at, false))
////                                return;
//
//                            Set<ParadoxChain> chains = cluster.get(at, direction, player.getDirection());
//                            ParadoxHandler.gashacon(player, () -> ParadoxHandler.startPerfectKnockout(chains, at, player, paradoxPlayer, false));
//                        }
//                    }));
//                }
//            } else ParadoxHandler.perfectKnockout(player, false);
        });
        ctx.get().setPacketHandled(true);
    }
}
