package com.sakurafuld.hyperdaimc.content.fumetsu.ai;

import com.sakurafuld.hyperdaimc.content.fumetsu.FumetsuEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class FumetsuNoTargetGoal extends Goal {
    private final FumetsuEntity fumetsu;
    public FumetsuNoTargetGoal(FumetsuEntity fumetsu) {
        this.fumetsu = fumetsu;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return !this.fumetsu.isGenocide();
    }
}
