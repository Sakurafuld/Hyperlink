package com.sakurafuld.hyperdaimc.mixin.chronicle;

import com.sakurafuld.hyperdaimc.content.hyper.chronicle.system.ChronicleHandler;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.system.ParadoxHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.extensions.IForgeBlockState;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
public abstract class BlockStateMixin implements IForgeBlockState {
    @Override
    public boolean onDestroyedByPlayer(Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (ChronicleHandler.isPaused(level, pos, player))
            return false;
        BlockState self = (BlockState) (Object) this;
        return self.getBlock().onDestroyedByPlayer(self, level, pos, player, willHarvest, fluid);
    }

    @Override
    public int getExpDrop(LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
        if (level instanceof Level world && ChronicleHandler.isPaused(world, pos, ParadoxHandler.gashaconPlayer))
            return 0;
        BlockState self = (BlockState) (Object) this;
        return self.getBlock().getExpDrop(self, level, randomSource, pos, fortuneLevel, silkTouchLevel);

    }

//    @Override
//    public float getExplosionResistance(BlockGetter level, BlockPos pos, Explosion explosion) {
//        if (level instanceof Level world && ChronicleHandler.isPaused(world, pos, null))
//            return 2000000000f;
//        BlockState self = (BlockState) (Object) this;
//        return self.getBlock().getExplosionResistance(self, level, pos, explosion);
//    }

    @Override
    public boolean canDropFromExplosion(BlockGetter level, BlockPos pos, Explosion explosion) {
        if (level instanceof Level world && ChronicleHandler.isPaused(world, pos, null))
            return false;
        BlockState self = (BlockState) (Object) this;
        return self.getBlock().canDropFromExplosion(self, level, pos, explosion);
    }

    @Override
    public void onBlockExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!ChronicleHandler.isPaused(level, pos, null)) {
            BlockState self = (BlockState) (Object) this;
            self.getBlock().onBlockExploded(self, level, pos, explosion);
        }
    }

    @Override
    public boolean isFlammable(BlockGetter level, BlockPos pos, Direction face) {
        BlockState self = (BlockState) (Object) this;
        if (level instanceof Level world && ChronicleHandler.isPaused(world, pos, null)) {
//            if (world.isClientSide() && (self.ignitedByLava() || self.getBlock().getFlammability(self, level, pos, face) > 0))
//                ChronicleHandler.hits.add(Pair.of(pos, Util.getMillis()));
            return false;
        }
        return self.getBlock().isFlammable(self, level, pos, face);
    }

    @Override
    public int getFlammability(BlockGetter level, BlockPos pos, Direction face) {
        if (level instanceof Level world && ChronicleHandler.isPaused(world, pos, null))
            return 0;
        BlockState self = (BlockState) (Object) this;
        return self.getBlock().getFlammability(self, level, pos, face);
    }

    @Override
    public void onCaughtFire(Level level, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
        if (!ChronicleHandler.isPaused(level, pos, igniter)) {
            BlockState self = (BlockState) (Object) this;
            self.getBlock().onCaughtFire(self, level, pos, face, igniter);
        }
    }

    @Override
    public int getFireSpreadSpeed(BlockGetter level, BlockPos pos, Direction face) {
        if (level instanceof Level world && ChronicleHandler.isPaused(world, pos, null))
            return 0;
        BlockState self = (BlockState) (Object) this;
        return self.getBlock().getFireSpreadSpeed(self, level, pos, face);
    }

    @Override
    public boolean canEntityDestroy(BlockGetter level, BlockPos pos, Entity entity) {
        if (level instanceof Level world && ChronicleHandler.isPaused(world, pos, entity))
            return false;
        BlockState self = (BlockState) (Object) this;
        return self.getBlock().canEntityDestroy(self, level, pos, entity);
    }

    @Nullable
    @Override
    public BlockState getToolModifiedState(UseOnContext context, ToolAction toolAction, boolean simulate) {
        BlockState self = (BlockState) (Object) this;
        if (ChronicleHandler.isPaused(context.getLevel(), context.getClickedPos(), context.getPlayer()))
            return null;
        BlockState eventState = ForgeEventFactory.onToolUse(self, context, toolAction, simulate);
        return eventState != self ? eventState : self.getBlock().getToolModifiedState(self, context, toolAction, simulate);

    }

    @Override
    public boolean canHarvestBlock(BlockGetter level, BlockPos pos, Player player) {
        if (level instanceof Level world && ChronicleHandler.isPaused(world, pos, player))
            return false;
        BlockState self = (BlockState) (Object) this;
        return self.getBlock().canHarvestBlock(self, level, pos, player);
    }
}
