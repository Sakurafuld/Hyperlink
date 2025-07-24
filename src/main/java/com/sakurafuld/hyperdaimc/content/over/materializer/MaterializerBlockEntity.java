package com.sakurafuld.hyperdaimc.content.over.materializer;

import com.sakurafuld.hyperdaimc.content.HyperBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MaterializerBlockEntity extends BlockEntity {

    public MaterializerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(HyperBlockEntities.MATERIALIZER.get(), pPos, pBlockState);
    }


    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
    }
}
