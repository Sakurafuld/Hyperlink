package com.sakurafuld.hyperdaimc.content.hyper.paradox.system;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxCapabilityItem;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxCapabilityPlayer;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxChain;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxChainCluster;
import com.sakurafuld.hyperdaimc.infrastructure.Boxes;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.paradox.ServerboundParadoxAction;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Objects;
import java.util.Optional;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class ParadoxTrigger {
    private static long lastKnockout = 0;
    private static long lastShiftClick = 0;
    private static long lastUnchain = 0;

    @SubscribeEvent(receiveCanceled = true)
    @OnlyIn(Dist.CLIENT)
    public static void bomber(InputEvent.InteractionKeyMappingTriggered event) {
        if (!HyperCommonConfig.ENABLE_PARADOX.get())
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = Objects.requireNonNull(mc.player);
        ClientLevel level = Objects.requireNonNull(mc.level);
        long millis = Util.getMillis();
        double reach = Math.max(Objects.requireNonNull(mc.gameMode).getPickRange(), player.getEntityReach());
        ItemStack mainHand = player.getMainHandItem();
        if (event.isAttack()) {
            if (mainHand.is(HyperItems.PARADOX.get())) {
                Optional<ParadoxCapabilityItem> capability = mainHand.getCapability(ParadoxCapabilityItem.TOKEN).resolve();
                if (capability.isPresent()) {
                    ParadoxCapabilityItem paradox = capability.get();
                    ParadoxChainCluster cluster = paradox.getCluster();
                    if (cluster != null) {
                        if (millis - lastKnockout > 250 || millis - lastShiftClick > 75) {
                            event.setCanceled(true);
                            HyperConnection.INSTANCE.sendToServer(new ServerboundParadoxAction(ServerboundParadoxAction.Action.PERFECT_KNOCKOUT_CLUSTER));
                            lastKnockout = millis;
                        }
                        lastShiftClick = millis;
                        return;
                    }
                }
            }
            if (ParadoxHandler.hasParadox(player)) {
                if (player.pick(reach, 1, HyperCommonConfig.PARADOX_HIT_FLUID.get()) instanceof BlockHitResult result && result.getType() != HitResult.Type.MISS) {
                    BlockPos pos = result.getBlockPos();
                    if (ParadoxBomber.canPerfectKnockout(player, level, pos, false)) {
                        event.setCanceled(true);
                        if (player.isShiftKeyDown() != HyperCommonConfig.PARADOX_INVERT_SHIFT.get()) {
                            if (millis - lastKnockout > 250 || millis - lastShiftClick > 75) {
                                HyperConnection.INSTANCE.sendToServer(new ServerboundParadoxAction(ServerboundParadoxAction.Action.PERFECT_KNOCKOUT));
                                lastKnockout = millis;
                            }
                            lastShiftClick = millis;
                        } else if (millis - lastKnockout > 250) {
                            HyperConnection.INSTANCE.sendToServer(new ServerboundParadoxAction(ServerboundParadoxAction.Action.PERFECT_KNOCKOUT));
                            player.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradox -> {
                                if (paradox.hasChain(pos))
                                    lastKnockout = millis;
                            });
                        }
                    }
                }
            }
            return;
        }

        if (mainHand.is(HyperItems.PARADOX.get())) {
            Vec3 vector = player.getViewVector(1);
            if (event.isUseItem()) {
                if (!player.isShiftKeyDown() && mc.hitResult instanceof EntityHitResult result && result.getType() != HitResult.Type.MISS)
                    return;
                player.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradoxPlayer -> {
                    if (player.isShiftKeyDown()) {
                        mainHand.getCapability(ParadoxCapabilityItem.TOKEN).ifPresent(paradoxItem -> {
                            if (paradoxItem.getCluster() != null) {
                                event.setCanceled(true);
                                event.setSwingHand(true);

                                player.playNotifySound(HyperSounds.PARADOX_UNCHAIN.get(), SoundSource.PLAYERS, 1, 1.5f);
                                HyperConnection.INSTANCE.sendToServer(new ServerboundParadoxAction(ServerboundParadoxAction.Action.UNPICK));
                            } else if (millis - lastUnchain > 200) {
                                boolean targeted;
                                if (paradoxPlayer.hasSelected()) {
                                    targeted = true;
                                } else {
                                    Vec3 eye = player.getEyePosition();
                                    Vec3 view = eye.add(vector.scale(reach));

                                    targeted = BlockGetter.traverseBlocks(eye, view, Unit.INSTANCE, (unit, current) -> {
                                        if (paradoxPlayer.hasChain(current))
                                            return current;
                                        BlockState state = level.getBlockState(current);
                                        VoxelShape shape = state.getShape(level, current, CollisionContext.of(player));
                                        BlockHitResult result = level.clipWithInteractionOverride(eye, view, current, shape, state);
                                        if (result != null && result.getType() != HitResult.Type.MISS)
                                            return Boxes.INVALID;
                                        return null;
                                    }, unit -> Boxes.INVALID) != Boxes.INVALID;
                                }

                                if (targeted) {
                                    event.setCanceled(true);
                                    event.setSwingHand(true);
                                    player.playNotifySound(HyperSounds.PARADOX_UNCHAIN.get(), SoundSource.PLAYERS, 1, 1);
                                    HyperConnection.INSTANCE.sendToServer(new ServerboundParadoxAction(ServerboundParadoxAction.Action.UNCHAIN));
                                    lastUnchain = millis;
                                }
                            }
                        });
                    } else if (paradoxPlayer.hasSelected()) {
                        event.setCanceled(true);
                        event.setSwingHand(true);
                        player.playNotifySound(HyperSounds.PARADOX_CHAIN.get(), SoundSource.PLAYERS, 1, 1);
                        HyperConnection.INSTANCE.sendToServer(new ServerboundParadoxAction(ServerboundParadoxAction.Action.UNSELECT_AND_CHAIN));
                    } else if (!level.isEmptyBlock(Boxes.getCursor(player, HyperCommonConfig.PARADOX_HIT_FLUID.get(), true))) {
                        event.setCanceled(true);
                        event.setSwingHand(true);
                        HyperConnection.INSTANCE.sendToServer(new ServerboundParadoxAction(ServerboundParadoxAction.Action.SELECT));

                        player.playNotifySound(HyperSounds.PARADOX_SELECT.get(), SoundSource.PLAYERS, 1, 1);
                    }
                });
            } /*else if (event.isPickBlock()) {
                player.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradoxPlayer -> mainHand.getCapability(ParadoxCapabilityItem.TOKEN).ifPresent(paradoxItem -> {
                    Vec3 eye = player.getEyePosition();
                    Vec3 view = eye.add(vector.scale(reach));

                    MutablePair<ParadoxChain, Direction> mutable = new MutablePair<>();
                    BlockGetter.traverseBlocks(eye, view, Unit.INSTANCE, (unit, current) -> {
                        BlockState state = level.getBlockState(current);
                        ParadoxChain chain = paradoxPlayer.getChain(current);
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
                        event.setCanceled(true);
                        player.playNotifySound(HyperSounds.PARADOX_UNCHAIN.get(), SoundSource.PLAYERS, 1, 1.5f);
                        HyperConnection.INSTANCE.sendToServer(new ServerboundParadoxAction(ServerboundParadoxAction.Action.PICK));
                    }
                }));
            }*/
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean pick() {
        if (!HyperCommonConfig.ENABLE_PARADOX.get())
            return false;

        MutableBoolean picked = new MutableBoolean();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = Objects.requireNonNull(mc.player);
        ClientLevel level = Objects.requireNonNull(mc.level);
        Vec3 vector = player.getViewVector(1);
        double reach = Math.max(Objects.requireNonNull(mc.gameMode).getPickRange(), player.getEntityReach());
        ItemStack mainHand = player.getMainHandItem();

        player.getCapability(ParadoxCapabilityPlayer.TOKEN).ifPresent(paradoxPlayer -> mainHand.getCapability(ParadoxCapabilityItem.TOKEN).ifPresent(paradoxItem -> {
            Vec3 eye = player.getEyePosition();
            Vec3 view = eye.add(vector.scale(reach));

            MutablePair<ParadoxChain, Direction> mutable = new MutablePair<>();
            BlockGetter.traverseBlocks(eye, view, Unit.INSTANCE, (unit, current) -> {
                BlockState state = level.getBlockState(current);
                ParadoxChain chain = paradoxPlayer.getChain(current);
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
                player.playNotifySound(HyperSounds.PARADOX_UNCHAIN.get(), SoundSource.PLAYERS, 1, 1.5f);
                HyperConnection.INSTANCE.sendToServer(new ServerboundParadoxAction(ServerboundParadoxAction.Action.PICK));
                picked.setTrue();
            }
        }));

        return picked.booleanValue();
    }
}
