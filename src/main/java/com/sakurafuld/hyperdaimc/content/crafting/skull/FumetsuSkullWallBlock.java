package com.sakurafuld.hyperdaimc.content.crafting.skull;

import com.sakurafuld.hyperdaimc.content.HyperBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FumetsuSkullWallBlock extends WallSkullBlock {
    public FumetsuSkullWallBlock(SkullBlock.Type type, Properties pProperties) {
        super(type, pProperties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return HyperBlockEntities.FUMETSU_SKULL.get().create(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return null;
    }
}
