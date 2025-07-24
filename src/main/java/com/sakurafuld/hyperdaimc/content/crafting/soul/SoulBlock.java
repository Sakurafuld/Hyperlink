package com.sakurafuld.hyperdaimc.content.crafting.soul;

import com.sakurafuld.hyperdaimc.content.HyperSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SoulBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(1, 1, 1, 15, 15, 15);

    public SoulBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        Vec3 vec = pEntity.getBoundingBox().getCenter().subtract(Vec3.atCenterOf(pPos))
                .normalize()
                .add(pEntity.getViewVector(1).multiply(0.125, 0, 0.125))
                .normalize()
                .add(0, 0.125, 0)
                .scale(pLevel.getRandom().nextDouble(0.4, 1));

        pEntity.setDeltaMovement(pEntity.getDeltaMovement().add(vec));
        pLevel.playSound(null, pPos, HyperSounds.SOUL.get(), SoundSource.BLOCKS, 1, 0.8f + pLevel.getRandom().nextFloat() * 0.3f);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }
}
