package com.sakurafuld.hyperdaimc.infrastructure.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface IEntityNovel {
    void hyperdaimc$novelRemove(Entity.RemovalReason reason);

    void hyperdaimc$novelize(LivingEntity writer);

    boolean hyperdaimc$isNovelized();

    void hyperdaimc$setNovelized();

    default int hyperdaimc$novelDead(boolean increment) {
        return Integer.MAX_VALUE;
    }
}
