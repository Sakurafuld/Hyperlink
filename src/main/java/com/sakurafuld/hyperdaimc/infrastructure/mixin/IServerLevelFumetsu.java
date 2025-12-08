package com.sakurafuld.hyperdaimc.infrastructure.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.Set;

public interface IServerLevelFumetsu {
    void hyperdaimc$fumetsuSpawn(Entity entity);

    FumetsuTickList hyperdaimc$fumetsuTickList();

    Set<Mob> hyperdaimc$fumetsuNavi();
}
