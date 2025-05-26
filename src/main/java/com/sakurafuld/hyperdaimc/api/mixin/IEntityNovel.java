package com.sakurafuld.hyperdaimc.api.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface IEntityNovel {
    void novelRemove(Entity.RemovalReason reason);

    void novelize(LivingEntity writer);

    boolean isNovelized();
}
