package com.sakurafuld.hyperdaimc.content.hyper.paradox.system;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.chronicle.system.ChronicleHandler;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxCapabilityPlayer;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.ParadoxChain;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.ILootTableParadox;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.paradox.ClientboundPerfectKnockedoutParticles;
import com.sakurafuld.hyperdaimc.network.paradox.ClientboundPerfectKnockedoutUpdates;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.LOG;

public class ParadoxBomber {
    public static boolean canPerfectKnockout(Player player, Level level, BlockPos pos, boolean skipPaused) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir())
            return false;
        //noinspection deprecation
        if (!HyperCommonConfig.PARADOX_HIT_FLUID.get() && (state.liquid() || state.getBlock() instanceof LiquidBlock))
            return false;
        if (!level.isLoaded(pos))
            return false;
        return skipPaused || !ChronicleHandler.isPaused(level, pos, player, true);
    }

    public static void startPerfectKnockout(Set<ParadoxChain> allChains, BlockPos cursor, ServerPlayer player, ParadoxCapabilityPlayer paradox, boolean skipPaused) {
        List<BlockPos> allPos = ParadoxChain.connect(allChains, cursor, player, skipPaused);
        if (allPos.isEmpty())
            return;
        int maxSize = allPos.size();
        ParadoxHandler.captureAndTransfer(player, () -> {
            long prePKC = System.currentTimeMillis();
            perfectKnockoutChaining(player, allPos, skipPaused);
            LOG.info("[Pickdox] PKC {}ms lagged for {}blocks", System.currentTimeMillis() - prePKC, maxSize - allPos.size());
        });
        paradox.start(allPos, skipPaused, maxSize);
        paradox.sync2Client(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player));
    }

    public static void perfectKnockoutChaining(ServerPlayer player, List<BlockPos> area, boolean skipPaused) {
        ServerLevel level = player.serverLevel();
        Long2IntOpenHashMap blocks = new Long2IntOpenHashMap();

        final int max = HyperCommonConfig.PARADOX_DESTROY_AT_ONCE.get();
        ListIterator<BlockPos> iterator = area.listIterator();
        while (iterator.hasNext()) {
            if (blocks.size() >= max)
                break;
            BlockPos pos = iterator.next();
            iterator.remove();
            if (!canPerfectKnockout(player, level, pos, skipPaused))
                continue;
            BlockState currentBlock = level.getBlockState(pos);

            blocks.put(pos.asLong(), Block.getId(currentBlock));
            FluidState currentFluid = level.getFluidState(pos);
            BlockEntity currentTile = level.getBlockEntity(pos);
            BlockState legacyBlock = currentBlock.getBlock() instanceof LiquidBlock ? Blocks.AIR.defaultBlockState() : currentFluid.createLegacyBlock();

            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, currentBlock, player);
            MinecraftForge.EVENT_BUS.post(event);

            boolean creative = player.isCreative();

            if (!creative) player.getMainHandItem().mineBlock(level, currentBlock, pos, player);
            currentBlock.onDestroyedByPlayer(level, pos, player, !creative, currentFluid);
            perfectKnockoutAbsolute(level, pos, currentBlock, legacyBlock);
            currentBlock.getBlock().destroy(level, pos, currentBlock);

            if (!creative) {
                float exhaustion = player.getFoodData().getExhaustionLevel();
                currentBlock.getBlock().playerDestroy(level, player, pos, currentBlock, currentTile, player.getMainHandItem());
                player.getFoodData().setExhaustion(exhaustion);

                ResourceLocation loot = currentBlock.getBlock().getLootTable();
                if (loot == BuiltInLootTables.EMPTY || (level.getServer().getLootData().getLootTable(loot) instanceof ILootTableParadox table && table.hyperdaimc$hasNoDrop())) {
                    ItemStack stack = new ItemStack(currentBlock.getBlock());
                    if (!stack.isEmpty()) Block.popResource(level, pos, stack);
                }

                int experience = event.getExpToDrop();
                if (experience > 0) currentBlock.getBlock().popExperience(level, pos, experience);
            }
        }

        if (!blocks.isEmpty()) {
            HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundPerfectKnockedoutUpdates(blocks.keySet().toLongArray()));
            if (!HyperCommonConfig.PARADOX_NO_CHAIN_PARTICLES.get())
                HyperConnection.INSTANCE.send(PacketDistributor.DIMENSION.with(level::dimension), new ClientboundPerfectKnockedoutParticles(blocks));
        }
    }

    public static void perfectKnockoutAt(ServerPlayer player, ServerLevel level, BlockPos cursor) {
        BlockState currentBlock = level.getBlockState(cursor);
        FluidState currentFluid = level.getFluidState(cursor);
        BlockEntity currentTile = level.getBlockEntity(cursor);
        BlockState legacyBlock = currentBlock.getBlock() instanceof LiquidBlock ? Blocks.AIR.defaultBlockState() : currentFluid.createLegacyBlock();

        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, cursor, currentBlock, player);
        MinecraftForge.EVENT_BUS.post(event);

        boolean creative = player.isCreative();

        if (!creative) player.getMainHandItem().mineBlock(level, currentBlock, cursor, player);
        currentBlock.onDestroyedByPlayer(level, cursor, player, !creative, currentFluid);
        perfectKnockoutAbsolute(level, cursor, currentBlock, legacyBlock);
        currentBlock.getBlock().destroy(level, cursor, currentBlock);

        HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(cursor)), new ClientboundPerfectKnockedoutParticles(new Long2IntOpenHashMap(new long[]{cursor.asLong()}, new int[]{Block.getId(currentBlock)})));

        if (!creative) {
            float exhaustion = player.getFoodData().getExhaustionLevel();
            currentBlock.getBlock().playerDestroy(level, player, cursor, currentBlock, currentTile, player.getMainHandItem());
            player.getFoodData().setExhaustion(exhaustion);

            ResourceLocation loot = currentBlock.getBlock().getLootTable();
            if (loot == BuiltInLootTables.EMPTY || (level.getServer().getLootData().getLootTable(loot) instanceof ILootTableParadox paradox && paradox.hyperdaimc$hasNoDrop())) {
                ItemStack stack = new ItemStack(currentBlock.getBlock());
                if (!stack.isEmpty()) Block.popResource(level, cursor, stack);
            }

            int experience = event.getExpToDrop();
            if (experience > 0) currentBlock.getBlock().popExperience(level, cursor, experience);
        }
        player.connection.send(new ClientboundBlockUpdatePacket(cursor, legacyBlock));
        level.playSound(null, cursor, HyperSounds.PERFECT_KNOCKOUT.get(), SoundSource.BLOCKS, 0.75f, 1);
    }

    private static void perfectKnockoutAbsolute(ServerLevel level, BlockPos pos, BlockState old, BlockState legacy) {
        int sectionX = pos.getX() & 15;
        int sectionY = pos.getY() & 15;
        int sectionZ = pos.getZ() & 15;
        LevelChunk chunk = level.getChunkAt(pos);
        chunk.getSection(chunk.getSectionIndex(pos.getY())).setBlockState(sectionX, sectionY, sectionZ, legacy);
        level.markAndNotifyBlock(pos, chunk, old, level.getBlockState(pos), Block.UPDATE_ALL, 512);
        level.getLightEngine().checkBlock(pos);
        chunk.setUnsaved(true);
    }
}
