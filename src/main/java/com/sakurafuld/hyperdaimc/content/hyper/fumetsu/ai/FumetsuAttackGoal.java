package com.sakurafuld.hyperdaimc.content.hyper.fumetsu.ai;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.skull.FumetsuSkull;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.squall.FumetsuSquall;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IServerLevelFumetsu;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class FumetsuAttackGoal extends Goal {
    private final FumetsuEntity fumetsu;
    private final float range;
    private final List<WeightedEntry.Wrapper<Runnable>> attacks = Lists.newArrayList();
    @Nullable
    private LivingEntity target = null;
    private int attackTime = -1;
    private int annihilate = 12;

    public FumetsuAttackGoal(FumetsuEntity fumetsu, float range) {
        this.fumetsu = fumetsu;
        this.range = range;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));

        this.attacks.add(WeightedEntry.wrap(() -> this.fumetsu.shoot(FumetsuSkull.Type.CRIMSON, 1), 30));
        this.attacks.add(WeightedEntry.wrap(() -> this.fumetsu.shoot(FumetsuSkull.Type.CYAN, 2), 30));
        this.attacks.add(WeightedEntry.wrap(() -> this.fumetsu.shoot(FumetsuSkull.Type.CRYSTAL, 0), 15));
        this.attacks.add(WeightedEntry.wrap(() -> {
            this.fumetsu.shoot(FumetsuSkull.Type.CRIMSON, 0);
            this.fumetsu.shoot(FumetsuSkull.Type.CRIMSON, 1);
        }, 20));
        this.attacks.add(WeightedEntry.wrap(() -> {
            this.fumetsu.shoot(FumetsuSkull.Type.CYAN, 0);
            this.fumetsu.shoot(FumetsuSkull.Type.CYAN, 2);
        }, 20));
        this.attacks.add(WeightedEntry.wrap(() -> {
            this.fumetsu.shoot(FumetsuSkull.Type.CRYSTAL, 0);
            this.fumetsu.shoot(FumetsuSkull.Type.CRIMSON, 1);
            this.fumetsu.shoot(FumetsuSkull.Type.CYAN, 2);
        }, 10));
        this.attacks.add(WeightedEntry.wrap(() -> {
            if (this.fumetsu.level() instanceof ServerLevel serverLevel) {
                Vec3 start = this.fumetsu.getEyePosition();

                FumetsuSquall squall = new FumetsuSquall(HyperEntities.FUMETSU_SQUALL.get(), this.fumetsu.level());
                squall.setup(this.fumetsu, start, this.fumetsu.getViewVector(1), 0.75f);

                ((IServerLevelFumetsu) serverLevel).hyperdaimc$fumetsuSpawn(squall);
                serverLevel.playSound(null, start.x(), start.y(), start.z(), HyperSounds.FUMETSU_SHOOT.get(), SoundSource.HOSTILE, 2, 1 + (this.fumetsu.getRandom().nextFloat() - this.fumetsu.getRandom().nextFloat()) * 0.2f);
            }
        }, 10));
        this.attacks.add(WeightedEntry.wrap(this::annihilate, 5));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.fumetsu.getTarget();
        if (target != null && !target.isRemoved()) {
            this.target = target;
            return true;
        } else return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || !this.fumetsu.getNavigation().isDone();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        this.target = null;
        this.attackTime = -1;
    }

    @Override
    public void tick() {
        if (this.target != null) {
            Vec3 movement = this.fumetsu.getDeltaMovement().multiply(1, 0.6, 1);

            Vec3 point = this.target.position();
            point = point.add(this.fumetsu.position().subtract(point).normalize().scale(7));
            point = new Vec3(point.x(), this.target.getY() + 3, point.z());

            Vec3 horizontalMove = new Vec3(point.x() - this.fumetsu.getX(), point.y() - this.fumetsu.getY(), point.z() - this.fumetsu.getZ());
            Vec3 normalized = horizontalMove.normalize();
            movement = movement.add(normalized.x() * 0.5, normalized.y() * 0.5, normalized.z() * 0.5);

            double length = point.subtract(this.fumetsu.position()).length();
            if (length <= 0.2)
                movement = movement.scale(0.01);

            this.fumetsu.setDeltaMovement(movement);

            float interval = Mth.lerp(this.fumetsu.getHealth() / this.fumetsu.getMaxHealth(), 5, 10);
            if (--this.attackTime == 0) {

                double distance = this.fumetsu.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
                this.attackTime = (int) Math.round(Math.sqrt(distance) / this.range * interval);

                if (--this.annihilate <= 0) {
                    this.annihilate = 12;
                    this.annihilate();
                } else
                    WeightedRandom.getRandomItem(this.fumetsu.getRandom(), this.attacks).ifPresent(attack -> attack.getData().run());
            } else if (this.attackTime < 0)
                this.attackTime = Math.round(interval);
        }
    }

    private void annihilate() {
        for (int count = 0; count < 12; count++) {
            double x = this.fumetsu.getX() + Math.cos(Math.toRadians(this.fumetsu.getRandom().nextInt(360)));
            double y = this.fumetsu.getEyeY() + Math.sin(Math.toRadians(this.fumetsu.getRandom().nextInt(360)));
            double z = this.fumetsu.getZ() + Math.sin(Math.toRadians(this.fumetsu.getRandom().nextInt(360)));

            this.fumetsu.shoot(this.fumetsu.getRandom().nextBoolean() ? FumetsuSkull.Type.CRIMSON : FumetsuSkull.Type.CYAN, 0, new Vec3(x, y, z), 1.5f);
        }
    }
}
