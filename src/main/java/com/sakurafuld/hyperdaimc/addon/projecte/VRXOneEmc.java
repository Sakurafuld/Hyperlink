package com.sakurafuld.hyperdaimc.addon.projecte;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.*;
import com.sakurafuld.hyperdaimc.infrastructure.Renders;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.capabilities.block_entity.IEmcStorage;
import moze_intel.projecte.api.capabilities.item.IItemEmcHolder;
import moze_intel.projecte.gameObjs.items.PhilosophersStone;
import moze_intel.projecte.utils.PlayerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class VRXOneEmc extends VRXOne {
    private static final ResourceLocation TEXTURE = identifier("gui/vrx/emc");

    private long emc;
    private boolean collected;

    public VRXOneEmc(long emc, boolean collected) {
        this();
        this.emc = emc;
        this.collected = collected;
    }

    public VRXOneEmc() {
        super(HyperProjectE.vrxEmc());
    }

    public static boolean check(CapabilityProvider<?> provider, Direction face) {
        return provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face).isPresent() || provider.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY, face).isPresent();
    }

    public static VRXOne convert(ItemStack stack) {
        if (stack.getItem() instanceof PhilosophersStone)
            return new VRXOneEmc(Long.MAX_VALUE, false);
        LazyOptional<IItemEmcHolder> capability = stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY);
        if (capability.isPresent()) {
            IItemEmcHolder holder = capability.orElseThrow(RuntimeException::new);
            if (holder.getStoredEmc(stack) > 0)
                return new VRXOneEmc(Long.MAX_VALUE, false);
        }

        return EMPTY;
    }

    public static List<VRXOne> collect(CapabilityProvider<?> provider, Direction face) {
        {
            LazyOptional<IKnowledgeProvider> capability = provider.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
            if (capability.isPresent()) {
                IKnowledgeProvider knowledge = capability.orElseThrow(RuntimeException::new);
                return Collections.singletonList(new VRXOneEmc(knowledge.getEmc().min(BigInteger.valueOf(Long.MAX_VALUE)).longValueExact(), true));
            }
        }
        {
            LazyOptional<IEmcStorage> capability = provider.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY, face);
            if (capability.isPresent()) {
                IEmcStorage storage = capability.orElseThrow(RuntimeException::new);
                return Collections.singletonList(new VRXOneEmc(storage.getStoredEmc(), true));
            }
        }

        return Collections.emptyList();
    }

    @Override
    public @Nullable Object prepareInsert(CapabilityProvider<?> provider, @Nullable Direction face, List<VRXOne> previous) {
        {
            LazyOptional<IKnowledgeProvider> capability = provider.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY);
            if (capability.isPresent()) {
                IKnowledgeProvider knowledge = capability.orElseThrow(RuntimeException::new);
                if (knowledge.getEmc().compareTo(HyperProjectE.unit()) < 0)
                    return "Yeah";
            }
        }
        {
            LazyOptional<IEmcStorage> capability = provider.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY, face);
            if (capability.isPresent()) {
                IEmcStorage storage = capability.orElseThrow(RuntimeException::new);
                if (storage.getNeededEmc() > 0)
                    return "Okay";
            }
        }
        {
            LazyOptional<IItemHandler> capability = provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face);
            if (capability.isPresent()) {
                IItemHandler items = capability.orElseThrow(RuntimeException::new);
                for (int index = 0; index < items.getSlots(); index++) {
                    ItemStack stack = items.getStackInSlot(index);
                    LazyOptional<IItemEmcHolder> optional = stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY);
                    if (optional.isPresent() && optional.orElseThrow(RuntimeException::new).getNeededEmc(stack) > 0)
                        return "Haha";
                }
            }
        }

        return null;
    }

    @Override
    public void insert(CapabilityProvider<?> provider, @Nullable Direction face, Object prepared) {
        provider.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(knowledge -> {
            if (knowledge.getEmc().compareTo(HyperProjectE.unit()) < 0) {
                knowledge.setEmc(HyperProjectE.unit());
                if (provider instanceof ServerPlayer player) {
                    knowledge.syncEmc(player);
                    PlayerHelper.updateScore(player, PlayerHelper.SCOREBOARD_EMC, HyperProjectE.unit());
                }
            }
        });

        provider.getCapability(PECapabilities.EMC_STORAGE_CAPABILITY, face).ifPresent(storage -> {
            int count = 0;
            while (count <= 1024 && storage.getNeededEmc() > 0 && storage.insertEmc(Long.MAX_VALUE, IEmcStorage.EmcAction.SIMULATE) > 0) {
                storage.insertEmc(Long.MAX_VALUE, IEmcStorage.EmcAction.EXECUTE);
                count++;
            }
        });

        provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face).ifPresent(items -> {
            for (int index = 0; index < items.getSlots(); index++) {
                ItemStack stack = items.getStackInSlot(index);
                stack.getCapability(PECapabilities.EMC_HOLDER_ITEM_CAPABILITY).ifPresent(holder -> {
                    int count = 0;
                    while (count <= 1024 && holder.getNeededEmc(stack) > 0 && holder.insertEmc(stack, Long.MAX_VALUE, IEmcStorage.EmcAction.SIMULATE) > 0) {
                        holder.insertEmc(stack, Long.MAX_VALUE, IEmcStorage.EmcAction.EXECUTE);
                        count++;
                    }
                });
            }
        });
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Emc", this.emc);
        tag.putBoolean("Collected", this.collected);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        this.emc = tag.getLong("Emc");
        this.collected = tag.getBoolean("Collected");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getQuantity() {
        return this.emc;
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
            } else if (!(slot.getOne() instanceof VRXOneEmc)) {
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
            tooltip.add(Component.translatable("tooltip.hyperdaimc.vrx.emc"));
            tooltip.add(Component.translatable("tooltip.hyperdaimc.vrx.emc.description").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("ProjectE").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
            graphics.renderTooltip(screen.getMinecraft().font, tooltip, Optional.empty(), x, y);
        }
    }

    @Override
    public String toString() {
        return "[E=MC^" + this.emc + ']';
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        VRXOneEmc vrxOneEmc = (VRXOneEmc) object;
        return emc == vrxOneEmc.emc && collected == vrxOneEmc.collected;
    }

    @Override
    public int hashCode() {
        return Objects.hash(emc, collected);
    }
}
