package com.sakurafuld.hyperdaimc.content.fumetsu.squall;

import com.mojang.math.Vector3f;
import com.sakurafuld.hyperdaimc.api.content.GashatParticleOptions;
import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.fumetsu.skull.FumetsuSkull;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FumetsuSquall extends FumetsuSkull {
    public FumetsuSquall(EntityType<? extends FumetsuSquall> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public void setup(FumetsuEntity owner, Vec3 start, Vec3 vector, float power) {
        this.setMovable(true);
        this.setSkullType(Type.CRYSTAL);
        this.setOwner(owner);
        this.setPower(power);

        float xRot = (float) Math.toDegrees(-Mth.atan2(vector.y(), vector.horizontalDistance()));
        float yRot = (float) Math.toDegrees(-Mth.atan2(vector.x(), vector.z()));

        this.moveTo(start.x(), start.y(), start.z(), yRot, xRot);

        this.setDeltaMovement(this.getPoweredRotVec());
        this.setMovable(false);
    }

    @Override
    protected void skullTick() {
        super.skullTick();

        if (this.tickCount % 5 == 0) {
            Vec3 center = this.getBoundingBox().getCenter();

            for (int count = 0; count < 4; count++) {
                double x = center.x() + Math.cos(Math.toRadians(this.random.nextInt(360)));
                double y = center.y() + Math.sin(Math.toRadians(this.random.nextInt(360)));
                double z = center.z() + Math.sin(Math.toRadians(this.random.nextInt(360)));

                Vec3 vector = new Vec3(x, y, z);
                FumetsuSkull skull = new FumetsuSkull(HyperEntities.FUMETSU_SKULL.get(), this.getLevel());
                skull.setup(this.random.nextBoolean() ? Type.CRIMSON : Type.CYAN, this.getOwner(), center, vector.subtract(center), 1.75f);

                this.getLevel().addFreshEntity(skull);
            }


            if (this.getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, center.x(), center.y(), center.z(), HyperSounds.FUMETSU_SHOOT.get(), SoundSource.HOSTILE, 0.25f, 3 + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
            }
        }
    }

    @Override
    protected int getAge() {
        return 80;
    }

    @Override
    protected float getHomingDelta(Vec3 homing) {
        return 0.1f;
    }

    @Override
    protected ParticleOptions getParticle() {
        Vector3f color = switch (this.random.nextInt(3)) {
            case 0 -> new Vector3f(0, 1, 1);
            case 1 -> new Vector3f(1, 0, 0);
            default -> new Vector3f(1, 1, 1);
        };
        return new GashatParticleOptions(color, 2, 0.4f, this.random.nextBoolean() ? 16 : -16);
    }

    @Override
    protected @Nullable EntityHitResult rayBoxTrace(Vec3 delta, AABB area) {
        return null;
    }
}
