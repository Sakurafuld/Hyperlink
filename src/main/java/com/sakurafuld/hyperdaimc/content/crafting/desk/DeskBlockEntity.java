package com.sakurafuld.hyperdaimc.content.crafting.desk;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.api.content.IOItemHandler;
import com.sakurafuld.hyperdaimc.content.HyperBlockEntities;
import com.sakurafuld.hyperdaimc.content.HyperRecipes;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.desk.ClientboundDeskMinecraft;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

public class DeskBlockEntity extends BlockEntity implements MenuProvider {
    private final Object2ObjectOpenHashMap<Item, IntAVLTreeSet> lock = new Object2ObjectOpenHashMap<>();
    private final Object2IntOpenHashMap<Item> minimum = new Object2IntOpenHashMap<>();

    public final ItemStackHandler inventory = new ItemStackHandler(81) {

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return DeskBlockEntity.this.checkLock(stack.getItem(), slot);
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (DeskBlockEntity.this.checkLock(stack.getItem(), slot)) {
                super.setStackInSlot(slot, stack);
            }
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (DeskBlockEntity.this.checkLockMinimum(stack.getItem(), slot)) {
                return super.insertItem(slot, stack, simulate);
            } else {
                return stack;
            }
        }

        @Override
        protected void onContentsChanged(int slot) {
            DeskBlockEntity.this.updateRecipe();
            DeskBlockEntity.this.updateLockMinimum();
            DeskBlockEntity.this.setChanged();
        }
    };
    public final ItemStackHandler result = new ItemStackHandler() {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (DeskBlockEntity.this.minecraft) {
                return ItemStack.EMPTY;
            } else {
                ItemStack stack = super.extractItem(slot, amount, simulate);
                if (!simulate && !stack.isEmpty()) {
                    DeskBlockEntity.this.consumeRecipe();
                }
                return stack;
            }
        }
    };
    private final RecipeWrapper wrapper = new RecipeWrapper(this.inventory);
    private final IOItemHandler<ItemStackHandler> ioHandler = new IOItemHandler<>(this.inventory, this.result);
    private LazyOptional<IItemHandler> capability = LazyOptional.of(() -> this.ioHandler);
    private boolean minecraft = false;
    public Player minecrafter = null;
    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            return DeskBlockEntity.this.minecraft ? 1 : 0;
        }

        @Override
        public void set(int pIndex, int pValue) {
            DeskBlockEntity.this.minecraft = pValue > 0;
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public DeskBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(HyperBlockEntities.DESK.get(), pPos, pBlockState);
    }

    public void updateRecipe() {
        if (this.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getRecipeManager()
                    .getRecipeFor(HyperRecipes.DESK.get(), this.wrapper, serverLevel)
                    .ifPresentOrElse(recipe -> {
                        this.minecraft = recipe.isMinecraft();
                        this.result.setStackInSlot(0, recipe.assemble(this.wrapper, serverLevel.registryAccess()));
                    }, () -> {
                        this.minecraft = false;
                        this.result.setStackInSlot(0, ItemStack.EMPTY);
                    });
        }
    }

    public void consumeRecipe() {
        for (int index = 0; index < this.inventory.getSlots(); index++) {
            ItemStack stack = this.inventory.getStackInSlot(index);
            if (!stack.isEmpty()) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    this.inventory.setStackInSlot(index, ItemStack.EMPTY);
                }
            }
        }

        this.updateRecipe();
        this.setChanged();
    }

    public void dropOrMinecraft() {
        if (this.getLevel() instanceof ServerLevel serverLevel) {
            if (this.minecraft) {
                DeskSavedData savedData = DeskSavedData.get(serverLevel);
                List<ItemStack> ingredients = Lists.newArrayList();
                for (int index = 0; index < this.inventory.getSlots(); index++) {
                    ItemStack ingredient = this.inventory.getStackInSlot(index);
                    if (!ingredient.isEmpty()) {
                        ingredients.add(ingredient);
                    }
                }
                DeskSavedData.Entry entry = savedData.add(this.getBlockPos(), ingredients, this.result.getStackInSlot(0));
                HyperConnection.INSTANCE.send(PacketDistributor.DIMENSION.with(serverLevel::dimension), new ClientboundDeskMinecraft(entry));
                this.consumeRecipe();
            }

            if (this.minecrafter == null) {
                Containers.dropContents(serverLevel, this.getBlockPos(), this.wrapper);
            } else {
                for (int index = 0; index < this.inventory.getSlots(); index++) {
                    DeskBlock.give(this.minecrafter, this.inventory.getStackInSlot(index));
                }
            }
        }
    }

    public boolean isMinecraft() {
        return this.minecraft;
    }

    public void lockRecipe(Object2ObjectOpenHashMap<Item, IntAVLTreeSet> lock) {
        this.lock.clear();
        this.lock.putAll(lock);
        this.updateLockMinimum();
    }

    public void foeEachLocked(BiConsumer<Item, IntAVLTreeSet> consumer) {
        this.lock.forEach(consumer);
    }

    public boolean checkLock(Item item, int index) {
        return item == Items.AIR || this.lock.isEmpty() || (this.lock.containsKey(item) && this.lock.get(item).contains(index));
    }

    public boolean checkLockMinimum(Item item, int index) {
        return item == Items.AIR || this.minimum.isEmpty() || (this.minimum.containsKey(item) && this.minimum.getInt(item) == index);
    }

    private void updateLockMinimum() {
        this.minimum.clear();

        this.lock.forEach((item, set) -> {
            int count = -1;
            int slot = set.firstInt();
            IntIterator iterator = set.intIterator();
            while (iterator.hasNext()) {
                int index = iterator.nextInt();
                ItemStack stack = this.inventory.getStackInSlot(index);
                if (stack.isEmpty()) {
                    slot = index;
                    break;
                }
                int current = this.inventory.getStackInSlot(index).getCount();
                if (count == -1 || count > current) {
                    count = current;
                    slot = index;
                }
            }
            this.minimum.put(item, slot);
        });
    }

    @Override
    public void onLoad() {
        this.updateRecipe();
        super.onLoad();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == ForgeCapabilities.ITEM_HANDLER ? this.capability.cast() : super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.capability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.capability = LazyOptional.of(() -> this.ioHandler);
    }

    @Override
    public Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new DeskMenu(pContainerId, pPlayerInventory, this);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.put("Items", this.ioHandler.serializeNBT());
        pTag.putBoolean("Minecraft", this.minecraft);
        ListTag list = new ListTag();
        this.lock.forEach((item, set) -> {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Item", Item.getId(item));
            tag.putIntArray("Indexes", set.toIntArray());
            list.add(tag);
        });
        pTag.put("Lock", list);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.ioHandler.deserializeNBT(pTag.getCompound("Items"));
        this.minecraft = pTag.getBoolean("Minecraft");

        Object2ObjectOpenHashMap<Item, IntAVLTreeSet> lock = pTag.getList("Lock", Tag.TAG_COMPOUND).stream()
                .map(CompoundTag.class::cast)
                .collect(Object2ObjectOpenHashMap::new, (map, tag) -> {
                    Item item = Item.byId(tag.getInt("Item"));
                    IntAVLTreeSet set = new IntAVLTreeSet(tag.getIntArray("Indexes"));
                    map.put(item, set);
                }, Object2ObjectOpenHashMap::putAll);

        this.lockRecipe(lock);
        this.updateRecipe();
    }
}
