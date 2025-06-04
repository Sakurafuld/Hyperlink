package com.sakurafuld.hyperdaimc.mixin.fumetsu;

import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PersistentEntitySectionManager.class)
public interface PersistentEntitySectionManagerAccessor<T extends EntityAccess> {
    @Invoker(value = "addEntityWithoutEvent", remap = false)
    boolean spawn(T pEntity, boolean pWorldGenSpawned);
}
