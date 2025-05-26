package com.sakurafuld.hyperdaimc.content.paradox;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.api.mixin.ILootTableParadox;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.chronicle.ChronicleHandler;
import com.sakurafuld.hyperdaimc.network.PacketHandler;
import com.sakurafuld.hyperdaimc.network.paradox.ClientboundParadoxFluid;
import com.sakurafuld.hyperdaimc.network.paradox.ServerboundPerfectKnockout;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class ParadoxHandler {
    public static Player gashaconPlayer = null;
    private static long lastKnockout = 0;

    @SubscribeEvent(receiveCanceled = true)
    @OnlyIn(Dist.CLIENT)
    public static void mazaruUp(InputEvent.InteractionKeyMappingTriggered event) {
        if (!HyperServerConfig.ENABLE_PARADOX.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();

        HitResult hit = mc.player.pick(mc.player.getBlockReach(), 1, HyperServerConfig.PARADOX_HIT_FLUID.get());


        if (event.isAttack() && hasParadox(mc.player)
                && !(hit instanceof BlockHitResult result && (result.getType() == HitResult.Type.MISS || ChronicleHandler.isPaused(mc.level, result.getBlockPos(), mc.player)))) {

            event.setCanceled(true);
            if (mc.player.isShiftKeyDown() != HyperCommonConfig.PARADOX_INVERT_SHIFT.get()) {
                if (Util.getMillis() - lastKnockout > 300) {
                    PacketHandler.INSTANCE.sendToServer(new ServerboundPerfectKnockout());
                    lastKnockout = Util.getMillis();
                }
            } else {
                PacketHandler.INSTANCE.sendToServer(new ServerboundPerfectKnockout());
            }
        }
    }

    public static boolean isNotParadox(Entity entity) {
        return entity == null || !Objects.equals(entity, gashaconPlayer);
    }

    public static boolean hasParadox(Player player) {
        return HyperServerConfig.ENABLE_PARADOX.get() && player.getMainHandItem().is(HyperItems.PARADOX.get());
    }

    public static void perfectKnockout(ServerPlayer player) {
        HitResult hit = player.pick(player.getBlockReach(), 1, HyperServerConfig.PARADOX_HIT_FLUID.get());
        BlockPos pos;
        if (hit instanceof BlockHitResult result && result.getType() != HitResult.Type.MISS) {
            pos = result.getBlockPos();
        } else {
            return;
        }
        if (!hasParadox(player) || (HyperServerConfig.CHRONICLE_PARADOX.get() && ChronicleHandler.isPaused(player.level(), pos, player))) {
            return;
        }

        ServerLevel level = player.serverLevel();
        if (!level.isLoaded(pos)) {
            return;
        }
        BlockState blockState = level.getBlockState(pos);
        FluidState fluidState = level.getFluidState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        BlockState legacyState = blockState.getBlock() instanceof LiquidBlock ? Blocks.AIR.defaultBlockState() : fluidState.createLegacyBlock();

        if (blockState.isAir()) {
            player.connection.send(new ClientboundBlockUpdatePacket(pos, Blocks.AIR.defaultBlockState()));
            return;
        }

        gashaconPlayer = player;

        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, blockState, player);
        MinecraftForge.EVENT_BUS.post(event);
        int experience = event.getExpToDrop();

        if (player.isCreative()) {

            blockState.onDestroyedByPlayer(level, pos, player, false, fluidState);
            blockState.getBlock().destroy(level, pos, blockState);
            if (blockState.getBlock() instanceof LiquidBlock) {
                PacketHandler.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), new ClientboundParadoxFluid(pos, blockState));
            }
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(blockState));

        } else {

            blockState.onDestroyedByPlayer(level, pos, player, true, fluidState);
            if (blockState.getBlock() instanceof LiquidBlock) {
                PacketHandler.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), new ClientboundParadoxFluid(pos, blockState));
            }
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(blockState));
            blockState.getBlock().destroy(level, pos, blockState);
            blockState.getBlock().playerDestroy(level, player, pos, blockState, blockEntity, player.getMainHandItem());

            ResourceLocation loot = blockState.getBlock().getLootTable();
            if (loot == BuiltInLootTables.EMPTY || (level.getServer().getLootData().getLootTable(loot) instanceof ILootTableParadox table && table.isNoDrop())) {
                ItemStack stack = new ItemStack(blockState.getBlock());
                if (!stack.isEmpty()) {
                    Block.popResource(level, pos, stack);
                }
            }

            if (experience > 0) {
                blockState.getBlock().popExperience(level, pos, experience);
            }
        }

        if (blockState.getBlock() instanceof LiquidBlock) {
            level.destroyBlock(pos, true, player);
        }

        int sectionX = pos.getX() & 15;
        int sectionY = pos.getY() & 15;
        int sectionZ = pos.getZ() & 15;
        LevelChunk chunk = level.getChunkAt(pos);
        chunk.getSection(chunk.getSectionIndex(pos.getY())).setBlockState(sectionX, sectionY, sectionZ, legacyState);
        level.markAndNotifyBlock(pos, chunk, blockState, level.getBlockState(pos), 3, 512);
        level.getLightEngine().checkBlock(pos);
        chunk.setUnsaved(true);

        player.connection.send(new ClientboundBlockUpdatePacket(pos, legacyState));

        level.playSound(null, pos, HyperSounds.PARADOX.get(), SoundSource.PLAYERS, 0.75f, 1);

        gashaconPlayer = null;
    }
}
