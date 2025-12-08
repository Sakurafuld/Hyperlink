package com.sakurafuld.hyperdaimc.mixin.tconstruct.ticex;

import com.sakurafuld.hyperdaimc.infrastructure.mixin.IEntityTicEx;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityTicEx {
    @Shadow
    public abstract SynchedEntityData getEntityData();

    @Unique
    private static final EntityDataAccessor<Boolean> DATA_TICEX_NOVEL = SynchedEntityData.defineId(Entity.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private static final String KEY_GASHAT = HYPERDAIMC + ":TicExNovel";

    @Override
    public boolean hyperdaimc$isTicExNovel() {
        return this.getEntityData().get(DATA_TICEX_NOVEL);
    }

    @Override
    public void hyperdaimc$setTicExNovel(boolean novel) {
        this.getEntityData().set(DATA_TICEX_NOVEL, novel);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializerGashat(EntityType<?> pEntityType, Level pLevel, CallbackInfo ci) {
        this.getEntityData().define(DATA_TICEX_NOVEL, false);
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void saveWithoutIdGashat(CompoundTag pCompound, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.hyperdaimc$isTicExNovel())
            pCompound.putBoolean(KEY_GASHAT, true);
    }

    @Inject(method = "load", at = @At("HEAD"))
    private void loadGashat(CompoundTag pCompound, CallbackInfo ci) {
        if (pCompound.contains(KEY_GASHAT))
            this.hyperdaimc$setTicExNovel(pCompound.getBoolean(KEY_GASHAT));
        else this.hyperdaimc$setTicExNovel(false);
    }

    @Inject(method = "rideTick", at = @At("HEAD"))
    protected void rideTickTicEx(CallbackInfo ci) {

    }
}
