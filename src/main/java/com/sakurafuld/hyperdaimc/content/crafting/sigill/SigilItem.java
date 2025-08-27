package com.sakurafuld.hyperdaimc.content.crafting.sigill;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SigilItem extends Item {
    private static BlockPattern pattern0 = null;
    private static BlockPattern pattern1 = null;

    public SigilItem(Properties pProperties) {
        super(pProperties.rarity(Rarity.UNCOMMON).stacksTo(1).fireResistant());
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return itemStack.copy();
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("tooltip.hyperdaimc.god_sigil").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if (HyperCommonConfig.FUMETSU_SUMMON.get() && pContext.getPlayer() != null && pContext.getPlayer().isShiftKeyDown()) {

            if (checkAndSpawn(pContext.getLevel(), pContext.getClickedPos(), getOrCreatePattern0()) || checkAndSpawn(pContext.getLevel(), pContext.getClickedPos(), getOrCreatePattern1()))
                ;

            return InteractionResult.sidedSuccess(pContext.getLevel().isClientSide());
        }
        return super.useOn(pContext);
    }

    public static boolean checkAndSpawn(Level level, BlockPos pos, BlockPattern pattern) {
        BlockPattern.BlockPatternMatch match = pattern.find(level, pos);
        if (match != null) {
            if (!level.isClientSide()) {
                for (int w = 0; w < pattern.getWidth(); ++w) {
                    for (int h = 0; h < pattern.getHeight(); ++h) {
                        BlockInWorld block = match.getBlock(w, h, 0);
                        level.setBlock(block.getPos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, block.getPos(), Block.getId(block.getState()));
                    }
                }

                FumetsuHandler.spawn.set(true);

                FumetsuEntity fumetsu = HyperEntities.FUMETSU.get().create(level);
                BlockPos center = match.getBlock(1, 2, 0).getPos();
                fumetsu.moveTo(center.getX() + 0.5, center.getY() + 0.25, center.getZ() + 0.5, match.getForwards().getAxis() == Direction.Axis.X ? 0 : 90, 0);
                fumetsu.yBodyRot = match.getForwards().getAxis() == Direction.Axis.X ? 0 : 90;

                FumetsuHandler.spawn.set(false);

                for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, fumetsu.getBoundingBox().inflate(50))) {
                    CriteriaTriggers.SUMMONED_ENTITY.trigger(player, fumetsu);
                }

                level.addFreshEntity(fumetsu);

                for (int w = 0; w < pattern.getWidth(); ++w) {
                    for (int h = 0; h < pattern.getHeight(); ++h) {
                        level.blockUpdated(match.getBlock(w, h, 0).getPos(), Blocks.AIR);
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private static BlockPattern getOrCreatePattern0() {
        if (pattern0 == null) {
            pattern0 = BlockPatternBuilder.start()
                    .aisle("&^%", "###", "~#~")
                    .where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(HyperBlocks.FUMETSU_SKULL.get()).or(BlockStatePredicate.forBlock(HyperBlocks.FUMETSU_WALL_SKULL.get()))))
                    .where('&', BlockInWorld.hasState(BlockStatePredicate.forBlock(HyperBlocks.FUMETSU_RIGHT.get()).or(BlockStatePredicate.forBlock(HyperBlocks.FUMETSU_WALL_RIGHT.get()))))
                    .where('%', BlockInWorld.hasState(BlockStatePredicate.forBlock(HyperBlocks.FUMETSU_LEFT.get()).or(BlockStatePredicate.forBlock(HyperBlocks.FUMETSU_WALL_LEFT.get()))))
                    .where('#', block -> block.getState().is(HyperBlocks.SOUL.get()))
                    .where('~', state -> state.getState().isAir())
                    .build();
        }

        return pattern0;
    }

    private static BlockPattern getOrCreatePattern1() {
        if (pattern1 == null) {
            pattern1 = BlockPatternBuilder.start()
                    .aisle("%^&", "###", "~#~")
                    .where('^', BlockInWorld.hasState(BlockStatePredicate.forBlock(HyperBlocks.FUMETSU_SKULL.get()).or(BlockStatePredicate.forBlock(HyperBlocks.FUMETSU_WALL_SKULL.get()))))
                    .where('&', BlockInWorld.hasState(BlockStatePredicate.forBlock(HyperBlocks.FUMETSU_RIGHT.get()).or(BlockStatePredicate.forBlock(HyperBlocks.FUMETSU_WALL_RIGHT.get()))))
                    .where('%', BlockInWorld.hasState(BlockStatePredicate.forBlock(HyperBlocks.FUMETSU_LEFT.get()).or(BlockStatePredicate.forBlock(HyperBlocks.FUMETSU_WALL_LEFT.get()))))
                    .where('#', block -> block.getState().is(HyperBlocks.SOUL.get()))
                    .where('~', state -> state.getState().isAir())
                    .build();
        }

        return pattern1;
    }
}
