package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ClickType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public abstract class VRXOne {
    public static final VRXOne EMPTY;

    public final VRXType type;

    public VRXOne(VRXType type) {
        this.type = type;
    }

    @Nullable
    public abstract Object prepareInsert(CapabilityProvider<?> provider, @Nullable Direction face, List<VRXOne> previous);

    public abstract void insert(CapabilityProvider<?> provider, @Nullable Direction face, Object prepared);

    public final CompoundTag serialize() {
        return this.type.serialize(this);
    }

    protected abstract CompoundTag save();

    public abstract void load(CompoundTag tag);

    public abstract boolean isEmpty();

    public abstract long getQuantity();

    public abstract void setQuantity(long quantity);

    public abstract void stackSlot(VRXMenu menu, VRXSlot slot, int button, ClickType type);

    public abstract boolean scrollSlot(VRXMenu menu, VRXSlot slot, double delta, boolean shiftDown);

    @OnlyIn(Dist.CLIENT)
    public abstract void render(GuiGraphics graphics, int x, int y);

    @OnlyIn(Dist.CLIENT)
    public abstract void renderTooltip(VRXScreen screen, GuiGraphics graphics, int x, int y);

    static {
        EMPTY = new VRXOne(VRXType.empty()) {
            @Override
            public Object prepareInsert(CapabilityProvider<?> provider, @Nullable Direction face, List<VRXOne> previous) {
                return null;
            }

            @Override
            public void insert(CapabilityProvider<?> provider, @Nullable Direction face, Object prepared) {
            }

            @Override
            protected CompoundTag save() {
                return new CompoundTag();
            }

            @Override
            public void load(CompoundTag tag) {
            }

            @Override
            public boolean isEmpty() {
                return true;
            }

            @Override
            public long getQuantity() {
                return 0;
            }

            @Override
            public void setQuantity(long quantity) {
            }

            @Override
            public void stackSlot(VRXMenu menu, VRXSlot slot, int button, ClickType type) {
            }

            @Override
            public boolean scrollSlot(VRXMenu menu, VRXSlot slot, double delta, boolean shiftDown) {
                return false;
            }

            @Override
            @OnlyIn(Dist.CLIENT)
            public void render(GuiGraphics graphics, int x, int y) {
            }

            @Override
            @OnlyIn(Dist.CLIENT)
            public void renderTooltip(VRXScreen screen, GuiGraphics graphics, int x, int y) {
            }

            @Override
            public String toString() {
                return "EMPTY";
            }

            @Override
            public boolean equals(Object obj) {
                return this == obj || obj instanceof VRXOne one && one.isEmpty();
            }
        };
    }
}
