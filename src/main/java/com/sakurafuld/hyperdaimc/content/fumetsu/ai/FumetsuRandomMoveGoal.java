package com.sakurafuld.hyperdaimc.content.fumetsu.ai;

import com.sakurafuld.hyperdaimc.content.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.helper.Boxes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class FumetsuRandomMoveGoal extends Goal {
    private final FumetsuEntity fumetsu;

    public FumetsuRandomMoveGoal(FumetsuEntity fumetsu) {
        this.fumetsu = fumetsu;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.fumetsu.getRandom().nextInt(reducedTickDelay(64)) == 0) {
            return true;
        }
        return (!this.fumetsu.getMoveControl().hasWanted() && this.fumetsu.getRandom().nextInt(reducedTickDelay(7)) == 0);
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void tick() {
        BlockPos origin = this.fumetsu.getOrigin();
        if (Boxes.INVALID.equals(origin)) {
            origin = this.fumetsu.blockPosition();
        }


        BlockPos offset = origin.offset(this.fumetsu.getRandom().nextInt(21) - 10, this.fumetsu.getRandom().nextInt(7) - 3, this.fumetsu.getRandom().nextInt(21) - 10);

        double speed = Mth.lerp(Math.min(1, Math.sqrt(this.fumetsu.blockPosition().distSqr(offset)) / 48), 0.25, 2);
//        LOG.debug("randomMove:{}", speed);
        this.fumetsu.getMoveControl().setWantedPosition(offset.getX() + 0.5, offset.getY() + 0.5, offset.getZ() + 0.5, speed);
        if (this.fumetsu.getTarget() == null) {
            this.fumetsu.getLookControl().setLookAt(offset.getX() + this.fumetsu.getRandom().nextDouble(), offset.getY() + this.fumetsu.getRandom().nextDouble(), offset.getZ() + this.fumetsu.getRandom().nextDouble(), 20, 10);
        }
    }
}
