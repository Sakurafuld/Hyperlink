package com.sakurafuld.hyperdaimc.content.crafting.skull;

import com.sakurafuld.hyperdaimc.content.HyperBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FumetsuSkullBlockEntity extends BlockEntity {
    public FumetsuSkullBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(HyperBlockEntities.FUMETSU_SKULL.get(), pPos, pBlockState);
    }

}
