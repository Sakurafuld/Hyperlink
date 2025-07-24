package com.sakurafuld.hyperdaimc.content.crafting.skull;

import com.sakurafuld.hyperdaimc.content.HyperBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FumetsuSkullBlock extends SkullBlock {
    public static final Type CENTER = new Type() {
    };
    public static final Type RIGHT = new Type() {
    };
    public static final Type LEFT = new Type() {
    };

    public FumetsuSkullBlock(Type type, Properties pProperties) {
        super(type, pProperties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return HyperBlockEntities.FUMETSU_SKULL.get().create(pPos, pState);
    }
}
