package com.sakurafuld.hyperdaimc.content.fumetsu.skull;

import com.mojang.math.Vector3f;
import com.sakurafuld.hyperdaimc.api.content.GashatParticleOptions;
import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.content.fumetsu.FumetsuEntity;
import com.sakurafuld.hyperdaimc.content.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.helper.Calculates;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sakurafuld.hyperdaimc.helper.Deets.*;

public class FumetsuSkull extends Entity implements IFumetsu {
    private static final EntityDataAccessor<String> DATA_TYPE = SynchedEntityData.defineId(FumetsuSkull.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_OWNER = SynchedEntityData.defineId(FumetsuSkull.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET = SynchedEntityData.defineId(FumetsuSkull.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_POWER = SynchedEntityData.defineId(FumetsuSkull.class, EntityDataSerializers.FLOAT);

    public FumetsuSkull(EntityType<? extends FumetsuSkull> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
    public void setup(Type type, FumetsuEntity owner, Vec3 start, Vec3 vector, float power) {
        this.setSkullType(type);
        this.setOwner(owner);
        this.setPower(power);
        this.setTarget(owner.getTarget());

        float xRot = (float) Math.toDegrees(-Mth.atan2(vector.y(), vector.horizontalDistance()));
        float yRot = (float) Math.toDegrees(-Mth.atan2(vector.x(), vector.z()));

        xRot += this.random.nextFloat(-22.5f, 22.5f);
        yRot += this.random.nextFloat(-22.5f, 22.5f);

        this.moveTo(start.x(), start.y(), start.z(), yRot, xRot);

        this.setDeltaMovement(this.getPoweredRotVec());
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(DATA_TYPE, "CYAN");
        this.getEntityData().define(DATA_OWNER, 0);
        this.getEntityData().define(DATA_TARGET, 0);
        this.getEntityData().define(DATA_POWER, 1f);
    }

    public Type getSkullType() {
        return Type.of(this.getEntityData().get(DATA_TYPE));
    }
    public void setSkullType(Type type) {
        this.getEntityData().set(DATA_TYPE, type.name());
    }

    public boolean ownedBy(Entity pEntity) {
        return Objects.equals(pEntity, this.getOwner());
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
    public Vec3 getPoweredRotVec() {
        return this.getViewVector(1).scale(this.getEntityData().get(DATA_POWER));
    }
    public void setPower(float power) {
        this.getEntityData().set(DATA_POWER, power);
    }

    @Override
    public void fumetsuTick() {
        if(this.tickCount > this.getAge() || !this.getLevel().hasChunkAt(this.blockPosition()) || this.getOwner() == null || this.getOwner().isRemoved()) {
            this.discard();
            return;
        }
        this.noPhysics = true;
        this.baseTick();
        this.skullTick();
    }

    @Override
    public void tick() {}

    protected void skullTick() {
        Vec3 movement = this.getDeltaMovement();
        double moveX = this.getX() + movement.x();
        double moveY = this.getY() + movement.y();
        double moveZ = this.getZ() + movement.z();

        FumetsuEntity fumetsu = this.getOwner();
        if(fumetsu != null) {
            this.setTarget(fumetsu.getTarget() == null ? fumetsu : fumetsu.getTarget());

            if (fumetsu.isAvailableTarget(this.getTarget())) {
                EntityHitResult hit = this.rayBoxTrace(movement, this.getBoundingBox().expandTowards(movement).inflate(1));
                if (hit != null) {
                    this.onHitEntity(hit);
                }

                Vec3 homing = this.getHome().subtract(this.getBoundingBox().getCenter());

                float delta = this.getHomingDelta(homing);

                float homingXRot = (float) Math.toDegrees(-Mth.atan2(homing.y(), homing.horizontalDistance()));
                float homingYRot = (float) Math.toDegrees(-Mth.atan2(homing.x(), homing.z()));

                homingXRot = Mth.rotLerp(delta, this.getXRot(), homingXRot);
                homingYRot = Mth.rotLerp(delta, this.getYRot(), homingYRot);

                this.setXRot(homingXRot);
                this.setYRot(homingYRot);

                movement = this.getPoweredRotVec();

                moveX = this.getX() + movement.x();
                moveY = this.getY() + movement.y();
                moveZ = this.getZ() + movement.z();
            }
        }

        this.setDeltaMovement(movement);
        this.setPos(moveX, moveY, moveZ);

        this.getLevel().addParticle(this.getParticle(), moveX, moveY + 0.5, moveZ, 0, 0, 0);
        this.checkInsideBlocks();
    }
    protected int getAge() {
        return 400;
    }
    protected float getHomingDelta(Vec3 homing) {
        float plus = 0;

        if(this.getTarget().equals(this.getOwner())) {
            return 0.25f;
        } else {

            if(homing.length() < 2) {
                plus = (float) (2 - homing.length()) * 30 ;
            }

            return Math.min(1, (float) Calculates.curve(Math.min(1, (this.tickCount + plus) / 100d), 0, 0.3, 1));
        }
    }
    protected Vec3 getHome() {
        return this.getTarget().getBoundingBox().getCenter();
    }
    protected ParticleOptions getParticle() {
        Vector3f color = switch (this.getSkullType()) {
            case CYAN -> new Vector3f(0, 1, 1);
            case CRIMSON -> new Vector3f(1, 0, 0);
            default -> new Vector3f(1, 1, 1);
        };

        return new GashatParticleOptions(color, this.random.nextFloat(0.2f, 0.6f), 0.1f, this.random.nextBoolean() ? 4 : -4);
    }
    @Nullable
    protected EntityHitResult rayBoxTrace(Vec3 delta, AABB area) {
        AABB aabb = this.getBoundingBox().inflate(1);
        List<Entity> entities = this.getLevel().getEntities(this, area, this::canHitEntity);
        Entity current = null;

        for(int count = 0; count < 2; ++count) {
            for (Entity entity : entities) {
                if(aabb.intersects(entity.getBoundingBox()) && (current == null || (this.distanceTo(current) > this.distanceTo(entity)))) {
                    current = entity;
                }
            }
            aabb = aabb.move(delta);
        }

        return current == null ? null : new EntityHitResult(current);
    }
    protected void onHitEntity(EntityHitResult pResult) {
        FumetsuEntity fumetsu = this.getOwner();
        if(fumetsu != null && fumetsu.isAvailableTarget(pResult.getEntity())) {
            if(this.getLevel() instanceof ServerLevel serverLevel) {
                NovelHandler.playSound(serverLevel, pResult.getEntity().position());
            }

            this.getLevel().getEntities(this, pResult.getEntity().getBoundingBox().inflate(1), this::canHitEntity).forEach(entity -> {

                NovelHandler.novelize(fumetsu, entity, false);
                if(entity instanceof LivingEntity living) {
                    int max;
                    if(this.getSkullType() == Type.CRYSTAL) {
                        living.removeAllEffects();
                        max = 20;
                    } else {
                        max = this.random.nextInt(4, 10);
                    }
                    for (int count = 0; count < max && !NovelHandler.novelized(living); count++) {
                        NovelHandler.novelize(fumetsu, living, false);
                    }
                }
            });

            this.discard();
        }
    }

    protected boolean canHitEntity(Entity pTarget) {
        FumetsuEntity fumetsu = this.getOwner();
        if(fumetsu != null) {
            return !this.ownedBy(pTarget) && fumetsu.getTarget() != null && pTarget.getType() == fumetsu.getTarget().getType() && fumetsu.isAvailableTarget(pTarget);
        } else {
            return false;
        }
    }
    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {return false;}
    @Override
    public void kill() {}

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        double size = this.getBoundingBox().getSize() * 4;
        if (Double.isNaN(size)) {
            size = 4;
        }

        size *= 64;
        return pDistance < size * size;
    }
    @Override
    public boolean isOnFire() {
        return false;
    }
    @Override
    public float getBrightness() {
        return 1;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putString("SkullType", this.getSkullType().name());
        pCompound.putInt("SkullOwner", this.getEntityData().get(DATA_OWNER));
        pCompound.putInt("SkullTarget", this.getEntityData().get(DATA_TARGET));
        pCompound.putFloat("SkullPower", this.getEntityData().get(DATA_POWER));
    }
    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.setSkullType(Type.of(pCompound.getString("SkullType")));
        this.getEntityData().set(DATA_OWNER, pCompound.getInt("SkullOwner"));
        this.getEntityData().set(DATA_TARGET, pCompound.getInt("SkullTarget"));
        this.setPower(pCompound.getFloat("SkullPower"));
    }
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public enum Type {
        CYAN(identifier(HYPERDAIMC, "textures/entity/fumetsu_skull_cyan.png")),
        CRIMSON(identifier(HYPERDAIMC, "textures/entity/fumetsu_skull_crimson.png")),
        CRYSTAL(identifier(HYPERDAIMC, "textures/entity/fumetsu_skull_crystal.png"));

        private static final Map<String, Type> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(Type::name, type -> type));

        private final ResourceLocation texture;
        Type(ResourceLocation texture) {
            this.texture = texture;
        }

        public static Type of(String name) {
            return BY_NAME.get(name);
        }
        public ResourceLocation getTexture() {
            return this.texture;
        }
    }
}
