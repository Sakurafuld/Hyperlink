package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXSetJeiItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class VRXOneItem extends VRXOne {
    private ItemStack stack = ItemStack.EMPTY;
    private int count = 0;

    public VRXOneItem(ItemStack stack) {
        this();
        this.stack = stack.copy();
        this.count = this.stack.getCount();
    }

    public VRXOneItem() {
        super(VRXType.item());
    }

    public static boolean check(CapabilityProvider<?> provider, Direction face) {
        return provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face).isPresent();
    }

    public static VRXJeiWrapper<ItemStack> cast(Object ingredient) {
        if (ingredient instanceof ItemStack stack)
            return new Wrapper(stack);
        else
            return VRXJeiWrapper.empty();
    }

    public static List<VRXOne> collect(CapabilityProvider<?> provider, Direction face) {
        List<VRXOne> list = new ObjectArrayList<>();
        provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face).ifPresent(handler -> {
            for (int index = 0; index < handler.getSlots(); index++) {
                ItemStack stack = handler.getStackInSlot(index);
                if (!stack.isEmpty()) list.add(new VRXOneItem(stack));
            }
        });

        return list;
    }

    @Override
    public Object prepareInsert(CapabilityProvider<?> provider, Direction face, List<VRXOne> previous) {
        LazyOptional<IItemHandler> capability = provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face);
        if (capability.isPresent()) {
            IItemHandler handler = capability.orElseThrow(IllegalStateException::new);
            long count = -previous.stream()
                    .filter(one -> one instanceof VRXOneItem item && this.stack.is(item.stack.getItem()))
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
                if (button == 0)
                    slot.setOne(VRXOne.EMPTY);
                else
                    slot.getOne().setQuantity((this.getQuantity() + 1) / 2);
            } else if (slot.isEmpty()) {
                if (button == 0) {
                    if (type == ClickType.QUICK_MOVE)
                        stack.setCount(64);
                    slot.setOne(new VRXOneItem(stack));
                } else {
                    stack.setCount(1);
                    slot.setOne(new VRXOneItem(stack));
                }
            } else if (slot.getOne() instanceof VRXOneItem item) {
                if (ItemHandlerHelper.canItemStacksStack(stack, item.stack)) {
                    if (button == 0) {
                        if (type == ClickType.QUICK_MOVE)
                            stack.setCount(64);
                        slot.grow(stack.getCount());
                    } else slot.grow(1);
                } else slot.setOne(EMPTY);
            } else slot.setOne(EMPTY);
        } else if (type == ClickType.CLONE && menu.getCarried().isEmpty()) {
            stack.setCount(stack.getMaxStackSize());
            menu.setCarried(stack);
        }
    }

    @Override
    public boolean scrollSlot(VRXMenu menu, VRXSlot slot, double delta, boolean shiftDown) {
        long count;

        if (menu.getCarried().isEmpty())
            count = shiftDown ? 64 : 1;
        else
            count = shiftDown ? this.getQuantity() * 10 : this.getQuantity();


        if (delta < 0)
            count = -count;


        if (slot.isEmpty()) {
            this.setQuantity(count);
            slot.setOne(this);
        } else slot.grow(count);

        if (slot.isEmpty())
            slot.setOne(EMPTY);

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
            Renders.slotScaledString(graphics, String.valueOf(this.getQuantity()), x, y);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderTooltip(VRXScreen screen, GuiGraphics graphics, int x, int y) {
        if (screen.getMenu().getCarried().isEmpty())
            graphics.renderTooltip(screen.getMinecraft().font, this.stack, x, y);
    }

    public ItemStack getItemStack() {
        return this.stack;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        VRXOneItem that = (VRXOneItem) object;
        return count == that.count && Objects.equals(stack, that.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack, count);
    }

    @Override
    public String toString() {
        return "[" + ForgeRegistries.ITEMS.getKey(this.stack.getItem()) + "=" + this.count + "]";
    }

    public static class Wrapper extends VRXJeiWrapper<ItemStack> {

        protected Wrapper(ItemStack ingredient) {
            super(ingredient);
        }

        @Override
        public VRXType type() {
            return VRXType.item();
        }

        @Override
        public void accept(int containerId, VRXSlot slot) {
            slot.setOne(new VRXOneItem(this.ingredient));
            HyperConnection.INSTANCE.sendToServer(new ServerboundVRXSetJeiItem(containerId, ((Slot) slot).index, this.ingredient));
        }

        public void accept(VRXMenu menu, int index) {
            Slot slot = menu.getSlot(index);
            if (slot instanceof VRXSlot vrxSlot)
                vrxSlot.setOne(new VRXOneItem(this.ingredient));
            else
                slot.set(this.ingredient);
        }
    }
}
