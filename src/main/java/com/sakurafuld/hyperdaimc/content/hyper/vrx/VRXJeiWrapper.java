package com.sakurafuld.hyperdaimc.content.hyper.vrx;

public abstract class VRXJeiWrapper<I> {
    private static final VRXJeiWrapper<?> EMPTY = new VRXJeiWrapper<>(null) {
        @Override
        public VRXType type() {
            return VRXType.empty();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void accept(int containerId, VRXSlot slot) {
        }
    };

    @SuppressWarnings("unchecked")
    public static <I> VRXJeiWrapper<I> empty() {
        return (VRXJeiWrapper<I>) EMPTY;
    }

    protected final I ingredient;

    protected VRXJeiWrapper(I ingredient) {
        this.ingredient = ingredient;
    }

    public abstract VRXType type();

    public boolean isEmpty() {
        return this.type().isEmpty();
    }

    public abstract void accept(int containerId, VRXSlot slot);
}
