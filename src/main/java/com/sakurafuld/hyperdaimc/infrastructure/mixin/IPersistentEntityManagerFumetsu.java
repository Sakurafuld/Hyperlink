package com.sakurafuld.hyperdaimc.infrastructure.mixin;

import net.minecraft.world.entity.Entity;

import java.util.Set;
import java.util.UUID;

public interface IPersistentEntityManagerFumetsu {
    void hyperdaimc$fumetsuSpawn(Entity entity);

    Set<UUID> hyperdaimc$fumetsuKnown();
}
