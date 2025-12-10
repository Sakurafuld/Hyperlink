package com.sakurafuld.hyperdaimc.content.hyper.chronicle.system;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.ChronicleSavedData;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.system.ParadoxHandler;
import com.sakurafuld.hyperdaimc.infrastructure.Boxes;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.chronicle.ClientboundChronicleHitEffect;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class ChronicleHandler {
    public static boolean chunkGenerating = false;
    public static boolean clientForceNonPaused = false;
    public static BlockPos selected = null;

    public static void hitEffect(BlockPos pos) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ChronicleRenderer.hits.put(pos.asLong(), Util.getMillis()));
    }

    public static boolean isPaused(Level level, BlockPos pos, @Nullable Entity entity, boolean force) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get())
            return false;
        if (chunkGenerating)
            return false;
        if (level.isClientSide() && clientForceNonPaused)
            return false;

        if (force || HyperCommonConfig.CHRONICLE_PARADOX.get() || ParadoxHandler.isNotParadox(entity)) {
            List<ChronicleSavedData.Entry> paused = ChronicleSavedData.get(level).getPaused(pos);
            if (paused == null || paused.isEmpty())
                return false;
            if (HyperCommonConfig.CHRONICLE_OWNER.get())
                return true;
            if (!(entity instanceof Player))
                return true;

            for (ChronicleSavedData.Entry entry : paused)
                if (!entry.uuid.equals(entity.getUUID()))
                    return true;
        }
        return false;
    }

    public static boolean isPaused(Level level, BlockPos pos, @Nullable Entity entity) {
        return isPaused(level, pos, entity, false);
    }

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            ChronicleSavedData.get(player.level()).sync2Client(PacketDistributor.PLAYER.with(() -> player));
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player)
            ChronicleSavedData.get(player.level()).sync2Client(PacketDistributor.PLAYER.with(() -> player));
    }

    @OnlyIn(Dist.CLIENT)
    public static BlockPos getCursorPos() {
        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        LocalPlayer player = Objects.requireNonNull(mc.player);
        ClientLevel level = Objects.requireNonNull(mc.level);

        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            Vec3 view = player.getViewVector(1).multiply(4, 4, 4);
            return Boxes.clamp(level, BlockPos.containing(player.getEyePosition().add(view)));
        } else if (hit instanceof BlockHitResult result) {
            BlockPos pos = result.getBlockPos().immutable();
            if (player.isShiftKeyDown() != HyperCommonConfig.CHRONICLE_INVERT_SHIFT.get())
                return Boxes.clamp(level, pos.relative(result.getDirection()));
            else return pos;
        } else return BlockPos.containing(hit.getLocation());
    }

    public static void playSound(ServerLevel level, Vec3 position, boolean pause) {
        if (pause)
            level.playSound(null, position.x(), position.y(), position.z(), HyperSounds.CHRONICLE_PAUSE.get(), SoundSource.PLAYERS, 1, 1);
        else
            level.playSound(null, position.x(), position.y(), position.z(), HyperSounds.CHRONICLE_RESTART.get(), SoundSource.PLAYERS, 1, 1);
    }

    @SubscribeEvent
    public static void pause(EntityJoinLevelEvent event) {
        if ((event.getEntity() instanceof PrimedTnt || event.getEntity() instanceof FallingBlockEntity) && isPaused(event.getLevel(), event.getEntity().blockPosition(), null))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void pause(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (isPaused(level, pos, event.getEntity())) {
            event.setCanceled(true);
            if (level.isClientSide())
                hitEffect(pos);
        }
    }

    @SubscribeEvent
    public static void pause(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (HyperCommonConfig.CHRONICLE_INTERACT.get() && isPaused(level, pos, event.getEntity())) {
            event.setCanceled(true);
            event.getEntity().swing(event.getHand());
            if (level.isClientSide())
                hitEffect(pos);
        }
    }

    @SubscribeEvent
    public static void pause(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof Level level && isPaused(level, event.getPos(), event.getPlayer()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void pause(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level) {
            BlockPos pos = event.getPos();
            if (isPaused(level, pos, event.getEntity())) {
                BlockSnapshot snapshot = event.getBlockSnapshot();
                if (!snapshot.getReplacedBlock().is(snapshot.getCurrentBlock().getBlock())) {
                    event.setCanceled(true);
                    if (!level.isClientSide())
                        HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), new ClientboundChronicleHitEffect(pos));
                }
            }
        }
    }

    @SubscribeEvent
    public static void pause(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getLevel() instanceof Level level) {
            BlockPos pos = event.getPos();
            if (isPaused(level, pos, null)) {
                event.setCanceled(true);
                if (!level.isClientSide())
                    HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), new ClientboundChronicleHitEffect(pos));
            }
        }
    }

    @SubscribeEvent
    public static void pause(PistonEvent.Pre event) {
        if (event.getLevel() instanceof Level level) {
            if (isPaused(level, event.getPos(), null))
                event.setCanceled(true);
            else if (!event.getState().isAir() && isPaused(level, event.getFaceOffsetPos(), null))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void pause(LivingDestroyBlockEvent event) {
        LivingEntity entity = event.getEntity();
        BlockPos pos = event.getPos();
        Level level = entity.level();
        if (isPaused(level, pos, entity)) {
            event.setCanceled(true);
            if (!level.isClientSide())
                HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), new ClientboundChronicleHitEffect(pos));
        }
    }

    @SubscribeEvent
    public static void pause(ExplosionEvent.Detonate event) {
        Level level = event.getLevel();
        if (level.isClientSide())
            for (BlockPos pos : event.getAffectedBlocks())
                if (isPaused(level, pos, null))
                    hitEffect(pos);
    }
}
