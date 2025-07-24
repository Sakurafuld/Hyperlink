package com.sakurafuld.hyperdaimc.content.hyper.fumetsu.storm;

import com.sakurafuld.hyperdaimc.api.content.GashatParticleOptions;
import com.sakurafuld.hyperdaimc.api.mixin.IEntityNovel;
import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.skull.FumetsuSkull;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class FumetsuStormSkull extends FumetsuSkull {

    public FumetsuStormSkull(EntityType<? extends FumetsuStormSkull> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void setup(Type type, FumetsuEntity owner, Vec3 start, Vec3 vector, float power) {
        this.setMovable(true);
        this.setSkullType(type);
        this.setOwner(owner);
        this.setPower(power);
        this.setTarget(owner.getTarget());
        float xRot = (float) Math.toDegrees(-Mth.atan2(vector.y(), vector.horizontalDistance()));
        float yRot = (float) Math.toDegrees(-Mth.atan2(vector.x(), vector.z()));
        this.moveTo(start.x(), start.y(), start.z(), yRot, xRot);
        this.setDeltaMovement(this.getPoweredRotVec());
        this.setMovable(false);
    }

    @Override
    public void fumetsuTick() {
        if (this.tickCount > this.getAge()) {
            this.transform();
        } else {
            this.setPower((float) Math.pow(0.75 + ((float) this.tickCount / 30f), 2));
            super.fumetsuTick();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        this.transform();
    }

    private void transform() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), HyperSounds.FUMETSU_STORM.get(), SoundSource.HOSTILE, 24, 1);
        }
        FumetsuStorm storm = new FumetsuStorm(HyperEntities.FUMETSU_STORM.get(), this.level());
        storm.setup(this.getOwner(), this.position());
        this.level().addFreshEntity(storm);
        ((IEntityNovel) this).novelRemove(RemovalReason.DISCARDED);
    }

    @Override
    protected int getAge() {
        return 30;
    }

    @Override
    protected ParticleOptions getParticle() {
        return new GashatParticleOptions(new Vector3f(this.random.nextFloat(), this.random.nextFloat(), this.random.nextFloat()), 0.5f, 0.1f, this.random.nextBoolean() ? 4 : -4);
    }

    @Override
    protected float getHomingDelta(Vec3 homing) {
        return 0.125f + Math.min(0.875f, (float) Math.pow(this.tickCount / 20d, 1.5));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
    }
}
