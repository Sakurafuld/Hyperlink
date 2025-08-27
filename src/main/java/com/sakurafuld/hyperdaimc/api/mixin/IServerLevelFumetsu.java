package com.sakurafuld.hyperdaimc.api.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.entity.EntityTickList;

import java.util.Set;

public interface IServerLevelFumetsu {
    void fumetsuSpawn(Entity entity);

    EntityTickList fumetsuTickList();

    Set<Mob> fumetsuNavi();
}
