package com.sakurafuld.hyperdaimc.api.mixin;

public interface ILivingEntityMuteki {
    boolean muteki();

    boolean initialized();

    default void force(boolean force) {}

    default boolean forced() {return false;}

    float lastHealth();
}
