package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.api.content.CombinedFluidHandler;
import com.sakurafuld.hyperdaimc.api.content.ContainedFluidHandlerItem;
import com.sakurafuld.hyperdaimc.helper.Renders;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXSetJeiGhost;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
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

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.*;

public abstract class VRXOne {
    public static final VRXOne EMPTY;

    public final Type type;

    public VRXOne(Type type) {
        this.type = type;
    }

    @Nullable
    public abstract Object prepareInsert(CapabilityProvider<?> provider, @Nullable Direction face, List<VRXOne> previous);

    public abstract void insert(CapabilityProvider<?> provider, @Nullable Direction face, Object prepared);

    public final CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", this.type.getName());
        tag.put("Data", this.save());
        return tag;
    }

    protected abstract CompoundTag save();

    public abstract void load(CompoundTag tag);

    // GUI.
    public abstract boolean isEmpty();

    public abstract long getQuantity();

    public abstract void setQuantity(long quantity);

    public abstract void stackSlot(VRXMenu menu, VRXSlot slot, int button, ClickType type);

    public abstract boolean scrollSlot(VRXMenu menu, VRXSlot slot, double delta, boolean shiftDown);

    @OnlyIn(Dist.CLIENT)
    public abstract void render(GuiGraphics graphics, int x, int y);

    @OnlyIn(Dist.CLIENT)
    public abstract void renderTooltip(VRXScreen screen, GuiGraphics graphics, int x, int y);
    //.

    public static class Type {

        public static List<Type> VALUES = null;
        public static Map<String, Type> MAP = null;

        public static final Type EMPTY = register("empty", Integer.MAX_VALUE, () -> VRXOne.EMPTY, stack -> VRXOne.EMPTY, (provider, face) -> Collections.emptyList(), (provider, face) -> false);
        public static final Type ITEM = register("item", 0, Item::new, Item::new, Item::collect, Item::check, Item::cast);
        public static final Type FLUID = register("fluid", 10, Fluid::new, Fluid::convert, Fluid::collect, Fluid::check, Fluid::cast);
        public static final Type ENERGY = register("energy", -10, Energy::new, Energy::convert, Energy::collect, Energy::check);

        private final String name;
        private final int priority;
        private final Supplier<VRXOne> creator;
        private final Function<ItemStack, VRXOne> converter;
        private final BiFunction<CapabilityProvider<?>, Direction, List<VRXOne>> collector;
        private final BiFunction<CapabilityProvider<?>, Direction, Boolean> checker;
        private final @Nullable Function<Object, VRXJeiWrapper<?>> caster;

        private Type(String name, int priority,
                     Supplier<VRXOne> creator,
                     Function<ItemStack, VRXOne> converter,
                     BiFunction<CapabilityProvider<?>, Direction, List<VRXOne>> collector,
                     BiFunction<CapabilityProvider<?>, Direction, Boolean> checker,
                     @Nullable Function<Object, VRXJeiWrapper<?>> caster) {

            this.name = name;
            this.priority = priority;
            this.creator = creator;
            this.converter = converter;
            this.collector = collector;
            this.checker = checker;
            this.caster = caster;
        }

        public static Type register(String name, int priority,
                                    Supplier<VRXOne> creator,
                                    Function<ItemStack, VRXOne> converter,
                                    BiFunction<CapabilityProvider<?>, Direction, List<VRXOne>> collector,
                                    BiFunction<CapabilityProvider<?>, Direction, Boolean> checker,
                                    @Nullable Function<Object, VRXJeiWrapper<?>> caster) {

            if (VALUES == null) {
                VALUES = Lists.newArrayList();
            }
            if (MAP == null) {
                MAP = Maps.newHashMap();
            }

            Type type = new Type(name, priority, creator, converter, collector, checker, caster);

            VALUES.add(type);
            VALUES.sort(Comparator.comparingInt(t -> t.priority));
            MAP.put(name, type);

            LOG.debug("registerVRXType:{}", name);
            return type;
        }

        public static Type register(String name, int priority,
                                    Supplier<VRXOne> creator,
                                    Function<ItemStack, VRXOne> converter,
                                    BiFunction<CapabilityProvider<?>, Direction, List<VRXOne>> collector,
                                    BiFunction<CapabilityProvider<?>, Direction, Boolean> checker) {

            return register(name, priority, creator, converter, collector, checker, null);
        }

        public static Type of(String name) {
            return MAP.getOrDefault(name, EMPTY);
        }

        public static VRXOne convert(ItemStack stack) {
            if (!(HyperCommonConfig.VRX_SEAL_HYPERLINK.get() && ForgeRegistries.ITEMS.getKey(stack.getItem()).getNamespace().equals(HYPERDAIMC))) {
                if (!stack.isEmpty()) {
                    for (Type type : VALUES) {
                        if (type != EMPTY && type != ITEM) {
                            VRXOne one = type.converter.apply(stack);
                            if (!one.isEmpty()) {
                                return one;
                            }
                        }
                    }

                    return ITEM.converter.apply(stack);
                }
            }

            return VRXOne.EMPTY;
        }

        public static List<VRXOne> collect(CapabilityProvider<?> provider, Direction face) {
            List<VRXOne> list = Lists.newArrayList();
            for (Type type : VALUES) {
                list.addAll(type.collector.apply(provider, face));
            }

            return list;
        }

        public static boolean check(CapabilityProvider<?> provider, Direction face) {
            for (Type type : VALUES) {
                if (type.checker.apply(provider, face)) {
                    return true;
                }
            }

            return false;
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public static <I> VRXJeiWrapper<I> cast(I ingredient) {
            for (Type type : VALUES) {
                if (type.caster != null) {
                    VRXJeiWrapper<I> wrapper = (VRXJeiWrapper<I>) type.caster.apply(ingredient);
                    if (wrapper != null) {
                        return wrapper;
                    }
                }
            }

            return null;
        }

        public String getName() {
            return this.name;
        }

        public int getPriority() {
            return this.priority;
        }

        public VRXOne load(CompoundTag tag) {
            VRXOne one = this.creator.get();
            one.load(tag);
            return one;
        }
    }

    public static class Item extends VRXOne {
        private ItemStack stack = ItemStack.EMPTY;
        private int count = 0;

        public Item(ItemStack object) {
            this();
            this.stack = object.copy();
            this.count = this.stack.getCount();
        }

        private Item() {
            super(Type.ITEM);
        }

        @Override
        public Object prepareInsert(CapabilityProvider<?> provider, Direction face, List<VRXOne> previous) {
            LazyOptional<IItemHandler> capability = provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face);
            if (capability.isPresent()) {
                IItemHandler handler = capability.orElseThrow(IllegalStateException::new);
                long count = -previous.stream()
                        .filter(one -> one instanceof Item item && this.stack.is(item.stack.getItem()))
                        .mapToLong(VRXOne::getQuantity)
                        .sum();
                for (int index = 0; index < handler.getSlots(); index++) {
                    ItemStack stack = handler.getStackInSlot(index);
                    if (!stack.isEmpty() && this.stack.is(stack.getItem())) {
                        count += stack.getCount();
                    }
                }

                return count;
            } else {
                return null;
            }
        }

        @Override
        public void insert(CapabilityProvider<?> provider, @Nullable Direction face, Object prepared) {
            if (prepared instanceof Long count && count < this.getQuantity()) {
                provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face).ifPresent(handler -> {
                    ItemStack stack = this.stack.copy();
                    if (count > 0) {
                        stack.setCount((int) (this.getQuantity() - count));
                    }
                    ItemHandlerHelper.insertItemStacked(handler, stack, false);
                });
            }
        }

        @Override
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.put("Item", this.stack.serializeNBT());
            tag.putInt("Count", this.count);
            return tag;
        }

        public void load(CompoundTag tag) {
            this.stack = ItemStack.of(tag.getCompound("Item"));
            this.count = tag.getInt("Count");
            this.setQuantity(this.count);
        }

        // GUI.
        public static List<VRXOne> collect(CapabilityProvider<?> provider, Direction face) {
            List<VRXOne> list = Lists.newArrayList();
            provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face).ifPresent(handler -> {
                for (int index = 0; index < handler.getSlots(); index++) {
                    ItemStack stack = handler.getStackInSlot(index);
                    if (!stack.isEmpty()) {
                        list.add(new Item(stack.copy()));
                    }
                }
            });

            return list;
        }

        public static boolean check(CapabilityProvider<?> provider, Direction face) {
            return provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face).isPresent();
        }

        public static VRXJeiWrapper<ItemStack> cast(Object ingredient) {
            if (ingredient instanceof ItemStack stack && !(HyperCommonConfig.VRX_SEAL_HYPERLINK.get() && ForgeRegistries.ITEMS.getKey(stack.getItem()).getNamespace().equals(HYPERDAIMC))) {
                return new VRXJeiWrapper<>(stack.copy()) {
                    @Override
                    public void accept(int containerId, VRXSlot slot) {
                        VRXOne one = new Item(this.ingredient);
                        slot.setOne(one);
                        HyperConnection.INSTANCE.sendToServer(new ServerboundVRXSetJeiGhost(containerId, ((Slot) slot).index, one));
                    }
                };
            } else {
                return null;
            }
        }

        @Override
        public boolean isEmpty() {
            return this.stack.isEmpty() || this.count <= 0;
        }

        @Override
        public long getQuantity() {
            return this.count;
        }

        @Override
        public void setQuantity(long quantity) {
            this.count = (int) Mth.clamp(quantity, 0, Integer.MAX_VALUE);
            this.stack.setCount(this.count);
        }

        @Override
        public void stackSlot(VRXMenu menu, VRXSlot slot, int button, ClickType type) {
            ItemStack stack = this.stack.copy();
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
                            stack.setCount(64);
                        }
                        slot.setOne(new Item(stack));
                    } else {
                        stack.setCount(1);
                        slot.setOne(new Item(stack));
                    }
                } else if (slot.getOne() instanceof Item item) {
                    if (ItemHandlerHelper.canItemStacksStack(stack, item.stack)) {
                        if (button == 0) {
                            if (type == ClickType.QUICK_MOVE) {
                                stack.setCount(64);
                            }
                            slot.grow(stack.getCount());
                        } else {
                            slot.grow(1);
                        }
                    } else {
                        slot.setOne(EMPTY);
                    }
                } else {
                    slot.setOne(EMPTY);
                }
            } else if (type == ClickType.CLONE && menu.getCarried().isEmpty()) {
                stack.setCount(stack.getMaxStackSize());
                menu.setCarried(stack);
            }
        }

        @Override
        public boolean scrollSlot(VRXMenu menu, VRXSlot slot, double delta, boolean shiftDown) {
            long count;

            if (menu.getCarried().isEmpty()) {
                count = shiftDown ? 64 : 1;
            } else {
                count = shiftDown ? this.getQuantity() * 10 : this.getQuantity();
            }

            if (delta < 0) {
                count = -count;
            }

            if (slot.isEmpty()) {
                this.setQuantity(count);
                slot.setOne(this);
            } else {
                slot.grow(count);
            }
            if (slot.isEmpty()) {
                slot.setOne(EMPTY);
            }

            return true;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void render(GuiGraphics graphics, int x, int y) {
            Minecraft mc = Minecraft.getInstance();
            graphics.renderItem(this.stack, x, y, 0);
            graphics.renderItemDecorations(mc.font, this.stack, x, y, "");
            if (this.getQuantity() != 1) {
                graphics.pose().translate(0, 0, 200);
                Renders.slotScaledString(graphics.pose(), String.valueOf(this.getQuantity()), x, y);
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void renderTooltip(VRXScreen screen, GuiGraphics graphics, int x, int y) {
            if (screen.getMenu().getCarried().isEmpty()) {
                graphics.renderTooltip(screen.getMinecraft().font, stack, x, y);
            }
        }

        public ItemStack getItemStack() {
            return this.stack.copy();
        }

        @Override
        public String toString() {
            return this.stack.toString();
        }
    }

    public static class Fluid extends VRXOne {
        private FluidStack stack = FluidStack.EMPTY;

        public Fluid(FluidStack object) {
            this();
            this.stack = object.copy();
        }

        private Fluid() {
            super(Type.FLUID);
        }

        @Override
        public Object prepareInsert(CapabilityProvider<?> provider, Direction face, List<VRXOne> previous) {
            IFluidHandler handler = null;
            LazyOptional<IFluidHandler> capability = provider.getCapability(ForgeCapabilities.FLUID_HANDLER, face);
            if (capability.isPresent()) {
                handler = capability.orElseThrow(IllegalStateException::new);
            }

            if (provider instanceof LivingEntity) {
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
            }

            if (handler == null) {
                return null;
            } else {
                long amount = -previous.stream()
                        .filter(one -> one instanceof Fluid fluid && this.stack.equals(fluid.stack))
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

                if (provider instanceof LivingEntity) {
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
                }

                if (handler != null) {
                    FluidStack stack = this.stack.copy();
                    if (amount > 0) {
                        stack.setAmount((int) (this.getQuantity() - amount));
                    }
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

        // GUI.
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
                    .map(fluid -> (VRXOne) new Fluid(fluid))
                    .orElse(EMPTY);
        }

        public static List<VRXOne> collect(CapabilityProvider<?> provider, Direction face) {
            List<VRXOne> list = Lists.newArrayList();
            provider.getCapability(ForgeCapabilities.FLUID_HANDLER, face).ifPresent(handler -> {
                for (int index = 0; index < handler.getTanks(); index++) {
                    FluidStack stack = handler.getFluidInTank(index);
                    if (!stack.isEmpty()) {
                        list.add(new Fluid(stack.copy()));
                    }
                }
            });

            return list;
        }

        public static boolean check(CapabilityProvider<?> provider, Direction face) {
            return provider.getCapability(ForgeCapabilities.FLUID_HANDLER, face).isPresent();
        }

        public static VRXJeiWrapper<FluidStack> cast(Object ingredient) {
            if (ingredient instanceof FluidStack stack && !(HyperCommonConfig.VRX_SEAL_HYPERLINK.get() && ForgeRegistries.FLUIDS.getKey(stack.getFluid()).getNamespace().equals(HYPERDAIMC))) {
                return new VRXJeiWrapper<>(stack.copy()) {
                    @Override
                    public void accept(int containerId, VRXSlot slot) {
                        VRXOne one = new Fluid(this.ingredient);
                        slot.setOne(one);
                        HyperConnection.INSTANCE.sendToServer(new ServerboundVRXSetJeiGhost(containerId, ((Slot) slot).index, one));
                    }
                };
            } else {
                return null;
            }
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
                        slot.setOne(new Fluid(stack));
                    } else {
                        if (type == ClickType.QUICK_MOVE) {
                            stack.setAmount((int) Math.max(0, this.getQuantity() / 10));
                        } else {
                            stack.setAmount((int) Math.max(0, this.getQuantity() / 100));
                        }
                        slot.setOne(new Fluid(stack));
                    }
                } else if (slot.getOne() instanceof Fluid fluid) {
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
                } else if (slot.getOne() instanceof Item item && ItemHandlerHelper.canItemStacksStack(item.stack, menu.getCarried())) {
                    item = new Item(menu.getCarried().copy());
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
                    slot.setOne(new Item(menu.getCarried()));
                } else if (!(slot.getOne() instanceof Item item && ItemHandlerHelper.canItemStacksStack(item.stack, menu.getCarried()))) {
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
            Renders.fluidStack(graphics.pose(), this.stack, x, y);
            Renders.fluidAmount(graphics.pose(), this.getQuantity(), x, y);
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
            return this.getQuantity() + " " + this.stack.getDisplayName().getString();
        }
    }

    public static class Energy extends VRXOne {
        private static final ResourceLocation TEXTURE = identifier("textures/gui/energy_slot.png");
        private int quantity = 0;
        private boolean collected = false;

        public Energy(int quantity, boolean collected) {
            this();
            this.quantity = quantity;
            this.collected = collected;
        }

        private Energy() {
            super(Type.ENERGY);
        }

        @Override
        public Object prepareInsert(CapabilityProvider<?> provider, Direction face, List<VRXOne> previous) {
            LazyOptional<IEnergyStorage> capability = provider.getCapability(ForgeCapabilities.ENERGY, face);
            if (capability.isPresent() && capability.orElseThrow(IllegalStateException::new).canReceive()) {
                return "Yeah";
            } else {
                LazyOptional<IItemHandler> optional = provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face);
                if (optional.isPresent()) {
                    IItemHandler items = optional.orElseThrow(IllegalStateException::new);
                    for (int index = 0; index < items.getSlots(); index++) {
                        capability = items.getStackInSlot(index).getCapability(ForgeCapabilities.ENERGY);
                        if (capability.isPresent() && capability.orElseThrow(IllegalStateException::new).canReceive()) {

                            return "Okay";
                        }
                    }
                }

                return null;
            }
        }

        @Override
        public void insert(CapabilityProvider<?> provider, @Nullable Direction face, Object prepared) {
            provider.getCapability(ForgeCapabilities.ENERGY, face).ifPresent(handler -> {
                int count = 0;
                while (count <= 1024 && handler.canReceive() && handler.receiveEnergy(this.quantity, true) > 0) {
                    handler.receiveEnergy(Integer.MAX_VALUE, false);
                    count++;
                }
            });

            provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face).ifPresent(items -> {
                for (int index = 0; index < items.getSlots(); index++) {
                    items.getStackInSlot(index).getCapability(ForgeCapabilities.ENERGY).ifPresent(handler -> {
                        int count = 0;
                        while (count <= 1024 && handler.canReceive() && handler.receiveEnergy(this.quantity, true) > 0) {
                            handler.receiveEnergy(Integer.MAX_VALUE, false);
                            count++;
                        }
                    });
                }
            });
        }

        @Override
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Quantity", this.quantity);
            tag.putBoolean("Collected", this.collected);
            return tag;
        }

        public void load(CompoundTag tag) {
            this.quantity = tag.getInt("Quantity");
            this.collected = tag.getBoolean("Collected");
        }

        // GUI.
        public static VRXOne convert(ItemStack stack) {
            return stack.getCapability(ForgeCapabilities.ENERGY)
                    .filter(handler -> handler.getEnergyStored() > 0)
                    .map(handler -> (VRXOne) new Energy(Integer.MAX_VALUE, false))
                    .orElse(EMPTY);
        }

        public static List<VRXOne> collect(CapabilityProvider<?> provider, Direction face) {
            List<VRXOne> list = Lists.newArrayList();
            provider.getCapability(ForgeCapabilities.ENERGY, face).ifPresent(handler -> {
                if (handler.getEnergyStored() > 0) {
                    list.add(new Energy(handler.getEnergyStored(), true));
                }
            });

            return list;
        }

        public static boolean check(CapabilityProvider<?> provider, Direction face) {
            return provider.getCapability(ForgeCapabilities.ENERGY, face).isPresent();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public long getQuantity() {
            return this.quantity;
        }

        @Override
        public void setQuantity(long quantity) {
            this.quantity = (int) Mth.clamp(quantity, 0, Integer.MAX_VALUE);
        }

        @Override
        public void stackSlot(VRXMenu menu, VRXSlot slot, int button, ClickType type) {
            if (button == 0 || button == 1) {
                if (menu.getCarried().isEmpty()) {

                    slot.setOne(VRXOne.EMPTY);
                } else if (slot.isEmpty()) {

                    slot.setOne(this);
                } else if (slot.getOne() instanceof Item item && ItemHandlerHelper.canItemStacksStack(item.stack, menu.getCarried())) {

                    item = new Item(menu.getCarried().copy());
                    item.stackSlot(menu, slot, button, type);
                } else if (!(slot.getOne() instanceof Energy)) {
                    slot.setOne(EMPTY);
                }
            } else if (type == ClickType.CLONE && !menu.getCarried().isEmpty()) {
                if (slot.isEmpty()) {
                    slot.setOne(new Item(menu.getCarried()));
                } else if (!(slot.getOne() instanceof Item item && ItemHandlerHelper.canItemStacksStack(item.stack, menu.getCarried()))) {
                    slot.setOne(EMPTY);
                }
            }
        }

        @Override
        public boolean scrollSlot(VRXMenu menu, VRXSlot slot, double delta, boolean shiftDown) {
            return false;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void render(GuiGraphics graphics, int x, int y) {
//            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            graphics.blit(TEXTURE, x, y, 0, 0, 16, 16, 16, 16);
            if (this.collected) {
                Renders.slotScaledString(graphics.pose(), String.valueOf(this.getQuantity()), x, y);
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void renderTooltip(VRXScreen screen, GuiGraphics graphics, int x, int y) {
            if (screen.getMenu().getCarried().isEmpty()) {
                List<Component> tooltip = Lists.newArrayList();
                tooltip.add(Component.translatable("tooltip.hyperdaimc.vrx.energy"));
                tooltip.add(Component.translatable("tooltip.hyperdaimc.vrx.energy.description").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal("Minecraft Forge").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
                graphics.renderTooltip(screen.getMinecraft().font, tooltip, Optional.empty(), x, y);
            }
        }

        @Override
        public String toString() {
            return String.valueOf(this.quantity);
        }
    }

    static {
        EMPTY = new VRXOne(Type.EMPTY) {
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
        };
    }
}
