package com.sakurafuld.hyperdaimc.content.fumetsu.storm;

import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.content.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.fumetsu.skull.FumetsuSkull;
import com.sakurafuld.hyperdaimc.content.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.helper.Calculates;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;
import static com.sakurafuld.hyperdaimc.helper.Deets.require;

public class FumetsuStorm extends Entity implements IFumetsu {
    private static final EntityDataAccessor<Integer> DATA_OWNER = SynchedEntityData.defineId(FumetsuStorm.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET = SynchedEntityData.defineId(FumetsuStorm.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_INFLATION = SynchedEntityData.defineId(FumetsuStorm.class, EntityDataSerializers.FLOAT);


    private final double SPEED = 0.875f;

    public AABB oldAABB = this.getBoundingBox();

    public FumetsuStorm(EntityType<FumetsuStorm> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    public void setup(FumetsuEntity fumetsu, Vec3 position) {
        this.setOwner(fumetsu);
        LivingEntity target = fumetsu.getTarget();
        this.setTarget(target);
        this.moveTo(position);

        if(target != null) {
            this.setDeltaMovement(target.getBoundingBox().getCenter().subtract(this.getBoundingBox().getCenter()).normalize().scale(SPEED));
        }

    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_OWNER, 0);
        this.getEntityData().define(DATA_TARGET, 0);
        this.getEntityData().define(DATA_INFLATION, 0f);
    }

    @Override
    public void fumetsuTick() {
        FumetsuEntity fumetsu = this.getOwner();
        if(this.tickCount > 40 || fumetsu == null || fumetsu.isRemoved()) {
            this.discard();
        } else {
            this.baseTick();

            this.oldAABB = this.getBoundingBox();

            this.setInflation(0.1f * (float) Calculates.curve( this.tickCount / 40d, 0, 100, 0));

            Entity target = this.getTarget();
            if(target != null) {
                this.setDeltaMovement(target.getBoundingBox().getCenter().subtract(this.getBoundingBox().getCenter()).normalize().scale(SPEED));
            } else if (fumetsu.getTarget() != null) {
                this.setTarget(fumetsu.getTarget());
            }

            Vec3 movement = this.getDeltaMovement();
            Random random = new Random(this.getId());

            double moveX = this.getX() + movement.x() + Mth.lerp(random.nextDouble(), -0.1, 0.1);
            double moveY = this.getY() + movement.y() + Mth.lerp(random.nextDouble(), -0.1, 0.1);
            double moveZ = this.getZ() + movement.z() + Mth.lerp(random.nextDouble(), -0.1, 0.1);
            this.setDeltaMovement(movement);
            this.setPos(moveX, moveY, moveZ);

            if((this.tickCount + this.getId()) % 3 == 0) {
                List<Entity> list = this.getLevel().getEntities(this, this.getBoundingBox(), entity -> entity.getType() != this.getType() && !entity.equals(fumetsu) && fumetsu.isAvailableTarget(entity));
                if(!list.isEmpty() && this.getLevel() instanceof ServerLevel serverLevel) {
                    NovelHandler.playSound(serverLevel, this.getBoundingBox().getCenter());
                }

                list.forEach(entity -> {
                    NovelHandler.novelize(fumetsu, entity, false);
                    if(entity instanceof LivingEntity living) {
                        living.removeAllEffects();
                        for (int count = 0; count < 5 && !NovelHandler.novelized(living); count++) {
                            NovelHandler.novelize(fumetsu, living, false);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void tick() {}
    @Override
    protected AABB makeBoundingBox() {
        return super.makeBoundingBox().inflate(this.getInflation());
    }
    @Nullable
    public FumetsuEntity getOwner() {
        int owner = this.getEntityData().get(DATA_OWNER);
        if(owner > 0 && this.getLevel().getEntity(owner) instanceof FumetsuEntity fumetsu) {
            return fumetsu;
        } else {
            return null;
        }
    }
    public void setOwner(@Nullable FumetsuEntity fumetsu) {
        this.getEntityData().set(DATA_OWNER, fumetsu == null ? 0 : fumetsu.getId());
    }
    @Nullable
    public Entity getTarget() {
        int target = this.getEntityData().get(DATA_TARGET);
        if(target > 0) {
            return this.getLevel().getEntity(target);
        } else {
            return null;
        }
    }
    public void setTarget(@Nullable Entity entity) {
        this.getEntityData().set(DATA_TARGET, entity == null ? 0 : entity.getId());
    }
    public float getInflation() {
        return this.getEntityData().get(DATA_INFLATION);
    }
    public void setInflation(float inflation) {
        this.getEntityData().set(DATA_INFLATION, inflation);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {return false;}
    @Override
    public void kill() {}
    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        this.getEntityData().set(DATA_OWNER, pCompound.getInt("Owner"));
        this.getEntityData().set(DATA_TARGET, pCompound.getInt("Target"));
        this.getEntityData().set(DATA_INFLATION, pCompound.getFloat("Inflation"));
    }
    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putInt("Owner", this.getEntityData().get(DATA_OWNER));
        pCompound.putInt("Target", this.getEntityData().get(DATA_TARGET));
        pCompound.putFloat("Inflation", this.getInflation());
    }
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
