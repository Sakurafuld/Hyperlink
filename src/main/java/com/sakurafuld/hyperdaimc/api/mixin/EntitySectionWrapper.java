package com.sakurafuld.hyperdaimc.api.mixin;

import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class EntitySectionWrapper<T extends EntityAccess> extends EntitySection<T> {
    private final EntitySection<T> o;

    public EntitySectionWrapper(EntitySection<T> o) {
        super(o.storage.baseClass, o.chunkStatus);
        this.o = o;
    }

    @Override
    public void add(T pEntity) {
        this.o.add(pEntity);
    }

    @Override
    public boolean remove(T pEntity) {
        return this.o.remove(pEntity);
    }

    @Override
    public void getEntities(AABB pBounds, Consumer<T> pConsumer) {
        this.o.getEntities(pBounds, pConsumer);
    }

    @Override
    public <U extends T> void getEntities(EntityTypeTest<T, U> pTest, AABB pBounds, Consumer<? super U> pConsumer) {
        this.o.getEntities(pTest, pBounds, pConsumer);
    }

    @Override
    public boolean isEmpty() {
        return this.o.isEmpty();
    }

    @Override
    public @NotNull Stream<T> getEntities() {
        return this.o.getEntities();
    }

    @Override
    public @NotNull Visibility getStatus() {
        return this.o.getStatus();
    }

    @Override
    public @NotNull Visibility updateChunkStatus(Visibility pChunkStatus) {
        return this.o.updateChunkStatus(pChunkStatus);
    }

    @VisibleForDebug
    public int size() {
        return this.o.size();
    }
}
