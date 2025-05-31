package com.sakurafuld.hyperdaimc.compat.jei;

import com.sakurafuld.hyperdaimc.content.vrx.VRXJeiWrapper;
import com.sakurafuld.hyperdaimc.content.vrx.VRXOne;
import com.sakurafuld.hyperdaimc.content.vrx.VRXScreen;
import com.sakurafuld.hyperdaimc.content.vrx.VRXSlot;
import com.sakurafuld.hyperdaimc.network.PacketHandler;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXSetJeiGhost;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.helper.Deets.identifier;

@JeiPlugin
public class HyperJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = identifier(HYPERDAIMC, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(VRXScreen.class, new IGhostIngredientHandler<>() {
            @Override
            @SuppressWarnings("unchecked")
            public <I> List<Target<I>> getTargets(VRXScreen screen, I ingredient, boolean b) {
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
                                PacketHandler.INSTANCE.sendToServer(new ServerboundVRXSetJeiGhost(screen.getMenu().containerId, this.slot.index, stack));
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

                } else {
                    return Collections.emptyList();
                }
            }

            @Override
            public void onComplete() {
            }
        });
    }
}
