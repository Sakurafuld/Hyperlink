package com.sakurafuld.hyperdaimc.content.hyper.paradox.system;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxCapabilityItem;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxCapabilityPlayer;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxChain;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxChainCluster;
import com.sakurafuld.hyperdaimc.infrastructure.Boxes;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.paradox.ClientboundParadoxCursor;
import com.sakurafuld.hyperdaimc.network.paradox.ClientboundParadoxUnchainSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Set;

public class ParadoxActor {
    public static void select(ServerPlayer player, ParadoxCapabilityPlayer paradox) {
        BlockPos cursor = Boxes.getCursor(player, HyperCommonConfig.PARADOX_HIT_FLUID.get(), true);
        paradox.select(cursor);
        HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), ClientboundParadoxCursor.select(cursor));
    }

    public static void unselect(ServerPlayer player, ParadoxCapabilityPlayer paradox) {
        if (player.isShiftKeyDown()) {
            if (paradox.hasSelected()) {
                paradox.unselect();
                HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), ClientboundParadoxCursor.unselect());
            } else {
                Vec3 eye = player.getEyePosition();
                double reach = Math.max(player.getBlockReach(), player.getEntityReach());
                Vec3 view = eye.add(player.getViewVector(1).scale(reach));

                BlockPos target = BlockGetter.traverseBlocks(eye, view, Unit.INSTANCE, (unit, current) -> {
                    if (paradox.hasChain(current))
                        return current;
                    ServerLevel level = player.serverLevel();
                    BlockState state = level.getBlockState(current);
                    VoxelShape shape = state.getShape(level, current, CollisionContext.of(player));
                    BlockHitResult result = level.clipWithInteractionOverride(eye, view, current, shape, state);
                    if (result != null && result.getType() != HitResult.Type.MISS)
                        return Boxes.INVALID;
                    return null;
                }, unit -> Boxes.INVALID);

                if (target != Boxes.INVALID) {
                    paradox.unchain(target);
                    HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), ClientboundParadoxCursor.unchain(target));
                }
            }
        }
    }

    public static void unselectAndChain(ServerPlayer player, ParadoxCapabilityPlayer paradox) {
        BlockPos cursor = Boxes.getCursor(player, HyperCommonConfig.PARADOX_HIT_FLUID.get(), true);
        paradox.unselectAndChain(cursor);
        HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), ClientboundParadoxCursor.unselectAndChain(cursor));
    }

    public static void pick(ServerPlayer player, ParadoxCapabilityPlayer paradox) {
        player.getMainHandItem().getCapability(ParadoxCapabilityItem.TOKEN).ifPresent(paradoxItem -> {
            ServerLevel level = player.serverLevel();
            Vec3 eye = player.getEyePosition();
            Vec3 vector = player.getViewVector(1);
            double reach = Math.max(player.getBlockReach(), player.getEntityReach());
            Vec3 view = eye.add(vector.scale(reach));

            MutablePair<ParadoxChain, Direction> mutable = new MutablePair<>();
            BlockPos target = BlockGetter.traverseBlocks(eye, view, Unit.INSTANCE, (unit, current) -> {
                BlockState state = level.getBlockState(current);
                ParadoxChain chain = paradox.getChain(current);
                VoxelShape shape = state.getShape(level, current, CollisionContext.of(player));
                if (chain != null) {
                    BlockHitResult result;
                    if (state.isAir())
                        result = Shapes.block().clip(eye, view, current);
                    else
                        result = level.clipWithInteractionOverride(eye, view, current, shape, state);
                    mutable.setLeft(chain);
                    mutable.setRight(result != null ? result.getDirection().getOpposite() : Direction.getNearest(vector.x(), vector.y(), vector.z()));
                    return current;
                }

                BlockHitResult result = level.clipWithInteractionOverride(eye, view, current, shape, state);
                if (result != null && result.getType() != HitResult.Type.MISS)
                    return Boxes.INVALID;
                return null;

            }, unit -> Boxes.INVALID);

            if (mutable.getLeft() != null && mutable.getRight() != null) {
                Set<ParadoxChain> chains = ParadoxChain.find(paradox.getChains(), mutable.getLeft());
                ParadoxChainCluster cluster = new ParadoxChainCluster(chains, target, mutable.getRight(), player.getDirection());
                paradoxItem.setCluster(cluster);
                player.inventoryMenu.broadcastChanges();
                chains.forEach(paradox::unchain);
                HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundParadoxUnchainSet(chains));
                player.swing(InteractionHand.MAIN_HAND, true);
            }
        });
    }

    public static void unpick(ServerPlayer player, ParadoxCapabilityPlayer paradox) {
        player.getMainHandItem().getCapability(ParadoxCapabilityItem.TOKEN).ifPresent(paradoxItem -> {
            paradoxItem.setCluster(null);
            player.inventoryMenu.broadcastChanges();
        });
    }

    public static void perfectKnockout(ServerPlayer player, ParadoxCapabilityPlayer paradox) {
        ParadoxHandler.perfectKnockout(player, false);
    }

    public static void perfectKnockoutCluster(ServerPlayer player, ParadoxCapabilityPlayer paradox) {
        player.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradoxPlayer -> player.getMainHandItem().getCapability(ParadoxCapabilityItem.TOKEN).ifPresent(paradoxItem -> {
            ParadoxChainCluster cluster = paradoxItem.getCluster();
            if (cluster != null) {
                double reach = Math.max(player.getBlockReach(), player.getEntityReach());
                BlockPos at = Boxes.getCursor(player, HyperCommonConfig.PARADOX_HIT_FLUID.get(), true);
                Direction direction;
                if (player.pick(reach, 1, HyperCommonConfig.PARADOX_HIT_FLUID.get()) instanceof BlockHitResult result && result.getType() != HitResult.Type.MISS)
                    direction = result.getDirection().getOpposite();
                else
                    direction = Direction.orderedByNearest(player)[0];

//                if (!ParadoxHandler.canPerfectKnockout(player, player.serverLevel(), at, false))
//                    return;

                Set<ParadoxChain> chains = cluster.get(at, direction, player.getDirection());
                ParadoxHandler.gashacon(player, () -> ParadoxBomber.startPerfectKnockout(chains, at, player, paradoxPlayer, false));
            }
        }));
    }
}
