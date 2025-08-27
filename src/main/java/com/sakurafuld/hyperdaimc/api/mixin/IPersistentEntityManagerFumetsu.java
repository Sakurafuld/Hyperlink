package com.sakurafuld.hyperdaimc.api.mixin;

import net.minecraft.world.entity.Entity;

import java.util.Set;
import java.util.UUID;

public interface IPersistentEntityManagerFumetsu {
    void fumetsuSpawn(Entity entity);

    Set<UUID> fumetsuKnown();
}
