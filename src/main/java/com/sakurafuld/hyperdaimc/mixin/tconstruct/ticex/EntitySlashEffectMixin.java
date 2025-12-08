package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.sakurafuld.hyperdaimc.content.hyper.novel.NovelHandler;
import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntitySlashEffectTicEx;
import com.sakurafuld.hyperdaimc.infrastructure.render.GashatParticleOptions;
import mods.flammpfeil.slashblade.entity.EntitySlashEffect;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@Pseudo
@Mixin(EntitySlashEffect.class)
public abstract class EntitySlashEffectMixin extends ProjectileMixin implements IEntitySlashEffectTicEx {
    @Unique
    private static final EntityDataAccessor<Boolean> DATA_SPECIAL = SynchedEntityData.defineId(EntitySlashEffect.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final String KEY_SPECIAL = HYPERDAIMC + ":SpecialSlash";

    @Inject(locals = LocalCapture.CAPTURE_FAILSOFT, method = "tick", at = @At(value = "INVOKE", target = "Lmods/flammpfeil/slashblade/entity/EntitySlashEffect;getShooter()Lnet/minecraft/world/entity/Entity;", ordinal = 0, remap = false))
    private void tickTicEx(CallbackInfo ci, Vec3 start, Vector4f normal, Vector4f dir, float progress, Vec3 normal3d, BlockHitResult rayResult) {
        if (!NovelHandler.ModifierRenderingLevel.ALL.check())
            return;
        EntitySlashEffect self = (EntitySlashEffect) (Object) this;
        if (self.level() instanceof ServerLevel level && this.hyperdaimc$isTicExNovel()) {
            RandomSource random = self.level().getRandom();
            GashatParticleOptions options = GashatParticleOptions.drop(random::nextFloat, 0.25f);
            Vec3 hit = rayResult.getLocation();
            Direction direction = rayResult.getDirection();
            level.sendParticles(options, hit.x(), hit.y(), hit.z(), 8, direction.getStepX() * (0.5 + random.nextDouble()), direction.getStepY() * (0.5 + random.nextDouble()), direction.getStepZ() * (0.5 + random.nextDouble()), 1);
        }
    }

    @Override
    public boolean hyperdaimc$isSpecial() {
        EntitySlashEffect self = (EntitySlashEffect) (Object) this;
        return self.getEntityData().get(DATA_SPECIAL);
    }

    @Override
    public void hyperdaimc$setSpecial(boolean special) {
        EntitySlashEffect self = (EntitySlashEffect) (Object) this;
        self.getEntityData().set(DATA_SPECIAL, special);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializerTicEx(EntityType<?> pEntityType, Level pLevel, CallbackInfo ci) {
        EntitySlashEffect self = (EntitySlashEffect) (Object) this;
        self.getEntityData().define(DATA_SPECIAL, false);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void addAdditionalSaveDataTicEx(CompoundTag compound, CallbackInfo ci) {
        if (this.hyperdaimc$isSpecial())
            compound.putBoolean(KEY_SPECIAL, true);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readAdditionalSaveDataTicEx(CompoundTag pCompound, CallbackInfo ci) {
        if (pCompound.contains(KEY_SPECIAL))
            this.hyperdaimc$setSpecial(pCompound.getBoolean(KEY_SPECIAL));
        else this.hyperdaimc$setSpecial(false);
    }
}
