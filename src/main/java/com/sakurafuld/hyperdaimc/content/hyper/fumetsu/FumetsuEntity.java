package com.sakurafuld.hyperdaimc.content.hyper.fumetsu;

import com.mojang.math.Vector3f;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.api.content.GashatParticleOptions;
import com.sakurafuld.hyperdaimc.api.content.IFumetsu;
import com.sakurafuld.hyperdaimc.api.mixin.IEntityNovel;
import com.sakurafuld.hyperdaimc.api.mixin.ILivingEntityMuteki;
import com.sakurafuld.hyperdaimc.api.mixin.IServerLevelFumetsu;
import com.sakurafuld.hyperdaimc.content.HyperEntities;
import com.sakurafuld.hyperdaimc.content.HyperItems;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.ai.*;
import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.skull.FumetsuSkull;
import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.helper.Boxes;
import com.sakurafuld.hyperdaimc.helper.Writes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

public class FumetsuEntity extends Monster implements IFumetsu, ILivingEntityMuteki {
    private static final EntityDataAccessor<Boolean> DATA_GENOCIDE = SynchedEntityData.defineId(FumetsuEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TARGET = SynchedEntityData.defineId(FumetsuEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> DATA_ORIGIN = SynchedEntityData.defineId(FumetsuEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Boolean> DATA_STORM = SynchedEntityData.defineId(FumetsuEntity.class, EntityDataSerializers.BOOLEAN);

    private long login = System.currentTimeMillis();
    private final float[] xRotOHeads = new float[2];
    private final float[] yRotOHeads = new float[2];
    private final float[] xRotHeads = new float[2];
    private final float[] yRotHeads = new float[2];
    public float wingModelXRot = 0;
    public float wingModelZRot = 0;
    public int genocideTime = 0;
    private float lastHealth = 1;
    private Vec3 lastPos = this.position();
    private Vec3 lastDelta = this.getDeltaMovement();
    private float lastXRot = this.getXRot();
    private float lastYRot = this.getYRot();
    private float lastYHeadRot = this.getYHeadRot();
    private boolean movable = false;

    public FumetsuEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new FumetsuMoveControl();
        this.xpReward = 0;
    }

    public static AttributeSupplier createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .build();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DATA_GENOCIDE, false);
        this.getEntityData().define(DATA_TARGET, 0);
        this.getEntityData().define(DATA_ORIGIN, Boxes.INVALID);
        this.getEntityData().define(DATA_STORM, false);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        FlyingPathNavigation navi = new FlyingPathNavigation(this, pLevel);
        navi.setCanOpenDoors(false);
        navi.setCanFloat(true);
        navi.setCanPassDoors(true);
        return navi;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(-1, new FumetsuStormGoal(this));
        this.goalSelector.addGoal(0, new FumetsuAttackGoal(this, (float) Math.sqrt(Float.MAX_VALUE)));
        this.goalSelector.addGoal(1, new FumetsuRandomMoveGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 16));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new FumetsuNoTargetGoal(this));
        this.targetSelector.addGoal(1, new FumetsuGenocideTargetGoal<>(this, Player.class));
        this.targetSelector.addGoal(2, new FumetsuGenocideTargetGoal<>(this, LivingEntity.class));
    }

    @Override
    public void fumetsuTick() {
        if (!this.firstTick) {
            this.setPosRaw(this.lastPos.x(), this.lastPos.y(), this.lastPos.z());
            this.setDeltaMovement(this.lastDelta);
            this.setXRot(this.lastXRot);
            this.setYRot(this.lastYRot);
            this.setYHeadRot(this.lastYHeadRot);
            this.setOldPosAndRot();
        }
        this.mutekiForce(true);
        float health = this.getHealth();
        if (health > 0) {
            this.lastHealth = health;
        }
        this.mutekiForce(false);

        if (this.login < FumetsuHandler.logout.get()) {
            if (this.getLevel() instanceof IServerLevelFumetsu levelFumetsu) {

                Random random = this.getRandom();
                AABB aabb = this.getBoundingBox();
                Vec3 min = new Vec3(aabb.minX, aabb.minY, aabb.minZ);
                int max = random.nextInt(8, 13);
                for (int count = 0; count < max; count++) {
                    Vec3 pos = min.add(random.nextDouble(aabb.getXsize()), random.nextDouble(aabb.getYsize()), random.nextDouble(aabb.getZsize()));
                    ItemEntity entity = new ItemEntity(this.getLevel(), pos.x(), pos.y(), pos.z(), new ItemStack(HyperItems.BUG_STARS.get(0).get()), 0, 0, 0);
                    entity.setNoGravity(true);
                    entity.setGlowingTag(true);
                    entity.setUnlimitedLifetime();
                    levelFumetsu.fumetsuSpawn(entity);
                }
                this.getLevel().broadcastEntityEvent(this, EntityEvent.POOF);
            }

            ((IEntityNovel) this).novelRemove(RemovalReason.DISCARDED);
            LOG.debug("FumetsuLogout-this:{},handler:{}", this.login, FumetsuHandler.logout.get());
            return;
        }
        if (this.isGenocide()) {
            this.genocideTime++;
        }
        if (!this.isGenocide() && this.getHealth() < this.getMaxHealth()) {
            this.genocide();
        }
        if (this.getOrigin().equals(Boxes.INVALID)) {
            this.getEntityData().set(DATA_ORIGIN, this.blockPosition());
        }

        if (this.getEntityData().get(DATA_TARGET) > 0) {
            Entity target = this.getLevel().getEntity(this.getEntityData().get(DATA_TARGET));
            if (!this.isAvailableTarget(target)) {
                this.getEntityData().set(DATA_TARGET, 0);
            }
        }
        if (this.getXRot() > 35) {
            this.setXRot(35);
        }

        this.noPhysics = true;
        this.defaultTick();
        this.noPhysics = false;
        this.setNoGravity(true);

        if (this.tickCount % 2 == 0) {
            Vec3 center = this.getBoundingBox().getCenter();
            this.getLevel().addParticle(
                    new GashatParticleOptions(new Vector3f(this.getRandom().nextFloat(), this.getRandom().nextFloat(), this.getRandom().nextFloat()), 0.5f, 0.1f, 10),
                    center.x() + this.getRandom().nextInt(-2, 2), center.y() + this.getRandom().nextInt(-2, 2), center.z() + this.getRandom().nextInt(-2, 2), 0, 0, 0);
        }

        if (this.tickCount % 20 == 0) {
            this.heal(2);
        }

        this.lastPos = this.position();
        this.lastDelta = this.getDeltaMovement();
        this.lastXRot = this.getXRot();
        this.lastYRot = this.getYRot();
        this.lastYHeadRot = this.getYHeadRot();
    }

    private void defaultTick() {
        this.baseTick();
        if (!this.getLevel().isClientSide()) {
            int i = this.getArrowCount();
            if (i > 0) {
                if (this.removeArrowTime <= 0) {
                    this.removeArrowTime = 20 * (30 - i);
                }

                --this.removeArrowTime;
                if (this.removeArrowTime <= 0) {
                    this.setArrowCount(i - 1);
                }
            }

            int j = this.getStingerCount();
            if (j > 0) {
                if (this.removeStingerTime <= 0) {
                    this.removeStingerTime = 20 * (30 - j);
                }

                --this.removeStingerTime;
                if (this.removeStingerTime <= 0) {
                    this.setStingerCount(j - 1);
                }
            }

            if (this.tickCount % 20 == 0) {
                this.getCombatTracker().recheckStatus();
            }
        }

        this.aiStep();
        double dx = this.getX() - this.xo;
        double dz = this.getZ() - this.zo;
        float distance = (float) (dx * dx + dz * dz);
        float yBodyRot = this.yBodyRot;
        float animStep = 0;
        this.oRun = this.run;
        float running = 0;
        if (distance > 0.0025000002F) {
            running = 1;
            animStep = (float) Math.sqrt(distance) * 3;
            float f4 = (float) Mth.atan2(dz, dx) * (180F / (float) Math.PI) - 90;
            float f5 = Mth.abs(Mth.wrapDegrees(this.getYRot()) - f4);
            if (95 < f5 && f5 < 265) {
                yBodyRot = f4 - 180;
            } else {
                yBodyRot = f4;
            }
        }

        if (this.attackAnim > 0) {
            yBodyRot = this.getYRot();
        }

        if (!this.onGround) {
            running = 0;
        }

        this.run += (running - this.run) * 0.3F;
        this.getLevel().getProfiler().push("headTurn");
        animStep = this.tickHeadTurn(yBodyRot, animStep);
        this.getLevel().getProfiler().pop();
        this.getLevel().getProfiler().push("rangeChecks");

        while (this.getYRot() - this.yRotO < -180) {
            this.yRotO -= 360;
        }

        while (this.getYRot() - this.yRotO >= 180) {
            this.yRotO += 360;
        }

        while (this.yBodyRot - this.yBodyRotO < -180) {
            this.yBodyRotO -= 360;
        }

        while (this.yBodyRot - this.yBodyRotO >= 180) {
            this.yBodyRotO += 360;
        }

        while (this.getXRot() - this.xRotO < -180) {
            this.xRotO -= 360;
        }

        while (this.getXRot() - this.xRotO >= 180) {
            this.xRotO += 360;
        }

        while (this.getYHeadRot() - this.yHeadRotO < -180) {
            this.yHeadRotO -= 360;
        }

        while (this.getYHeadRot() - this.yHeadRotO >= 180) {
            this.yHeadRotO += 360;
        }

        this.getLevel().getProfiler().pop();
        this.animStep += animStep;
        if (this.isFallFlying()) {
            ++this.fallFlyTicks;
        } else {
            this.fallFlyTicks = 0;
        }

        if (!this.getLevel().isClientSide()) {
            this.tickLeash();
            if (this.tickCount % 5 == 0) {
                this.updateControlFlags();
            }
        }
        if (this.getVehicle() != null) {
            this.stopRiding();
            while (this.getVehicle() != null) {
                this.removeVehicle();
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        Entity target = this.getTarget();
        if (!this.isStorming()) {
            if (target != null) {

                double dx = target.getX() - this.getX();
                double dy = target.getEyeY() - this.getEyeY();
                double dz = target.getZ() - this.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);

                this.setYRot(Mth.rotateIfNecessary(this.getYRot(), (float) Math.toDegrees(-Mth.atan2(dx, dz)), 2));
                this.setYHeadRot(this.getYRot());
                this.setYBodyRot(this.getYRot());
                this.setXRot(Mth.rotateIfNecessary(this.getXRot(), Math.min(35, (float) Math.toDegrees(-Mth.atan2(dy, distance))), 2));
                if (!this.getLevel().isClientSide() && this.getLevel().getGameTime() % 100 == 0) {
                    NovelHandler.novelize(this, target, true);
                }
            } else {
                this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0.6, 1));
            }


            for (int head = 0; head < 2; ++head) {
                this.yRotOHeads[head] = this.yRotHeads[head];
                this.xRotOHeads[head] = this.xRotHeads[head];
                if (target != null) {
                    double headX = this.getHeadX(head + 1);
                    double headY = this.getHeadY(head + 1);
                    double headZ = this.getHeadZ(head + 1);

                    double dx = target.getX() - headX;
                    double dy = target.getEyeY() - headY;
                    double dz = target.getZ() - headZ;
                    double distance = Math.sqrt(dx * dx + dz * dz);

                    float xRot = (float) Math.toDegrees(-Mth.atan2(dy, distance));
                    float yRot = (float) Math.toDegrees(-Mth.atan2(dx, dz));

                    this.xRotHeads[head] = Mth.rotateIfNecessary(this.xRotHeads[head], xRot, this.getMaxHeadXRot());
                    this.yRotHeads[head] = Mth.rotateIfNecessary(this.yRotHeads[head], yRot, this.getHeadRotSpeed());
                } else {
                    this.xRotHeads[head] = Mth.rotateIfNecessary(this.xRotHeads[head], this.getXRot(), 10);
                    this.yRotHeads[head] = Mth.rotateIfNecessary(this.yRotHeads[head], this.yBodyRot, 10);
                }
            }
        } else {
            Vec3 centerHead = new Vec3(this.getHeadX(0), this.getHeadY(0), this.getHeadZ(0));

            if (target != null) {
                double dx = target.getX() - this.getX();
                double dz = target.getZ() - this.getZ();

                this.setXRot(Mth.rotateIfNecessary(this.getXRot(), -40, 2));
                this.setYRot(Mth.rotateIfNecessary(this.getYRot(), (float) Math.toDegrees(-Mth.atan2(dx, dz)), 2));
                this.setYHeadRot(this.getYRot());
                this.setYBodyRot(this.getYRot());
            }

            for (int head = 0; head < 2; ++head) {
                this.yRotOHeads[head] = this.yRotHeads[head];
                this.xRotOHeads[head] = this.xRotHeads[head];

                if (target != null) {
                    Vec3 sideHead = new Vec3(this.getHeadX(head + 1), this.getHeadY(head + 1), this.getHeadZ(head + 1));
                    Vec3 vec = sideHead.subtract(centerHead);

                    this.xRotHeads[head] = Mth.rotateIfNecessary(this.xRotHeads[head], (float) Math.toDegrees(-Mth.atan2(vec.y(), vec.horizontalDistance())), 2);
                    this.yRotHeads[head] = Mth.rotateIfNecessary(this.yRotHeads[head], (float) Math.toDegrees(-Mth.atan2(vec.x(), vec.z())), 2);
                }
            }
        }
    }

    @Override
    public boolean isMovable() {
        return this.movable;
    }

    @Override
    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    @Override
    public void move(MoverType pType, Vec3 pPos) {
        if (this.isMovable()) {
            super.move(pType, pPos);
            this.checkInsideBlocks();
        }
    }

    @Override
    protected void checkInsideBlocks() {
        boolean old = this.isMovable();
        this.setMovable(false);
        super.checkInsideBlocks();
        this.setMovable(old);
    }

    @Override
    public void setXRot(float pXRot) {
        if (this.isMovable()) {
            super.setXRot(pXRot);
        }
    }

    @Override
    public void setYRot(float pYRot) {
        if (this.isMovable()) {
            super.setYRot(pYRot);
        }
    }

    @Override
    public void setYHeadRot(float pRotation) {
        if (this.isMovable()) {
            super.setYHeadRot(pRotation);
        }
    }

    @Override
    public void setDeltaMovement(Vec3 pDeltaMovement) {
        if (this.isMovable()) {
            super.setDeltaMovement(pDeltaMovement);
        }
    }

    @Override
    public boolean muteki() {
        return HyperCommonConfig.ENABLE_MUTEKI.get();
    }

    @Override
    public float mutekiLastHealth() {
        return this.lastHealth;
    }

    @Override
    public void tick() {
    }


    public void shoot(FumetsuSkull.Type type, Vec3 start, Vec3 target, float power) {
        if (this.getLevel() instanceof ServerLevel serverLevel) {
            FumetsuSkull skull = new FumetsuSkull(HyperEntities.FUMETSU_SKULL.get(), this.getLevel());

            skull.setup(type, this, start, target.subtract(start), power);

            ((IServerLevelFumetsu) serverLevel).fumetsuSpawn(skull);
            serverLevel.playSound(null, start.x(), start.y(), start.z(), HyperSounds.FUMETSU_SHOOT.get(), SoundSource.HOSTILE, 2, 1 + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.2f);
        }
    }

    public void shoot(FumetsuSkull.Type type, int head, Vec3 target, float power) {
        this.shoot(type, new Vec3(this.getHeadX(head), this.getHeadY(head), this.getHeadZ(head)), target, power);
    }

    public void shoot(FumetsuSkull.Type type, int head) {
        if (this.getTarget() != null) {
            this.shoot(type, head, this.getTarget().position(), 1);
        }
    }

    @Override
    public Component getDisplayName() {
        return Writes.gameOver(super.getDisplayName().getString());
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pAmount > 0) {
            this.genocide();
        }
        return super.hurt(pSource, pAmount);
    }

    @Override
    public void die(DamageSource pDamageSource) {
        if (NovelHandler.novelized(this)) {
            super.die(pDamageSource);
        }
    }

    @Override
    protected void tickDeath() {
        if (NovelHandler.novelized(this)) {
            super.tickDeath();
        }
    }

    @Override
    public int getTeamColor() {
        TextColor color = Writes.gameOver("A").getSiblings().get(0).getStyle().getColor();
        return color != null ? color.getValue() : super.getTeamColor();
    }

    public boolean isGenocide() {
        return this.getEntityData().get(DATA_GENOCIDE);
    }

    public void genocide() {
        this.getEntityData().set(DATA_GENOCIDE, true);
    }

    public BlockPos getOrigin() {
        return this.getEntityData().get(DATA_ORIGIN);
    }

    public void originate() {
        this.getEntityData().set(DATA_ORIGIN, Boxes.INVALID);
    }

    public boolean isAvailableTarget(@Nullable Entity target) {
        if (this != target && target instanceof FumetsuEntity) {
            return false;
        } else if (target != null && !(target instanceof Player) && Math.sqrt(this.getOrigin().distToCenterSqr(target.position())) > HyperCommonConfig.FUMETSU_RANGE.get()) {
            return false;
        } else if (target instanceof Player player && (player.isCreative() || player.getHealth() <= 0)) {
            return false;
        } else {
            return target instanceof LivingEntity && !NovelHandler.novelized(target) && !target.isRemoved() && !target.isSpectator();
        }
    }

    public boolean isStorming() {
        return this.getEntityData().get(DATA_STORM);
    }

    public void setStorm(boolean storm) {
        this.getEntityData().set(DATA_STORM, storm);
    }

    @Nullable
    @Override
    public LivingEntity getTarget() {
        if (this.getEntityData().get(DATA_TARGET) > 0 && this.getLevel().getEntity(this.getEntityData().get(DATA_TARGET)) instanceof LivingEntity target) {
            return target;
        } else {
            return null;
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity pTarget) {
        if (this.getEntityData().get(DATA_TARGET) <= 0 && this.isAvailableTarget(pTarget)) {
            this.getEntityData().set(DATA_TARGET, pTarget.getId());
        }
    }

    @Override
    public boolean hasLineOfSight(Entity pEntity) {
        if (pEntity.getLevel() != this.getLevel()) {
            return false;
        } else if (HyperCommonConfig.FUMETSU_UNDERGROUND.get() || pEntity instanceof Player) {
            return true;
        } else {
            Vec3 from = new Vec3(this.getX(), this.getEyeY(), this.getZ());
            Vec3 to = new Vec3(pEntity.getX(), pEntity.getEyeY(), pEntity.getZ());
            BlockHitResult hit = this.getLevel().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            // なかなか面白い挙動.
            return hit.getType() == HitResult.Type.MISS || !(hit.getDirection() == Direction.UP && this.getEyeY() > pEntity.getEyeY());
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return HyperSounds.FUMETSU_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return HyperSounds.FUMETSU_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    public boolean isEffectiveAi() {
        return !this.getLevel().isClientSide();
    }

    @Override
    public boolean startRiding(Entity pEntity, boolean pForce) {
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity pTarget) {
        return true;
    }

    @Override
    public boolean canAttack(LivingEntity pLivingentity, TargetingConditions pCondition) {
        return true;
    }

    @Override
    public boolean canAttackType(EntityType<?> pType) {
        return true;
    }

    @Override
    public boolean isAlliedTo(Entity pEntity) {
        return pEntity instanceof FumetsuEntity;
    }

    @Override
    public boolean isAlliedTo(Team pTeam) {
        return false;
    }

    @Override
    public void checkDespawn() {
        this.noActionTime = 0;
    }

    @Override
    public void makeStuckInBlock(BlockState pState, Vec3 pMotionMultiplier) {
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Nullable
    @Override
    public Entity changeDimension(ServerLevel pDestination) {
        return null;
    }

    @Nullable
    @Override
    public Entity changeDimension(ServerLevel pDestination, ITeleporter teleporter) {
        return null;
    }

    @Override
    public boolean isSilent() {
        return false;
    }

    @Override
    public void onRemovedFromWorld() {
        if (NovelHandler.novelized(this)) {
            super.onRemovedFromWorld();
        }
    }

    public double getHeadX(int head) {
        if (head <= 0) {
            return this.getX();
        } else {
            float yRot = (float) Math.toRadians(this.yBodyRot + (180 * (head - 1)));
            float offset = Mth.cos(yRot);
            return this.getX() + offset * -1.3;
        }
    }

    public double getHeadY(int head) {
        return head <= 0 ? this.getY() + 3 : this.getY() + 2.2;
    }

    public double getHeadZ(int head) {
        if (head <= 0) {
            return this.getZ();
        } else {
            float yRot = (float) Math.toRadians(this.yBodyRot + (180 * (head - 1)));
            float offset = Mth.sin(yRot);
            return this.getZ() + offset * -1.3;
        }
    }

    public float getOldHeadYRot(int head) {
        return this.yRotOHeads[head];
    }

    public float getOldHeadXRot(int head) {
        return this.xRotOHeads[head];
    }

    public float getHeadXRot(int head) {
        return this.xRotHeads[head];
    }

    public float getHeadYRot(int head) {
        return this.yRotHeads[head];
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("GenocideData", this.isGenocide());
        pCompound.putInt("TargetData", this.getEntityData().get(DATA_TARGET));
        pCompound.put("OriginData", NbtUtils.writeBlockPos(this.getOrigin()));
        pCompound.putBoolean("StormData", this.isStorming());
        pCompound.putLong("Login", this.login);
        pCompound.putInt("GenocideTime", this.genocideTime);
        pCompound.put("LastPosFumetsu", this.newDoubleList(this.lastPos.x(), this.lastPos.y(), this.lastPos.z()));
        pCompound.put("LastDeltaFumetsu", this.newDoubleList(this.lastDelta.x(), this.lastDelta.y(), this.lastDelta.z()));
        pCompound.putFloat("LastXRotFumetsu", this.lastXRot);
        pCompound.putFloat("LastYRotFumetsu", this.lastYRot);
        pCompound.putFloat("LastYHeadRotFumetsu", this.lastYHeadRot);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.getEntityData().set(DATA_GENOCIDE, pCompound.getBoolean("GenocideData"));
        this.getEntityData().set(DATA_TARGET, pCompound.getInt("TargetData"));
        this.getEntityData().set(DATA_ORIGIN, NbtUtils.readBlockPos((pCompound.getCompound("OriginData"))));
        this.getEntityData().set(DATA_STORM, pCompound.getBoolean("StormData"));
        if (pCompound.contains("Login")) {
            this.login = pCompound.getLong("Login");
        } else if (!pCompound.contains("Logout")) {
            this.login = System.currentTimeMillis();
            this.originate();
            this.genocide();
        } else {
            this.login = 0;
        }
        this.genocideTime = pCompound.getInt("GenocideTime");
        ListTag lastPos = pCompound.getList("LastPosFumetsu", Tag.TAG_DOUBLE);
        this.lastPos = new Vec3(lastPos.getDouble(0), lastPos.getDouble(1), lastPos.getDouble(2));
        ListTag lastDelta = pCompound.getList("LastDeltaFumetsu", Tag.TAG_DOUBLE);
        this.lastDelta = new Vec3(lastDelta.getDouble(0), lastDelta.getDouble(1), lastDelta.getDouble(2));
        this.lastXRot = pCompound.getFloat("LastXRotFumetsu");
        this.lastYRot = pCompound.getFloat("LastYRotFumetsu");
        this.lastYHeadRot = pCompound.getFloat("LastYHeadRotFumetsu");
    }

    @Override
    public void load(CompoundTag pCompound) {
        this.setMovable(true);
        super.load(pCompound);
        this.setMovable(false);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    class FumetsuMoveControl extends MoveControl {
        public FumetsuMoveControl() {
            super(FumetsuEntity.this);
        }

        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                Vec3 vec3 = new Vec3(this.wantedX - FumetsuEntity.this.getX(), this.wantedY - FumetsuEntity.this.getY(), this.wantedZ - FumetsuEntity.this.getZ());
                double length = vec3.length();
                if (length < FumetsuEntity.this.getBoundingBox().getSize()) {
                    this.operation = MoveControl.Operation.WAIT;
                    FumetsuEntity.this.setDeltaMovement(FumetsuEntity.this.getDeltaMovement().scale(0.5D));
                } else {
                    FumetsuEntity.this.setDeltaMovement(FumetsuEntity.this.getDeltaMovement().add(vec3.scale(this.speedModifier * 0.05D / length)));
                    if (FumetsuEntity.this.getTarget() == null) {
                        Vec3 delta = FumetsuEntity.this.getDeltaMovement();
                        FumetsuEntity.this.setYRot((float) Math.toDegrees(-Mth.atan2(delta.x, delta.z)));
                    }
                }
            }
        }
    }
}
