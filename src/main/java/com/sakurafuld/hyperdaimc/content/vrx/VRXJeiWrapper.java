package com.sakurafuld.hyperdaimc.content.vrx;

public abstract class VRXJeiWrapper<I> {
    protected final I ingredient;

    protected VRXJeiWrapper(I ingredient) {
        this.ingredient = ingredient;
    }

    public abstract void accept(int containerId, VRXSlot slot);
}
