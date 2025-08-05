package com.sakurafuld.hyperdaimc.content.over.materializer;

import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperBlockEntities;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.HyperMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class MaterializerMenu extends AbstractContainerMenu {
    public final ContainerLevelAccess access;
    private final ContainerData data;

    public MaterializerMenu(int pContainerId, Inventory inventory, FriendlyByteBuf buf) {
        this(pContainerId, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos(), HyperBlockEntities.MATERIALIZER.get()).orElseThrow());
    }

    public MaterializerMenu(int id, Inventory inventory, MaterializerBlockEntity materializer) {
        super(HyperMenus.MATERIALIZER.get(), id);
        this.access = ContainerLevelAccess.create(inventory.player.level(), materializer.getBlockPos());
        this.data = materializer.data;

        this.addSlot(new SlotItemHandler(materializer.catalyst, 0, 16, 48) {
            @Override
            public boolean mayPickup(Player playerIn) {
                return true;
            }

            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) {
                return Math.min(this.getMaxStackSize(), stack.getMaxStackSize());
            }
        });
        this.addSlot(new SlotItemHandler(materializer.fuel, 0, 80, 17));
        this.addSlot(new SlotItemHandler(materializer.result, 0, 148, 48) {
            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) {
                return Math.min(this.getMaxStackSize(), stack.getMaxStackSize());
            }
        });

        for (int column = 0; column < 3; ++column) {
            for (int row = 0; row < 9; ++row) {
                this.addSlot(new Slot(inventory, row + column * 9 + 9, 8 + row * 18, 84 + column * 18));
            }
        }

        for (int row = 0; row < 9; ++row) {
            this.addSlot(new Slot(inventory, row, 8 + row * 18, 142));
        }

        this.addDataSlots(this.data);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        Slot slot = this.getSlot(pIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack source = slot.getItem();
        ItemStack ret = source.copy();

        if (pIndex < 3) {
            if (!this.moveItemStackTo(source, 3, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(source, 0, 2, false)) {
            if (pIndex < this.slots.size() - 9) {
                if (!this.moveItemStackTo(source, this.slots.size() - 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(source, 3, this.slots.size() - 9, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return ret;
    }

    public int getFuelMax() {
        return this.data.get(0);
    }

    public int getFuelRemaining() {
        return this.data.get(1);
    }

    public int getProcessProgress() {
        return this.data.get(2);
    }

    public int getFuelGauge() {
        return this.getFuelMax() > 0 ? this.getFuelRemaining() * 152 / this.getFuelMax() : 0;
    }

    public int getProcessGauge() {
        return this.getProcessProgress() * 94 / HyperCommonConfig.MATERIALIZER_TIME.get();
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(this.access, pPlayer, HyperBlocks.MATERIALIZER.get());
    }
}
