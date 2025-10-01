package com.sakurafuld.hyperdaimc.api.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.Set;

public interface IServerLevelFumetsu {
    void fumetsuSpawn(Entity entity);

    FumetsuTickList fumetsuTickList();

    Set<Mob> fumetsuNavi();
}
