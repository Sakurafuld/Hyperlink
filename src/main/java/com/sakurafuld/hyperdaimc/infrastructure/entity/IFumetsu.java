package com.sakurafuld.hyperdaimc.infrastructure.entity;

public interface IFumetsu {
    void fumetsuTick();

    boolean isMovable();

    void setMovable(boolean movable);

}
