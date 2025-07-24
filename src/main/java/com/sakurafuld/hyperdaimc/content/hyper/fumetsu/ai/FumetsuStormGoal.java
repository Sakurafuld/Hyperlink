package com.sakurafuld.hyperdaimc.content.hyper.fumetsu.ai;

import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.skull.FumetsuSkull;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.storm.FumetsuStormSkull;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class FumetsuStormGoal extends Goal {
    private final FumetsuEntity fumetsu;
    @Nullable
    private LivingEntity target = null;
    private long lastUse;
    private int tickCount = 0;
    private boolean end = false;

    public FumetsuStormGoal(FumetsuEntity fumetsu) {
        this.fumetsu = fumetsu;
        this.lastUse = fumetsu.level().getGameTime();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.target = this.fumetsu.getTarget();
        return this.target != null && !this.target.isRemoved() && this.fumetsu.level().getGameTime() - this.lastUse > 100;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.end || !this.fumetsu.getNavigation().isDone();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        this.tickCount = 0;
        this.fumetsu.setStorm(true);
    }

    @Override
    public void stop() {
        this.tickCount = 0;
        this.target = null;
        this.end = false;
        this.lastUse = this.fumetsu.level().getGameTime();
        this.fumetsu.setStorm(false);
    }

    @Override
    public void tick() {
        if (this.target != null && ++this.tickCount <= 45) {

            Vec3 movement = this.fumetsu.getDeltaMovement().multiply(1, 0.6, 1);

            Vec3 point = this.target.position();
            point = point.add(this.fumetsu.position().subtract(point).normalize().scale(12));
            point = new Vec3(point.x(), this.target.getY(), point.z());

            Vec3 horizontalMove = new Vec3(point.x() - this.fumetsu.getX(), point.y() - this.fumetsu.getY(), point.z() - this.fumetsu.getZ());
            Vec3 normalized = horizontalMove.normalize();
            movement = movement.add(normalized.x() * 0.5, normalized.y() * 0.5, normalized.z() * 0.5);

            double length = point.subtract(this.fumetsu.position()).length();
            if (length <= 0.5) {
                movement = movement.scale(length * 0.1);
            }

            this.fumetsu.setDeltaMovement(movement);

            Vec3 centerHead = new Vec3(this.fumetsu.getHeadX(0), this.fumetsu.getHeadY(0), this.fumetsu.getHeadZ(0));

            if (this.tickCount == 35) {

                FumetsuStormSkull skull = new FumetsuStormSkull(HyperEntities.FUMETSU_STORM_SKULL.get(), this.fumetsu.level());
                skull.setup(FumetsuSkull.Type.CRYSTAL, this.fumetsu, centerHead, Vec3.ZERO, 1);
                skull.moveTo(skull.getX(), skull.getY(), skull.getZ(), this.fumetsu.getYHeadRot(), -50);
                skull.setDeltaMovement(skull.getPoweredRotVec());

                this.fumetsu.level().addFreshEntity(skull);
                if (this.fumetsu.level() instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, centerHead.x(), centerHead.y(), centerHead.z(), HyperSounds.FUMETSU_SHOOT.get(), SoundSource.HOSTILE, 2, 1 + (this.fumetsu.getRandom().nextFloat() - this.fumetsu.getRandom().nextFloat()) * 0.2f);
                }
                this.shoot(centerHead, 1);
                this.shoot(centerHead, 2);
            }
        } else {
            this.end = true;
        }
    }

    private void shoot(Vec3 centerHead, int head) {
        Vec3 sideHead = new Vec3(this.fumetsu.getHeadX(head), this.fumetsu.getHeadY(head), this.fumetsu.getHeadZ(head));
        Vec3 vec = sideHead.subtract(centerHead).add(0, -1, 0);
        FumetsuStormSkull skull = new FumetsuStormSkull(HyperEntities.FUMETSU_STORM_SKULL.get(), this.fumetsu.level());
        skull.setup(head == 1 ? FumetsuSkull.Type.CRIMSON : FumetsuSkull.Type.CYAN, this.fumetsu, sideHead, vec, 1);

        this.fumetsu.level().addFreshEntity(skull);
        if (this.fumetsu.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, centerHead.x(), centerHead.y(), centerHead.z(), HyperSounds.FUMETSU_SHOOT.get(), SoundSource.HOSTILE, 2, 1 + (this.fumetsu.getRandom().nextFloat() - this.fumetsu.getRandom().nextFloat()) * 0.2f);
        }
    }

}
