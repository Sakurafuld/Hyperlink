package com.sakurafuld.hyperdaimc.content.hyper.chronicle.system;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleSavedData;
import com.sakurafuld.hyperdaimc.infrastructure.Boxes;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.chronicle.ServerboundChroniclePause;
import com.sakurafuld.hyperdaimc.network.chronicle.ServerboundChronicleRestart;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Objects;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = HYPERDAIMC, value = Dist.CLIENT)
public class ChronicleTrigger {
    private static long lastRestart = 0;

    @SubscribeEvent
    public static void pauseOrRestart(InputEvent.InteractionKeyMappingTriggered event) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get())
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = Objects.requireNonNull(mc.player);
        ClientLevel level = Objects.requireNonNull(mc.level);
        if (player.getMainHandItem().is(HyperItems.CHRONICLE.get())) {

            BlockPos cursor = ChronicleHandler.getCursorPos();
            ChronicleSavedData data = ChronicleSavedData.get(level);
            if (event.isUseItem()) {
                if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof ItemFrame)
                    return;

                event.setCanceled(true);
                event.setSwingHand(true);

                if (ChronicleHandler.selected == null) {
                    ChronicleHandler.selected = cursor;
                    level.playSound(player, ChronicleHandler.selected, HyperSounds.CHRONICLE_SELECT.get(), SoundSource.PLAYERS, 1, 2);
                } else if (data.check(player.getUUID(), ChronicleHandler.selected, cursor, error -> player.displayClientMessage(error, false))) {
                    HyperConnection.INSTANCE.sendToServer(new ServerboundChroniclePause(ChronicleHandler.selected, cursor));
                    ChronicleHandler.selected = null;
                }
            } else if (event.isAttack()) {
                if (ChronicleHandler.selected != null) {
                    event.setCanceled(true);
                    ChronicleHandler.selected = null;
                    lastRestart = Util.getMillis();
                    level.playSound(player, cursor, HyperSounds.CHRONICLE_RESTART.get(), SoundSource.PLAYERS, 1, 1);
                } else if (Util.getMillis() - lastRestart > 250) {
                    Vec3 eye = player.getEyePosition();
                    double reach = Math.max(Objects.requireNonNull(mc.gameMode).getPickRange(), player.getEntityReach());
                    Vec3 view = eye.add(player.getViewVector(1).scale(reach));

                    BlockPos target = BlockGetter.traverseBlocks(eye, view, Unit.INSTANCE, (unit, current) -> {
                        List<ChronicleSavedData.Entry> paused = data.getPaused(current);
                        if (paused == null || paused.isEmpty()) {
                            BlockState state = level.getBlockState(current);
                            VoxelShape shape = state.getShape(level, current, CollisionContext.of(player));
                            BlockHitResult result = level.clipWithInteractionOverride(eye, view, current, shape, state);
                            if (result != null && result.getType() != HitResult.Type.MISS) return Boxes.INVALID;
                            return null;
                        }

                        for (ChronicleSavedData.Entry entry : paused)
                            if (entry.uuid.equals(player.getUUID()))
                                return current;

                        return null;
                    }, unit -> Boxes.INVALID);

                    if (target != Boxes.INVALID) {
                        event.setCanceled(true);
                        HyperConnection.INSTANCE.sendToServer(new ServerboundChronicleRestart(target));
                        lastRestart = Util.getMillis();
                    }
                }
            }
        }
    }
}
