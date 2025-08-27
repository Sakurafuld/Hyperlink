package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import com.sakurafuld.hyperdaimc.content.hyper.fumetsu.FumetsuHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin<T extends Entity> {
    @Inject(method = "loadEntityRecursive", at = @At(value = "INVOKE", target = "Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;", ordinal = 0))
    private static void loadEntityRecursiveFumetsu$BEFORE(CompoundTag pCompound, Level pLevel, Function<Entity, Entity> pEntityFunction, CallbackInfoReturnable<Entity> cir) {
        FumetsuHandler.spawn.set(true);
    }

    @Inject(method = "loadEntityRecursive", at = @At(value = "INVOKE", target = "Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;", ordinal = 0, shift = At.Shift.AFTER))
    private static void loadEntityRecursiveFumetsu$AFTER(CompoundTag pCompound, Level pLevel, Function<Entity, Entity> pEntityFunction, CallbackInfoReturnable<Entity> cir) {
        FumetsuHandler.spawn.set(false);
    }

    @Inject(method = "create(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/CompoundTag;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;", at = @At("HEAD"))
    private void create$HEAD(ServerLevel pLevel, CompoundTag pNbt, Consumer<T> pConsumer, BlockPos pPos, MobSpawnType pSpawnType, boolean pShouldOffsetY, boolean pShouldOffsetYMore, CallbackInfoReturnable<T> cir) {
        FumetsuHandler.spawn.set(true);
    }

    @Inject(method = "create(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/CompoundTag;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;", at = @At("RETURN"))
    private void create$RETURN(ServerLevel pLevel, CompoundTag pNbt, Consumer<T> pConsumer, BlockPos pPos, MobSpawnType pSpawnType, boolean pShouldOffsetY, boolean pShouldOffsetYMore, CallbackInfoReturnable<T> cir) {
        FumetsuHandler.spawn.set(false);
    }
}
