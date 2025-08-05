package com.sakurafuld.hyperdaimc.content.over.materializer;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.api.content.IOItemHandler;
import com.sakurafuld.hyperdaimc.content.HyperBlockEntities;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.materializer.ClientboundMaterializerSyncRecipe;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MaterializerBlockEntity extends BlockEntity implements MenuProvider {

    public final ItemStackHandler catalyst = new ItemStackHandler() {
        @Override
        protected void onContentsChanged(int slot) {
            MaterializerBlockEntity.this.updateRecipe();
            MaterializerBlockEntity.this.setChanged();
        }
    };
    public final ItemStackHandler fuel = new ItemStackHandler() {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return !stack.isEmpty() && MaterializerHandler.getFuel(stack.getItem()) > 0;
        }
    };
    public final ItemStackHandler result = new ItemStackHandler() {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    };
    private final IOItemHandler<ItemStackHandler> ioHandler = new IOItemHandler<>(this.catalyst, this.result);
    private LazyOptional<IItemHandler> capabilityFuel = LazyOptional.of(() -> this.fuel);
    private LazyOptional<IItemHandler> capability = LazyOptional.of(() -> this.ioHandler);

    private final List<ItemStack> processRecipe = Lists.newArrayList();
    private int fuelMax = 0;
    private int fuelRemaining = 0;
    private int processProgress = 0;
    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int pIndex) {
            return switch (pIndex) {
                case 0 -> MaterializerBlockEntity.this.fuelMax;
                case 1 -> MaterializerBlockEntity.this.fuelRemaining;
                default -> MaterializerBlockEntity.this.processProgress;
            };
        }

        @Override
        public void set(int pIndex, int pValue) {
            switch (pIndex) {
                case 0 -> MaterializerBlockEntity.this.fuelMax = pValue;
                case 1 -> MaterializerBlockEntity.this.fuelRemaining = pValue;
                default -> MaterializerBlockEntity.this.processProgress = pValue;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };


    public MaterializerBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(HyperBlockEntities.MATERIALIZER.get(), pPos, pBlockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide()) {
            if (this.fuelRemaining <= 0) {
                ItemStack fuel = this.fuel.extractItem(0, 1, false);
                if (!fuel.isEmpty()) {
                    this.fuelMax = MaterializerHandler.getFuel(fuel.getItem());
                    this.fuelRemaining = this.fuelMax;
                }
                this.setChanged();
            }

            if (!this.processRecipe.isEmpty() && this.fuelRemaining > 0 && this.result.getStackInSlot(0).isEmpty() && ++this.processProgress >= HyperCommonConfig.MATERIALIZER_TIME.get()) {
                if (--this.fuelRemaining <= 0) {
                    this.fuelMax = 0;
                }

                this.processProgress = 0;
                ItemStack stack = this.processRecipe.get(level.getRandom().nextInt(this.processRecipe.size()));
                this.result.setStackInSlot(0, stack.copy());
                this.setChanged();
            }
            if (MaterializerHandler.reloaded) {
                this.updateRecipe();
                this.setChanged();
            }
        }
    }

    private void updateRecipe() {
        if (this.getLevel() instanceof ServerLevel serverLevel) {
            this.updateRecipe(MaterializerHandler.materialize(serverLevel, this.catalyst.getStackInSlot(0)));
            HyperConnection.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> serverLevel.getChunkAt(this.getBlockPos())), new ClientboundMaterializerSyncRecipe(this.getBlockPos(), this.processRecipe));
        }
    }

    public void updateRecipe(List<ItemStack> list) {
        if (!this.processRecipe.equals(list)) {
            this.processRecipe.clear();
            this.processRecipe.addAll(list);
            this.processProgress = 0;
        }
    }

    public ItemStack getProcessItem() {
        return this.processRecipe.isEmpty() ? ItemStack.EMPTY : this.processRecipe.get(this.getLevel().getRandom().nextInt(this.processRecipe.size()));
    }

    public void drop() {
        Containers.dropContents(this.getLevel(), this.getBlockPos(), new RecipeWrapper(this.catalyst));
        Containers.dropContents(this.getLevel(), this.getBlockPos(), new RecipeWrapper(this.fuel));
        Containers.dropContents(this.getLevel(), this.getBlockPos(), new RecipeWrapper(this.result));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? side == Direction.UP ? this.capabilityFuel.cast() : this.capability.cast() : super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.capabilityFuel.invalidate();
        this.capability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.capabilityFuel = LazyOptional.of(() -> this.fuel);
        this.capability = LazyOptional.of(() -> this.ioHandler);
    }

    @Override
    public Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new MaterializerMenu(pContainerId, pPlayerInventory, this);
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
        pTag.put("Fuel", this.fuel.serializeNBT());
        pTag.put("Catalyst", this.catalyst.serializeNBT());
        pTag.put("Result", this.result.serializeNBT());

        ListTag processRecipe = new ListTag();
        this.processRecipe.stream()
                .map(IForgeItemStack::serializeNBT)
                .forEach(processRecipe::add);
        pTag.put("ProcessRecipe", processRecipe);
        pTag.putInt("FuelMax", this.fuelMax);
        pTag.putInt("FuelRemaining", this.fuelRemaining);
        pTag.putInt("ProcessProgress", this.processProgress);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.fuel.deserializeNBT(pTag.getCompound("Fuel"));
        this.catalyst.deserializeNBT(pTag.getCompound("Catalyst"));
        this.result.deserializeNBT(pTag.getCompound("Result"));

        this.processRecipe.clear();
        pTag.getList("ProcessRecipe", Tag.TAG_COMPOUND).stream()
                .map(CompoundTag.class::cast)
                .map(ItemStack::of)
                .forEach(this.processRecipe::add);
        this.fuelMax = pTag.getInt("FuelMax");
        this.fuelRemaining = pTag.getInt("FuelRemaining");
        this.processProgress = pTag.getInt("ProcessProgress");
    }
}
