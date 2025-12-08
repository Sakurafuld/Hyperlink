package com.sakurafuld.hyperdaimc.addon.mekanism;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.*;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.infrastructure.capability.CombinedGasHandler;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.client.gui.GuiUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class VRXOneGas extends VRXOne {
    private GasStack stack = GasStack.EMPTY;

    public VRXOneGas(GasStack stack) {
        this();
        this.stack = stack;
    }

    public VRXOneGas() {
        super(HyperMekanism.vrxGas());
    }


    public static boolean check(CapabilityProvider<?> provider, Direction face) {
        return provider.getCapability(Capabilities.GAS_HANDLER, face).isPresent();
    }

    public static VRXOne convert(ItemStack stack) {
        return stack.getCapability(Capabilities.GAS_HANDLER)
                .map(handler -> {
                    for (int index = 0; index < handler.getTanks(); index++) {
                        GasStack gas = handler.getChemicalInTank(index);
                        if (!gas.isEmpty()) {
                            return gas.copy();
                        }
                    }
                    return GasStack.EMPTY;
                })
                .filter(gas -> !gas.isEmpty())
                .map(gas -> (VRXOne) new VRXOneGas(gas))
                .orElse(EMPTY);
    }

    public static VRXJeiWrapper<GasStack> cast(Object ingredient) {
        if (ingredient instanceof GasStack stack)
            return new Wrapper(stack);
        else
            return VRXJeiWrapper.empty();
    }

    public static List<VRXOne> collect(CapabilityProvider<?> provider, Direction face) {
        List<VRXOne> list = Lists.newArrayList();
        provider.getCapability(Capabilities.GAS_HANDLER, face).ifPresent(handler -> {
            for (int index = 0; index < handler.getTanks(); index++) {
                GasStack stack = handler.getChemicalInTank(index);
                if (!stack.isEmpty()) {
                    list.add(new VRXOneGas(stack.copy()));
                }
            }
        });

        return list;
    }

    @Override
    public @Nullable Object prepareInsert(CapabilityProvider<?> provider, @Nullable Direction face, List<VRXOne> previous) {
        IGasHandler handler = null;
        LazyOptional<IGasHandler> capability = provider.getCapability(Capabilities.GAS_HANDLER, face);
        if (capability.isPresent()) {
            handler = capability.orElseThrow(IllegalStateException::new);
        }

        if (provider instanceof LivingEntity) {
            LazyOptional<IItemHandler> optional = provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face);
            if (optional.isPresent()) {
                IItemHandler items = optional.orElseThrow(IllegalStateException::new);
                for (int index = 0; index < items.getSlots(); index++) {
                    LazyOptional<IGasHandler> gases = items.getStackInSlot(index).getCapability(Capabilities.GAS_HANDLER);
                    if (gases.isPresent()) {
                        if (handler == null) {
                            handler = gases.orElseThrow(IllegalStateException::new);
                        } else {
                            handler = new CombinedGasHandler(handler, gases.orElseThrow(IllegalStateException::new));
                        }
                    }
                }
            }
        }

        if (handler == null) {
            return null;
        } else {
            long amount = -previous.stream()
                    .filter(one -> one instanceof VRXOneGas gas && this.stack.isTypeEqual(gas.stack))
                    .mapToLong(VRXOne::getQuantity)
                    .sum();
            for (int index = 0; index < handler.getTanks(); index++) {
                GasStack stack = handler.getChemicalInTank(index);
                if (this.stack.isTypeEqual(stack)) {
                    amount += stack.getAmount();
                }
            }
            return amount;
        }
    }

    @Override
    public void insert(CapabilityProvider<?> provider, @Nullable Direction face, Object prepared) {
        if (prepared instanceof Long amount && amount < this.getQuantity()) {
            IGasHandler handler = null;
            LazyOptional<IGasHandler> capability = provider.getCapability(Capabilities.GAS_HANDLER, face);

            if (capability.isPresent()) {
                handler = capability.orElseThrow(IllegalStateException::new);
            }

            if (provider instanceof LivingEntity) {
                LazyOptional<IItemHandler> optional = provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face);
                if (optional.isPresent()) {
                    IItemHandler items = optional.orElseThrow(IllegalStateException::new);
                    for (int index = 0; index < items.getSlots(); index++) {
                        LazyOptional<IGasHandler> gases = items.getStackInSlot(index).getCapability(Capabilities.GAS_HANDLER);
                        if (gases.isPresent()) {
                            if (handler == null) {
                                handler = gases.orElseThrow(IllegalStateException::new);
                            } else {
                                handler = new CombinedGasHandler(handler, gases.orElseThrow(IllegalStateException::new));
                            }
                        }
                    }
                }
            }

            if (handler != null) {
                GasStack stack = this.stack.copy();
                if (amount > 0) {
                    stack.setAmount((int) (this.getQuantity() - amount));
                }
                ChemicalUtils.insert(stack, Action.EXECUTE, handler.getEmptyStack(), handler::getTanks, handler::getChemicalInTank, handler::insertChemical);
            }
        }
    }

    @Override
    protected CompoundTag save() {
        return this.stack.write(new CompoundTag());
    }

    @Override
    public void load(CompoundTag tag) {
        this.stack = GasStack.readFromNBT(tag);
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
        this.stack.setAmount(quantity);
    }

    @Override
    public void stackSlot(VRXMenu menu, VRXSlot slot, int button, ClickType type) {
        GasStack stack = this.stack.copy();
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
                        stack.setAmount((int) Mth.clamp(this.getQuantity() * 10d, 1d, Long.MAX_VALUE));
                    }
                    slot.setOne(new VRXOneGas(stack));
                } else {
                    if (type == ClickType.QUICK_MOVE) {
                        stack.setAmount((int) Math.max(0, this.getQuantity() / 10));
                    } else {
                        stack.setAmount((int) Math.max(0, this.getQuantity() / 100));
                    }
                    slot.setOne(new VRXOneGas(stack));
                }
            } else if (slot.getOne() instanceof VRXOneGas gas) {
                if (stack.isTypeEqual(gas.stack)) {
                    if (button == 0) {
                        if (type == ClickType.QUICK_MOVE) {
                            stack.setAmount((int) Mth.clamp(this.getQuantity() * 10d, 1d, Long.MAX_VALUE));
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
        } else if (type == ClickType.CLONE && !menu.getCarried().isEmpty()) {
            if (slot.isEmpty()) {
                slot.setOne(new VRXOneItem(menu.getCarried()));
            } else if (!(slot.getOne() instanceof VRXOneItem item && ItemHandlerHelper.canItemStacksStack(item.getItemStack(), menu.getCarried()))) {
                slot.setOne(EMPTY);
            }
        }
    }

    @Override
    public boolean scrollSlot(VRXMenu menu, VRXSlot slot, double delta, boolean shiftDown) {
        double amount;
        if (menu.getCarried().isEmpty()) {
            amount = shiftDown ? 1000 : 1;
        } else {
            amount = shiftDown ? this.getQuantity() * 10d : this.getQuantity();
        }

        if (delta < 0) {
            amount = -amount;
        }

        if (amount > Long.MAX_VALUE) {
            amount = Long.MAX_VALUE;
        }

        if (slot.isEmpty()) {
            this.setQuantity(Math.round(amount));
            slot.setOne(this);
        } else {
            slot.grow(Math.round(amount));
        }
        if (slot.isEmpty()) {
            slot.setOne(EMPTY);
        }

        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y) {
        MekanismRenderer.color(graphics, this.stack);
        GuiUtils.drawTiledSprite(graphics, x, y, 16, 16, 16, MekanismRenderer.getSprite(this.stack.getType().getIcon()),
                16, 16, 0, GuiUtils.TilingDirection.UP_RIGHT);
        MekanismRenderer.resetColor(graphics);
        Renders.fluidAmount(graphics, this.getQuantity(), x, y);
    }

    @Override
    public void renderTooltip(VRXScreen screen, GuiGraphics graphics, int x, int y) {
        if (screen.getMenu().getCarried().isEmpty()) {

            List<Component> tooltip = Lists.newArrayList();
            tooltip.add(this.stack.getTextComponent());
            if (!this.stack.isEmpty()) {
                tooltip.add(Component.literal(this.stack.getAmount() + "mb").withStyle(ChatFormatting.GRAY));
                ResourceLocation registryName = this.stack.getTypeRegistryName();
                if (screen.getMinecraft().options.advancedItemTooltips) {
                    tooltip.add(Component.literal(registryName.toString()).withStyle(ChatFormatting.DARK_GRAY));
                }

                String modid = registryName.getNamespace();
                String modName = ModList.get().getModContainerById(modid)
                        .map(modContainer -> modContainer.getModInfo().getDisplayName())
                        .orElse(StringUtils.capitalize(modid));
                tooltip.add(Component.literal(modName).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
            }

            graphics.renderTooltip(screen.getMinecraft().font, tooltip, Optional.empty(), x, y);
        } else {
            Component component = Component.literal(this.getQuantity() + "mb").withStyle(ChatFormatting.GRAY);
            graphics.renderTooltip(screen.getMinecraft().font, Collections.singletonList(component), Optional.empty(), x, y);
        }
    }

    @Override
    public String toString() {
        return "[" + this.stack.getType() + "=" + this.stack.getAmount() + "]";
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        VRXOneGas vrxOneGas = (VRXOneGas) object;
        return Objects.equals(stack, vrxOneGas.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stack);
    }

    public static class Wrapper extends VRXJeiWrapper<GasStack> {
        protected Wrapper(GasStack ingredient) {
            super(ingredient);
        }

        @Override
        public VRXType type() {
            return HyperMekanism.vrxGas();
        }

        @Override
        public void accept(int containerId, VRXSlot slot) {
            slot.setOne(new VRXOneGas(this.ingredient));
            HyperConnection.INSTANCE.sendToServer(new ServerboundVRXSetJeiGas(containerId, ((Slot) slot).index, this.ingredient));
        }

        public void accept(VRXMenu menu, int index) {
            Slot slot = menu.getSlot(index);
            if (slot instanceof VRXSlot vrxSlot)
                vrxSlot.setOne(new VRXOneGas(this.ingredient));
        }
    }
}
