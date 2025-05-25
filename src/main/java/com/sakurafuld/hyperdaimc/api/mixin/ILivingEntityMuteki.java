package com.sakurafuld.hyperdaimc.api.mixin;

public interface ILivingEntityMuteki {
    default void force(boolean force) {
    }

    default boolean forced() {
        return false;
    }

    boolean muteki();

    boolean initialized();

    float lastHealth();
}
