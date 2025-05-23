package com.sakurafuld.hyperdaimc.compat;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sakurafuld.hyperdaimc.api.CombinedGasHandler;
import com.sakurafuld.hyperdaimc.content.vrx.*;
import com.sakurafuld.hyperdaimc.helper.Renders;
import com.sakurafuld.hyperdaimc.network.PacketHandler;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXSetJeiGhost;
import mekanism.api.Action;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.client.gui.GuiUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class VRXOneGas extends VRXOne {
    public static VRXOne.Type TYPE;

    private GasStack stack = GasStack.EMPTY;

    public VRXOneGas(GasStack stack) {
        this();
        this.stack = stack;
    }
    public VRXOneGas() {
        super(TYPE);
    }
    public static void initialize() {
        TYPE = VRXOne.Type.register("gas", 20, VRXOneGas::new, VRXOneGas::convert, VRXOneGas::collect, VRXOneGas::check, VRXOneGas::cast);
    }

    @Override
    public @Nullable Object prepareInsert(CapabilityProvider<?> provider, @Nullable Direction face, List<VRXOne> previous) {
        IGasHandler handler = null;
        LazyOptional<IGasHandler> capability = provider.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, face);
        if (capability.isPresent()) {
            handler = capability.orElseThrow(IllegalStateException::new);
        }

        if (provider instanceof LivingEntity) {
            LazyOptional<IItemHandler> optional = provider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face);
            if (optional.isPresent()) {
                IItemHandler items = optional.orElseThrow(IllegalStateException::new);
                for (int index = 0; index < items.getSlots(); index++) {
                    LazyOptional<IGasHandler> gases = items.getStackInSlot(index).getCapability(Capabilities.GAS_HANDLER_CAPABILITY);
                    if (gases.isPresent()) {
                        if(handler == null) {
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
            LazyOptional<IGasHandler> capability = provider.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, face);

            if (capability.isPresent()) {
                handler = capability.orElseThrow(IllegalStateException::new);
            }

            if (provider instanceof LivingEntity) {
                LazyOptional<IItemHandler> optional = provider.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face);
                if (optional.isPresent()) {
                    IItemHandler items = optional.orElseThrow(IllegalStateException::new);
                    for (int index = 0; index < items.getSlots(); index++) {
                        LazyOptional<IGasHandler> gases = items.getStackInSlot(index).getCapability(Capabilities.GAS_HANDLER_CAPABILITY);
                        if (gases.isPresent()) {
                            if(handler == null) {
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
            } else if (slot.getOne() instanceof Item item && ItemHandlerHelper.canItemStacksStack(item.getItemStack(), menu.getCarried())) {
                item = new Item(menu.getCarried().copy());
                item.stackSlot(menu, slot, button, type);
            } else {
                slot.setOne(EMPTY);
            }
        } else if (type == ClickType.CLONE && !menu.getCarried().isEmpty()) {
            if(slot.isEmpty()) {
                slot.setOne(new Item(menu.getCarried()));
            } else if (!(slot.getOne() instanceof Item item && ItemHandlerHelper.canItemStacksStack(item.getItemStack(), menu.getCarried()))) {
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

        if(amount > Long.MAX_VALUE) {
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
    public void render(PoseStack poseStack, int x, int y) {
        TextureAtlasSprite sprite = MekanismRenderer.getChemicalTexture(this.stack.getType());
        MekanismRenderer.color(this.stack);
        GuiUtils.drawSprite(poseStack, x, y, 16, 16, 0, sprite);
        MekanismRenderer.resetColor();
        Renders.fluidAmount(poseStack, this.getQuantity(), x, y);
    }

    @Override
    public void renderTooltip(VRXScreen screen, PoseStack poseStack, int x, int y) {
        if (screen.getMenu().getCarried().isEmpty()) {

            List<Component> tooltip = Lists.newArrayList();
            tooltip.add(this.stack.getTextComponent());
            if (!this.stack.isEmpty()) {
                tooltip.add(new TextComponent(this.stack.getAmount() + "mb").withStyle(ChatFormatting.GRAY));
                ResourceLocation registryName = this.stack.getTypeRegistryName();
                if (screen.getMinecraft().options.advancedItemTooltips) {
                    tooltip.add(new TextComponent(registryName.toString()).withStyle(ChatFormatting.DARK_GRAY));
                }

                String modid = registryName.getNamespace();
                String modName = ModList.get().getModContainerById(modid)
                        .map(modContainer -> modContainer.getModInfo().getDisplayName())
                        .orElse(StringUtils.capitalize(modid));
                tooltip.add(new TextComponent(modName).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
            }

            screen.renderTooltip(poseStack, tooltip, Optional.empty(), x, y);
        } else {
            Component component = new TextComponent(this.getQuantity() + "mb").withStyle(ChatFormatting.GRAY);
            screen.renderTooltip(poseStack, Collections.singletonList(component), Optional.empty(), x, y);
        }
    }

    // GUI.
    public static VRXOne convert(ItemStack stack) {
        return stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY)
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

    public static List<VRXOne> collect(CapabilityProvider<?> provider, Direction face) {
        List<VRXOne> list = Lists.newArrayList();
        provider.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, face).ifPresent(handler -> {
            for (int index = 0; index < handler.getTanks(); index++) {
                GasStack stack = handler.getChemicalInTank(index);
                if (!stack.isEmpty()) {
                    list.add(new VRXOneGas(stack.copy()));
                }
            }
        });

        return list;
    }
    public static boolean check(CapabilityProvider<?> provider, Direction face) {
        return provider.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, face).isPresent();
    }
    public static VRXJeiWrapper<GasStack> cast(Object ingredient) {
        if(ingredient instanceof GasStack stack) {
            return new VRXJeiWrapper<>(stack.copy()) {
                @Override
                public void accept(int containerId, VRXSlot slot) {
                    VRXOne one = new VRXOneGas(this.ingredient);
                    slot.setOne(one);
                    PacketHandler.INSTANCE.sendToServer(new ServerboundVRXSetJeiGhost(containerId, ((Slot) slot).index, one));
                }
            };
        } else {
            return null;
        }
    }
}
