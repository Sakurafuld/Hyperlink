package com.sakurafuld.hyperdaimc.addon.botania;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.content.hyper.vrx.*;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.BOTANIA;
import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

public class VRXOneMana extends VRXOne {
    private static final ResourceLocation TEXTURE = identifier(BOTANIA, "block/mana_water");

    private int mana;
    private boolean collected;

    public VRXOneMana(int mana, boolean collected) {
        this();
        this.mana = mana;
        this.collected = collected;
    }

    public VRXOneMana() {
        super(HyperBotania.vrxMana());
    }


    public static boolean check(CapabilityProvider<?> provider, Direction face) {
        return provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face).isPresent() || (provider instanceof BlockEntity tile && XplatAbstractions.INSTANCE.findManaReceiver(tile.getLevel(), tile.getBlockPos(), face) != null);
    }

    public static VRXOne convert(ItemStack stack) {
        ManaItem item = XplatAbstractions.INSTANCE.findManaItem(stack);
        if (item != null && item.getMana() > 0)
            return new VRXOneMana(Integer.MAX_VALUE, false);
        else return EMPTY;
    }

    public static List<VRXOne> collect(CapabilityProvider<?> provider, Direction face) {
        if (provider instanceof BlockEntity tile) {
            ManaReceiver receiver = XplatAbstractions.INSTANCE.findManaReceiver(tile.getLevel(), tile.getBlockPos(), face);
            if (receiver != null) {
                return Collections.singletonList(new VRXOneMana(receiver.getCurrentMana(), true));
            }
        }

        return Collections.emptyList();
    }

    @Override
    public @Nullable Object prepareInsert(CapabilityProvider<?> provider, @Nullable Direction face, List<VRXOne> previous) {
        if (provider instanceof BlockEntity tile) {
            ManaReceiver receiver = XplatAbstractions.INSTANCE.findManaReceiver(tile.getLevel(), tile.getBlockPos(), face);
            if (receiver != null && !receiver.isFull()) {
                return "BlockEntity";
            }
        }

        LazyOptional<IItemHandler> optional = provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face);
        if (optional.isPresent()) {
            IItemHandler items = optional.orElseThrow(IllegalStateException::new);
            for (int index = 0; index < items.getSlots(); index++) {
                ManaItem item = XplatAbstractions.INSTANCE.findManaItem(items.getStackInSlot(index));
                if (item != null && item.getMana() < item.getMaxMana()) {
                    return "ItemHandler";
                }
            }
        }

        return null;
    }

    @Override
    public void insert(CapabilityProvider<?> provider, @Nullable Direction face, Object prepared) {
        if (provider instanceof BlockEntity tile) {
            ManaReceiver receiver = XplatAbstractions.INSTANCE.findManaReceiver(tile.getLevel(), tile.getBlockPos(), face);
            if (receiver != null) {
                int count = 0;
                while (count <= 1024 && !receiver.isFull()) {
                    receiver.receiveMana(Integer.MAX_VALUE);
                    count++;
                }
            }
        }

        provider.getCapability(ForgeCapabilities.ITEM_HANDLER, face).ifPresent(items -> {
            for (int index = 0; index < items.getSlots(); index++) {
                ManaItem item = XplatAbstractions.INSTANCE.findManaItem(items.getStackInSlot(index));
                if (item != null) {
                    int count = 0;
                    while (count <= 1024 && item.getMana() < item.getMaxMana()) {
                        item.addMana(Integer.MAX_VALUE);
                        count++;
                    }
                }
            }
        });
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Mana", this.mana);
        tag.putBoolean("Collected", this.collected);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        this.mana = tag.getInt("Mana");
        this.collected = tag.getBoolean("Collected");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getQuantity() {
        return this.mana;
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
            } else if (!(slot.getOne() instanceof VRXOneMana)) {
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
            Renders.slotScaledString(graphics, String.valueOf(this.getQuantity()), x, y);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderTooltip(VRXScreen screen, GuiGraphics graphics, int x, int y) {
        if (screen.getMenu().getCarried().isEmpty()) {
            List<Component> tooltip = Lists.newArrayList();
            tooltip.add(Component.translatable("tooltip.hyperdaimc.vrx.botania_mana"));
            tooltip.add(Component.translatable("tooltip.hyperdaimc.vrx.botania_mana.description").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("Botania").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC));
            graphics.renderTooltip(screen.getMinecraft().font, tooltip, Optional.empty(), x, y);
        }
    }

    @Override
    public String toString() {
        return "[Iron'sMana=" + this.mana + ']';
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        VRXOneMana that = (VRXOneMana) object;
        return mana == that.mana && collected == that.collected;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mana, collected);
    }
}
