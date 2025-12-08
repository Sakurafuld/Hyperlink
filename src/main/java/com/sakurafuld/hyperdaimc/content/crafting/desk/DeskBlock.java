package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.sakurafuld.hyperdaimc.content.HyperBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class DeskBlock extends Block implements EntityBlock {

    public DeskBlock(Properties pProperties) {
        super(pProperties);
    }

    public static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty()) {
            ItemEntity entity = player.drop(stack, false);
            if (entity != null) {
                entity.setNoPickUpDelay();
                entity.setThrower(player.getUUID());
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return HyperBlockEntities.DESK.get().create(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            pLevel.getBlockEntity(pPos, HyperBlockEntities.DESK.get()).ifPresent(desk -> NetworkHooks.openScreen(serverPlayer, desk, pPos));
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        pLevel.getBlockEntity(pPos, HyperBlockEntities.DESK.get()).ifPresent(DeskBlockEntity::dropOrMinecraft);
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        pLevel.getBlockEntity(pPos, HyperBlockEntities.DESK.get()).ifPresent(desk -> {
            if (desk.isMinecraft()) {
                desk.minecrafter = pPlayer;
            }
        });
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        if (pBlockEntity instanceof DeskBlockEntity desk && desk.minecrafter == pPlayer) {
            pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
            pPlayer.causeFoodExhaustion(0.005f);
            give(pPlayer, new ItemStack(this));
            desk.minecrafter = null;
        } else {
            super.playerDestroy(pLevel, pPlayer, pPos, pState, pBlockEntity, pTool);
        }
    }
}
