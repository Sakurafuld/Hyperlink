package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class VRXOneEnergy extends VRXOne {
    private static final ResourceLocation TEXTURE = identifier("gui/vrx/energy");
    private int quantity = 0;
    private boolean collected = false;

    public VRXOneEnergy(int quantity, boolean collected) {
        this();
        this.quantity = quantity;
        this.collected = collected;
    }

    public VRXOneEnergy() {
        super(VRXType.energy());
    }

    public static boolean check(CapabilityProvider<?> provider, Direction face) {
        return provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face).isPresent() || provider.getCapability(ForgeCapabilities.ENERGY, face).isPresent();
    }

    public static VRXOne convert(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .filter(handler -> handler.getEnergyStored() > 0)
                .map(handler -> (VRXOne) new VRXOneEnergy(Integer.MAX_VALUE, false))
                .orElse(EMPTY);
    }

    public static List<VRXOne> collect(CapabilityProvider<?> provider, Direction face) {
        return provider.getCapability(ForgeCapabilities.ENERGY, face).map(handler ->
                        Collections.singletonList((VRXOne) new VRXOneEnergy(handler.getEnergyStored(), true)))
                .orElse(Collections.emptyList());
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

    @Override
    public void load(CompoundTag tag) {
        this.quantity = tag.getInt("Quantity");
        this.collected = tag.getBoolean("Collected");
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
    }

    @Override
    public void stackSlot(VRXMenu menu, VRXSlot slot, int button, ClickType type) {
        if (button == 0 || button == 1) {
            if (menu.getCarried().isEmpty()) {

                slot.setOne(VRXOne.EMPTY);
            } else if (slot.isEmpty()) {

                slot.setOne(this);
            } else if (slot.getOne() instanceof VRXOneItem item && ItemHandlerHelper.canItemStacksStack(item.getItemStack(), menu.getCarried())) {

                item = new VRXOneItem(menu.getCarried().copy());
                item.stackSlot(menu, slot, button, type);
            } else if (!(slot.getOne() instanceof VRXOneEnergy)) {
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
        if (!menu.getCarried().isEmpty() && !slot.isEmpty()) {
            int carried = menu.getCarried().getCount();
            long count = shiftDown ? carried * 10L : carried;

            if (delta < 0)
                count = -count;

            slot.grow(count);

            if (slot.isEmpty())
                slot.setOne(EMPTY);

            return true;
        } else return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE);
        graphics.blit(x, y, 0, 16, 16, sprite);
        if (this.collected)
            Renders.itemCount(graphics, this.getQuantity(), x, y);
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
        return "[ForgeEnergy=" + this.quantity + "]";
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        VRXOneEnergy that = (VRXOneEnergy) object;
        return quantity == that.quantity && collected == that.collected;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, collected);
    }
}
