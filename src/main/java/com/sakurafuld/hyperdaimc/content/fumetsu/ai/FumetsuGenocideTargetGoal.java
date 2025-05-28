package com.sakurafuld.hyperdaimc.content.fumetsu.ai;

import com.sakurafuld.hyperdaimc.HyperServerConfig;
import com.sakurafuld.hyperdaimc.content.fumetsu.FumetsuEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class FumetsuGenocideTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public FumetsuGenocideTargetGoal(FumetsuEntity fumetsu, Class<T> type) {
        super(fumetsu, type, 0, false, false, entity -> !(entity instanceof FumetsuEntity) && fumetsu.isAvailableTarget(entity));
        this.targetConditions = TargetingConditions.forCombat()
                .ignoreInvisibilityTesting()
                .range(this.getFollowDistance())
                .selector(entity -> !(entity instanceof FumetsuEntity) && fumetsu.isAvailableTarget(entity));
    }

    @Override
    protected @NotNull AABB getTargetSearchArea(double pTargetDistance) {
        return this.mob.getBoundingBox().inflate(pTargetDistance, pTargetDistance, pTargetDistance);
    }

    @Override
    protected double getFollowDistance() {
        return HyperServerConfig.FUMETSU_RANGE.get();
    }
}
