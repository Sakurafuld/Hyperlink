package com.sakurafuld.hyperdaimc.compat.jei;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXJeiWrapper;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXOne;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXScreen;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.VRXSlot;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXSetJeiGhost;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class VRXGhostIngredientHandler implements IGhostIngredientHandler<VRXScreen> {
    @Override
    @SuppressWarnings("unchecked")
    public <I> List<Target<I>> getTargets(VRXScreen screen, I ingredient, boolean b) {
        if (HyperCommonConfig.VRX_JEI.get()) {
            VRXJeiWrapper<I> wrapper = VRXOne.Type.cast(ingredient);
            if (wrapper != null) {
                class WrappedTarget implements Target<I> {
                    private final VRXSlot slot;

                    WrappedTarget(VRXSlot slot) {
                        this.slot = slot;
                    }

                    @Override
                    public Rect2i getArea() {
                        return new Rect2i(screen.getGuiLeft() + this.slot.x, screen.getGuiTop() + this.slot.y, 16, 16);
                    }

                    @Override
                    public void accept(I ingredient) {
                        wrapper.accept(screen.getMenu().containerId, this.slot);
                    }
                }
                class SimpleTarget implements Target<ItemStack> {
                    private final Slot slot;

                    SimpleTarget(Slot slot) {
                        this.slot = slot;
                    }

                    @Override
                    public Rect2i getArea() {
                        return new Rect2i(screen.getGuiLeft() + this.slot.x, screen.getGuiTop() + this.slot.y, 16, 16);
                    }

                    @Override
                    public void accept(ItemStack stack) {
                        if (this.slot.getItem().isEmpty()) {
                            this.slot.set(stack.copy());
                            HyperConnection.INSTANCE.sendToServer(new ServerboundVRXSetJeiGhost(screen.getMenu().containerId, this.slot.index, stack));
                        }
                    }
                }
                return screen.getMenu().slots.stream()
                        .map(slot -> {
                            if (slot instanceof VRXSlot vrxSlot) {
                                return new WrappedTarget(vrxSlot);
                            } else if (ingredient instanceof ItemStack) {
                                return (Target<I>) new SimpleTarget(slot);
                            } else {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void onComplete() {
    }
}
