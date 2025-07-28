package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.sakurafuld.hyperdaimc.content.HyperBlockEntities;
import com.sakurafuld.hyperdaimc.content.HyperBlocks;
import com.sakurafuld.hyperdaimc.content.HyperMenus;
import com.sakurafuld.hyperdaimc.content.HyperSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class DeskMenu extends AbstractContainerMenu {
    public final ContainerLevelAccess access;
    private final ContainerData data;
    public boolean canCraft = false;

    public DeskMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos(), HyperBlockEntities.DESK.get()).orElseThrow());
    }

    public DeskMenu(int id, Inventory inventory, DeskBlockEntity desk) {
        super(HyperMenus.DESK.get(), id);
        this.access = ContainerLevelAccess.create(inventory.player.level(), desk.getBlockPos());
        this.data = desk.data;
        this.addSlot(new SlotItemHandler(desk.result, 0, 80, 188) {

            @Override
            public boolean mayPickup(Player playerIn) {
                if (DeskMenu.this.isMinecraft()) {
                    return false;
                } else {
                    if (!DeskMenu.this.canCraft && this.hasItem()) {
                        playerIn.playNotifySound(HyperSounds.DESK_RESULT.get(), SoundSource.MASTER, 0.25f, 1);
                    }
                    return true;
                }
            }

            @Override
            public int getMaxStackSize(@NotNull ItemStack stack) {
                return Math.min(this.getMaxStackSize(), stack.getMaxStackSize());
            }
        });

        for (int row = 0; row < 9; ++row) {
            for (int colunm = 0; colunm < 9; ++colunm) {
                this.addSlot(new SlotItemHandler(desk.inventory, colunm + row * 9, 8 + colunm * 18, 18 + row * 18) {
                    @Override
                    public boolean mayPickup(Player playerIn) {
                        return true;
                    }

                    @Override
                    public int getMaxStackSize(@NotNull ItemStack stack) {
                        return Math.min(this.getMaxStackSize(), stack.getMaxStackSize());
                    }
                });
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int colunm = 0; colunm < 9; ++colunm) {
                this.addSlot(new Slot(inventory, colunm + row * 9 + 9, 8 + colunm * 18, 214 + row * 18));
            }
        }
        for (int row = 0; row < 9; ++row) {
            this.addSlot(new Slot(inventory, row, 8 + row * 18, 272));
        }

        this.addDataSlots(this.data);
        desk.updateRecipe();
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(this.access, pPlayer, HyperBlocks.DESK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {

        Slot slot = this.getSlot(pIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack source = slot.getItem();
        ItemStack ret = source.copy();

        if (pIndex < 82) {
            if (!this.moveItemStackTo(source, 82, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(source, 1, 82, false)) {
            if (pIndex < 109) {
                if (!this.moveItemStackTo(source, 109, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(source, 82, 109, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (pIndex == 0) {
            this.access.execute(((level, pos) -> level.getBlockEntity(pos, HyperBlockEntities.DESK.get()).ifPresent(DeskBlockEntity::consumeRecipe)));
        }

        return ret;
    }

    public boolean isMinecraft() {
        return this.data.get(0) == 1;
    }
}
