package com.sakurafuld.hyperdaimc.api.mixin;

public interface ILivingEntityMuteki {
    default void mutekiForce(boolean force) {
    }

    default boolean mutekiForced() {
        return false;
    }

    boolean muteki();

    float mutekiLastHealth();
}
