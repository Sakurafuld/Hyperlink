package com.sakurafuld.hyperdaimc.infrastructure.mixin;

import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityLookupWrapper<T extends EntityAccess> extends EntityLookup<T> {
    private final EntityLookup<T> o;

    public EntityLookupWrapper(EntityLookup<T> o) {
        this.o = o;
    }

    @Override
    public <U extends T> void getEntities(EntityTypeTest<T, U> pTest, AbortableIterationConsumer<U> pConsumer) {
        this.o.getEntities(pTest, pConsumer);
    }

    @Override
    public @NotNull Iterable<T> getAllEntities() {
        return this.o.getAllEntities();
    }

    @Override
    public void add(T pEntity) {
        this.o.add(pEntity);
    }

    @Override
    public void remove(T pEntity) {
        this.o.remove(pEntity);
    }

    @Nullable
    @Override
    public T getEntity(int pId) {
        return this.o.getEntity(pId);
    }

    @Nullable
    @Override
    public T getEntity(UUID pUuid) {
        return this.o.getEntity(pUuid);
    }

    @Override
    public int count() {
        return this.o.count();
    }
}
