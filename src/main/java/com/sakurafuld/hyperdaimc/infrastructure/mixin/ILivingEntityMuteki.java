package com.sakurafuld.hyperdaimc.infrastructure.mixin;

public interface ILivingEntityMuteki {
    default void hyperdaimc$mutekiForce(boolean force) {
    }

    default boolean hyperdaimc$isMutekiForced() {
        return false;
    }

    boolean hyperdaimc$muteki();

    float hyperdaimc$mutekiLastHealth();

    default void hyperdaimc$mutekiSetLocal(float local) {
    }

    void hyperdaimc$mutekiNovelize();
}
