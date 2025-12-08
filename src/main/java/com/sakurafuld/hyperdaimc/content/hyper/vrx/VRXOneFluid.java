package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.infrastructure.capability.CombinedFluidHandler;
import com.sakurafuld.hyperdaimc.infrastructure.capability.ContainedFluidHandlerItem;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXSetJeiFluid;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class VRXOneFluid extends VRXOne {
    private FluidStack stack = FluidStack.EMPTY;

    public VRXOneFluid(FluidStack stack) {
        this();
        this.stack = stack.copy();
    }

    public VRXOneFluid() {
        super(VRXType.fluid());
    }


    public static boolean check(CapabilityProvider<?> provider, Direction face) {
        return provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face).isPresent() || provider.getCapability(ForgeCapabilities.FLUID_HANDLER, face).isPresent();
    }

    public static VRXOne convert(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(handler -> {
                    for (int index = 0; index < handler.getTanks(); index++) {
                        FluidStack fluid = handler.getFluidInTank(index);
                        if (!fluid.isEmpty()) {
                            return fluid.copy();
                        }
                    }
                    return FluidStack.EMPTY;
                })
                .filter(fluid -> !fluid.isEmpty())
                .map(fluid -> (VRXOne) new VRXOneFluid(fluid))
                .orElse(EMPTY);
    }

    public static VRXJeiWrapper<FluidStack> cast(Object ingredient) {
        if (ingredient instanceof FluidStack stack)
            return new Wrapper(stack);
        else
            return VRXJeiWrapper.empty();
    }

    public static List<VRXOne> collect(CapabilityProvider<?> provider, Direction face) {
        List<VRXOne> list = Lists.newArrayList();
        provider.getCapability(ForgeCapabilities.FLUID_HANDLER, face).ifPresent(handler -> {
            for (int index = 0; index < handler.getTanks(); index++) {
                FluidStack stack = handler.getFluidInTank(index);
                if (!stack.isEmpty()) {
                    list.add(new VRXOneFluid(stack.copy()));
                }
            }
        });

        return list;
    }

    @Override
    public Object prepareInsert(CapabilityProvider<?> provider, Direction face, List<VRXOne> previous) {
        IFluidHandler handler = null;
        LazyOptional<IFluidHandler> capability = provider.getCapability(ForgeCapabilities.FLUID_HANDLER, face);
        if (capability.isPresent()) {
            handler = capability.orElseThrow(IllegalStateException::new);
        }

        LazyOptional<IItemHandler> optional = provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face);
        if (optional.isPresent()) {
            IItemHandler items = optional.orElseThrow(IllegalStateException::new);
            if (items instanceof IItemHandlerModifiable modifiable) {
                for (int index = 0; index < items.getSlots(); index++) {
                    LazyOptional<IFluidHandlerItem> fluids = items.getStackInSlot(index).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
                    if (fluids.isPresent()) {
                        ContainedFluidHandlerItem containedHandler = new ContainedFluidHandlerItem(fluids.orElseThrow(IllegalStateException::new), modifiable, index);
                        if (handler == null) {
                            handler = containedHandler;
                        } else {
                            handler = new CombinedFluidHandler(handler, containedHandler);
                        }
                    }
                }
            }
        }

        if (handler == null) {
            return null;
        } else {
            long amount = -previous.stream()
                    .filter(one -> one instanceof VRXOneFluid fluid && this.stack.equals(fluid.stack))
                    .mapToLong(VRXOne::getQuantity)
                    .sum();
            for (int index = 0; index < handler.getTanks(); index++) {
                FluidStack stack = handler.getFluidInTank(index);
                if (this.stack.equals(stack)) {
                    amount += stack.getAmount();
                }
            }
            return amount;
        }
    }

    @Override
    public void insert(CapabilityProvider<?> provider, @Nullable Direction face, Object prepared) {
        if (prepared instanceof Long amount && amount < this.getQuantity()) {
            IFluidHandler handler = null;
            LazyOptional<IFluidHandler> capability = provider.getCapability(ForgeCapabilities.FLUID_HANDLER, face);

            if (capability.isPresent()) {
                handler = capability.orElseThrow(IllegalStateException::new);
            }

            LazyOptional<IItemHandler> optional = provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face);
            if (optional.isPresent()) {
                IItemHandler items = optional.orElseThrow(IllegalStateException::new);
                if (items instanceof IItemHandlerModifiable modifiable) {
                    for (int index = 0; index < items.getSlots(); index++) {
                        LazyOptional<IFluidHandlerItem> fluids = items.getStackInSlot(index).getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
                        if (fluids.isPresent()) {
                            ContainedFluidHandlerItem containedHandler = new ContainedFluidHandlerItem(fluids.orElseThrow(IllegalStateException::new), modifiable, index);
                            if (handler == null) {
                                handler = containedHandler;
                            } else {
                                handler = new CombinedFluidHandler(handler, containedHandler);
                            }
                        }
                    }
                }
            }

            if (handler != null) {
                FluidStack stack = this.stack.copy();
                if (amount > 0)
                    stack.setAmount((int) (this.getQuantity() - amount));
                handler.fill(stack, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("Fluid", this.stack.writeToNBT(new CompoundTag()));
        return tag;
    }

    public void load(CompoundTag tag) {
        this.stack = FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid"));
    }

    @Override
    public boolean isEmpty() {
        return this.stack.isEmpty();
    }

    @Override
    public long getQuantity() {
        return this.stack.getAmount();
    }

    @Override
    public void setQuantity(long quantity) {
        this.stack.setAmount((int) Mth.clamp(quantity, 0, Integer.MAX_VALUE));
    }

    @Override
    public void stackSlot(VRXMenu menu, VRXSlot slot, int button, ClickType type) {
        FluidStack stack = this.stack.copy();
        if (button == 0 || button == 1) {
            if (menu.getCarried().isEmpty()) {
                if (button == 0) {
                    slot.setOne(VRXOne.EMPTY);
                } else {
                    slot.getOne().setQuantity((this.getQuantity() + 1) / 2);
                }
            } else if (slot.isEmpty()) {
                if (button == 0) {
                    if (type == ClickType.QUICK_MOVE) {
                        stack.setAmount((int) Mth.clamp(this.getQuantity() * 10L, 1L, Integer.MAX_VALUE));
                    }
                    slot.setOne(new VRXOneFluid(stack));
                } else {
                    if (type == ClickType.QUICK_MOVE) {
                        stack.setAmount((int) Math.max(0, this.getQuantity() / 10));
                    } else {
                        stack.setAmount((int) Math.max(0, this.getQuantity() / 100));
                    }
                    slot.setOne(new VRXOneFluid(stack));
                }
            } else if (slot.getOne() instanceof VRXOneFluid fluid) {
                if (stack.equals(fluid.stack)) {
                    if (button == 0) {
                        if (type == ClickType.QUICK_MOVE) {
                            stack.setAmount((int) Mth.clamp(this.getQuantity() * 10L, 1L, Integer.MAX_VALUE));
                        }
                        slot.grow(stack.getAmount());
                    } else {
                        if (type == ClickType.QUICK_MOVE) {
                            slot.grow(Math.max(0, this.getQuantity() / 10));
                        } else {
                            slot.grow(Math.max(0, this.getQuantity() / 100));
                        }
                    }
                } else {
                    slot.setOne(EMPTY);
                }
            } else if (slot.getOne() instanceof VRXOneItem item && ItemHandlerHelper.canItemStacksStack(item.getItemStack(), menu.getCarried())) {
                item = new VRXOneItem(menu.getCarried().copy());
                item.stackSlot(menu, slot, button, type);
            } else {
                slot.setOne(EMPTY);
            }
        } else if (type == ClickType.CLONE) {
            if (menu.getCarried().isEmpty()) {
                ItemStack bucket = new ItemStack(this.stack.getFluid().getBucket());
                bucket.setCount(bucket.getMaxStackSize());
                menu.setCarried(bucket);
            } else if (slot.isEmpty()) {
                slot.setOne(new VRXOneItem(menu.getCarried()));
            } else if (!(slot.getOne() instanceof VRXOneItem item && ItemHandlerHelper.canItemStacksStack(item.getItemStack(), menu.getCarried()))) {
                slot.setOne(EMPTY);
            }
        }
    }

    @Override
    public boolean scrollSlot(VRXMenu menu, VRXSlot slot, double delta, boolean shiftDown) {
        long amount;
        if (menu.getCarried().isEmpty()) {
            amount = shiftDown ? 1000 : 1;
        } else {
            amount = shiftDown ? this.getQuantity() * 10 : this.getQuantity();
        }

        if (delta < 0) {
            amount = -amount;
        }

        if (slot.isEmpty()) {
            this.setQuantity(amount);
            slot.setOne(this);
        } else {
            slot.grow(amount);
        }
        if (slot.isEmpty()) {
            slot.setOne(EMPTY);
        }

        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        Renders.fluidStack(graphics, this.stack, x, y);
        Renders.fluidAmount(graphics, this.getQuantity(), x, y);
    }

    @Override
    public void renderTooltip(VRXScreen screen, GuiGraphics graphics, int x, int y) {
        if (screen.getMenu().getCarried().isEmpty()) {

            List<Component> tooltip = Lists.newArrayList();
            tooltip.add(this.stack.getDisplayName());
            if (!this.stack.isEmpty()) {
                tooltip.add(Component.literal(this.stack.getAmount() + "mb").withStyle(ChatFormatting.GRAY));
                ResourceLocation registryName = ForgeRegistries.FLUIDS.getKey(this.stack.getFluid());
                if (registryName != null) {
                    if (screen.getMinecraft().options.advancedItemTooltips) {
                        tooltip.add(Component.literal(registryName.toString()).withStyle(ChatFormatting.DARK_GRAY));
                    }

                    String modid = registryName.getNamespace();
                    String modName = ModList.get().getModContainerById(modid)
                            .map(modContainer -> modContainer.getModInfo().getDisplayName())
                            .orElse(StringUtils.capitalize(modid));
                    tooltip.add(Component.literal(modName).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                }
            }

            graphics.renderTooltip(screen.getMinecraft().font, tooltip, Optional.empty(), x, y);
        } else {
            Component component = Component.literal(this.getQuantity() + "mb").withStyle(ChatFormatting.GRAY);
            graphics.renderTooltip(screen.getMinecraft().font, Collections.singletonList(component), Optional.empty(), x, y);
        }
    }

    @Override
    public String toString() {
        return "[" + ForgeRegistries.FLUIDS.getKey(this.stack.getFluid()) + "=" + this.getQuantity() + "]";
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        VRXOneFluid that = (VRXOneFluid) object;
        return stack.isFluidStackIdentical(that.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stack);
    }

    public static class Wrapper extends VRXJeiWrapper<FluidStack> {
        protected Wrapper(FluidStack ingredient) {
            super(ingredient);
        }

        @Override
        public VRXType type() {
            return VRXType.fluid();
        }

        @Override
        public void accept(int containerId, VRXSlot slot) {
            slot.setOne(new VRXOneFluid(this.ingredient));
            HyperConnection.INSTANCE.sendToServer(new ServerboundVRXSetJeiFluid(containerId, ((Slot) slot).index, this.ingredient));
        }

        public void accept(VRXMenu menu, int index) {
            Slot slot = menu.getSlot(index);
            if (slot instanceof VRXSlot vrxSlot)
                vrxSlot.setOne(new VRXOneFluid(this.ingredient));
        }
    }
}
