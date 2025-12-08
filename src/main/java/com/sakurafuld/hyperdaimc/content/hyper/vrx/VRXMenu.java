package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperMenus;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ClientboundVRXSetTooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class VRXMenu extends AbstractContainerMenu {
    public final Canvas canvas;
    public final List<Pair<NonNullList<VRXOne>, Direction>> indexes = Lists.newArrayList();
    private final Palette existed;
    //    private final List<Direction> availableFaces;
//    private final List<VRXType> availableTypes;
    public int index = 0;
    private boolean first = true;
    @Nullable
    private VRXTooltip tooltip = null;

    public VRXMenu(int pContainerId, Inventory inventory, FriendlyByteBuf buf) {
        this(pContainerId, inventory, Canvas.read(buf));
    }

    public VRXMenu(int pContainerId, Inventory inventory, Canvas canvas) {
        super(HyperMenus.VRX.get(), pContainerId);
        Player player = inventory.player;

        @SuppressWarnings("UnstableApiUsage")
        CapabilityProvider<?> provider = canvas.supply(player.level(), block -> block, entity -> entity);

//        List<Direction> availableFaces = Lists.newArrayList(Direction.values());
//        availableFaces.add(null);
//        List<VRXType> availableTypes = Lists.newArrayList();
//        availableFaces.removeIf(direction -> {
//            List<VRXType> checked = VRXRegistry.check(provider, direction);
//            if (checked.isEmpty())
//                return true;
//            else {
//                availableTypes.addAll(checked);
//                return false;
//            }
//        });
//        this.availableFaces = availableFaces;
//        this.availableTypes = ImmutableList.copyOf(availableTypes);

        @Nullable
        Direction face;
        if (!canvas.availableFaces.isEmpty() && !canvas.availableFaces.contains(canvas.face))
            face = canvas.availableFaces.get(0);
        else face = canvas.face;
        this.canvas = canvas.faced(face);

        Palette existing = this.getExistingVRX(player);
        List<VRXOne> first = existing.remove(face);
        if (first.isEmpty())
            this.indexes.add(Pair.of(NonNullList.withSize(27, VRXOne.EMPTY), face));
        else
            this.addIndex(first, face);
        existing.forEach((direction, ones) ->
                this.addIndex(ones, direction));
        existing.put(face, first);
        this.existed = existing;

        ItemStackHandler handler = new ItemStackHandler(27);

        int vrxSlot = 0;
        for (int row = 0; row < 3; ++row) {
            for (int colunm = 0; colunm < 9; ++colunm) {
                this.addSlot(new VRXSlot(handler, colunm + row * 9, 8 + colunm * 18, 18 + row * 18, this, this.getCurrentOnes().get(vrxSlot++)));
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int colunm = 0; colunm < 9; ++colunm) {
                this.addSlot(new Slot(inventory, colunm + row * 9 + 9, 8 + colunm * 18, 86 + row * 18));
            }
        }
        for (int row = 0; row < 9; ++row) {
            this.addSlot(new Slot(inventory, row, 8 + row * 18, 144));
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        if (!HyperCommonConfig.ENABLE_VRX.get())
            return false;
        if (this.canvas.availableFaces.isEmpty())
            return false;
        if (this.first) {
            if (pPlayer instanceof ServerPlayer serverPlayer)
                this.updateTooltip(serverPlayer);
            this.first = false;
        }

        return this.canvas.supply(pPlayer.level(), Objects::nonNull, Objects::nonNull);
    }

    // Prevent Jei Error.
    public void closedByKey(ServerPlayer player) {
        Palette palette = new Palette();
        this.indexes.forEach(pair -> {
            List<VRXOne> emptyExcluded = pair.getFirst().stream().filter(one -> !one.isEmpty()).toList();
            if (!emptyExcluded.isEmpty())
                palette.put(pair.getSecond(), emptyExcluded);
        });

        boolean different = !this.existed.matches(palette);

        this.canvas.execute(player.level(), block -> {
            VRXSavedData data = VRXSavedData.get(player.level());
            data.create(player.getUUID(), block.getBlockPos(), palette);
            data.sync2Client(PacketDistributor.DIMENSION.with(player.level()::dimension));
            if (different)
                VRXHandler.playSound(player.serverLevel(), Vec3.atCenterOf(block.getBlockPos()), palette.noEmpty());
        }, entity -> entity.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> {
            vrx.create(player.getUUID(), palette);
            vrx.sync2Client(entity.getId(), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player));
            if (different)
                VRXHandler.playSound(player.serverLevel(), entity.position(), palette.noEmpty());
        }));
    }

    @Override
    public void clicked(int pSlotId, int pButton, ClickType pClickType, Player pPlayer) {
        if (pSlotId >= 0 && this.getSlot(pSlotId) instanceof VRXSlot slot)
            slot.clicked(pButton, pClickType);
        else
            super.clicked(pSlotId, pButton, pClickType, pPlayer);
    }

    @Override
    public boolean clickMenuButton(Player pPlayer, int pId) {
        boolean face = ((pId >> 1) & 1) == 0;
        if (face) {
            List<Direction> availableFaces = this.canvas.availableFaces;
            int ordinal = availableFaces.indexOf(this.getCurrentFace());
            ordinal = (ordinal + ((pId & 1) == InputConstants.MOUSE_BUTTON_LEFT ? 1 : -1)) % availableFaces.size();
            this.indexes.set(this.index, Pair.of(this.getCurrentOnes(), availableFaces.get(ordinal < 0 ? availableFaces.size() + ordinal : ordinal)));

            if (pPlayer instanceof ServerPlayer serverPlayer)
                this.updateTooltip(serverPlayer);
            return true;
        } else {
            if ((pId & 1) == InputConstants.MOUSE_BUTTON_LEFT) {
                if (this.index > 0) {
                    this.changeIndex(this.index - 1);
                    return true;
                }
            } else {
                int max = this.indexes.size() - 1;
                if (this.index >= max) {
                    this.index = max;
                    this.indexes.add(Pair.of(NonNullList.withSize(27, VRXOne.EMPTY), this.indexes.get(max).getSecond()));
                }

                this.changeIndex(this.index + 1);
                return true;
            }
        }

        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        Slot slot = this.getSlot(pIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        ItemStack ret = source.copy();

        if (pIndex < 27) {
            if (!this.moveItemStackTo(source, 27, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (pIndex < this.slots.size() - 9) {
            if (!this.moveItemStackTo(source, this.slots.size() - 9, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(source, 27, this.slots.size() - 9, false)) {
            return ItemStack.EMPTY;
        }

        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return ret;
    }

    public void updateTooltip(ServerPlayer player) {
        List<VRXOne> list = Lists.newArrayList();
        this.canvas.execute(player.level(),
                block -> list.addAll(VRXRegistry.collect(block, this.getCurrentFace())),
                entity -> list.addAll(VRXRegistry.collect(entity, this.getCurrentFace())));

        HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundVRXSetTooltip(this.containerId, list));
    }

    public List<VRXType> getAvailableTypes() {
        return this.canvas.availableTypes;
    }

    public Optional<TooltipComponent> getTooltip() {
        return Optional.ofNullable(this.tooltip);
    }

    public void setTooltip(List<VRXOne> list) {
        this.tooltip = new VRXTooltip(list);
    }

    private Palette getExistingVRX(Player player) {
        Palette palette = new Palette();
        this.canvas.execute(player.level(), block -> {
            VRXSavedData data = VRXSavedData.get(player.level());
            data.getEntries(block.getBlockPos()).forEach(entry -> {
                if (player.getUUID().equals(entry.uuid))
                    palette.put(entry.face, entry.contents);
            });
        }, entity -> entity.getCapability(VRXCapability.TOKEN).ifPresent(vrx -> vrx.getEntries().forEach(entry -> {
            if (player.getUUID().equals(entry.uuid()))
                palette.put(entry.face(), entry.contents());
        })));

        return palette;
    }

    private void addIndex(List<VRXOne> ones, @Nullable Direction face) {
        if (!ones.isEmpty()) {
            NonNullList<VRXOne> list = NonNullList.withSize(27, VRXOne.EMPTY);
            int at = 0;
            for (VRXOne one : ones) {
                list.set(at, one);
                if (at >= 26) {
                    at = 0;
                    this.indexes.add(Pair.of(list, face));
                    list = NonNullList.withSize(27, VRXOne.EMPTY);
                } else at++;
            }

            if (list.stream().anyMatch(one -> !one.isEmpty()))
                this.indexes.add(Pair.of(list, face));
        }
    }

    private void changeIndex(int index) {
        this.index = Mth.clamp(0, index, this.indexes.size() - 1);
        for (int slot = 0; slot < 27; slot++) {
            if (this.getSlot(slot) instanceof VRXSlot vrx)
                vrx.setOne(this.getCurrentOnes().get(slot));
        }
    }

    private NonNullList<VRXOne> getCurrentOnes() {
        return this.indexes.get(this.index).getFirst();
    }

    public Direction getCurrentFace() {
        return this.indexes.get(this.index).getSecond();
    }

    public void onVRXChanged(int slot) {
        this.getCurrentOnes().set(slot, ((VRXSlot) this.getSlot(slot)).getOne());
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class Canvas {
        public final @Nullable BlockPos block;
        public final int entity;
        public final @Nullable Direction face;
        private final List<Direction> availableFaces;
        private final List<VRXType> availableTypes;

        private Canvas(@Nullable BlockPos block, int entity, @Nullable Direction face, List<Direction> availableFaces, List<VRXType> availableTypes) {
            this.block = block;
            this.entity = entity;
            this.face = face;
            this.availableFaces = availableFaces;
            this.availableTypes = availableTypes;
        }

        public static Pair<List<Direction>, List<VRXType>> getAvailables(CapabilityProvider<?> provider) {
            List<Direction> availableFaces = Lists.newArrayList(Direction.values());
            availableFaces.add(null);
            List<VRXType> availableTypes = Lists.newArrayList();
            availableFaces.removeIf(direction -> {
                List<VRXType> checked = VRXRegistry.check(provider, direction);
                if (checked.isEmpty())
                    return true;
                else {
                    availableTypes.addAll(checked);
                    return false;
                }
            });

            return Pair.of(availableFaces, availableTypes);
        }

        public static Canvas block(BlockPos block, @Nullable Direction face, List<Direction> availableFaces, List<VRXType> availableTypes) {
            return new Canvas(block, -1, face, availableFaces, availableTypes);
        }

        public static Canvas entity(int entity, @Nullable Direction face, List<Direction> availableFaces, List<VRXType> availableTypes) {
            return new Canvas(null, entity, face, availableFaces, availableTypes);
        }

        public Canvas faced(@Nullable Direction available) {
            return new Canvas(this.block, this.entity, available, this.availableFaces, this.availableTypes);
        }

        @Nullable
        public BlockEntity getBlock(Level level) {
            if (this.block == null) return null;
            return level.getBlockEntity(this.block);
        }

        @Nullable
        public Entity getEntity(Level level) {
            if (this.entity < 0) return null;
            return level.getEntity(this.entity);
        }

        public <T> T supply(Level level, Function<BlockEntity, T> blockFunction, Function<Entity, T> entityFunction) {
            if (this.block != null)
                return blockFunction.apply(level.getBlockEntity(this.block));
            else
                return entityFunction.apply(level.getEntity(this.entity));
        }

        public void execute(Level level, Consumer<BlockEntity> blockConsumer, Consumer<Entity> entityConsumer) {
            this.supply(level, block -> {
                if (block != null) blockConsumer.accept(block);
                return null;
            }, entity -> {
                if (entity != null) entityConsumer.accept(entity);
                return null;
            });
        }

        public void write(FriendlyByteBuf buf) {
            writeDirectionNullable(buf, this.face);

            buf.writeCollection(this.availableFaces, Canvas::writeDirectionNullable);
            buf.writeCollection(this.availableTypes, (buf1, type) -> buf1.writeUtf(type.name));

            if (this.block != null) {
                buf.writeBoolean(true);
                buf.writeBlockPos(this.block);
            } else {
                buf.writeBoolean(false);
                buf.writeVarInt(this.entity);
            }
        }

        private static void writeDirectionNullable(FriendlyByteBuf buf, @Nullable Direction face) {
            if (face != null) {
                buf.writeBoolean(true);
                buf.writeEnum(face);
            } else
                buf.writeBoolean(false);
        }

        public static Canvas read(FriendlyByteBuf buf) {
            @Nullable Direction face = readDirectionNullable(buf);
            List<Direction> availableFaces = buf.readList(Canvas::readDirectionNullable);
            List<VRXType> availableTypes = buf.readList(buf1 -> VRXRegistry.get(buf1.readUtf()));
            if (buf.readBoolean())
                return block(buf.readBlockPos(), face, availableFaces, availableTypes);
            else
                return entity(buf.readVarInt(), face, availableFaces, availableTypes);
        }

        public static Direction readDirectionNullable(FriendlyByteBuf buf) {
            return buf.readBoolean() ? buf.readEnum(Direction.class) : null;
        }
    }

    public static class Palette {
        private final TreeMap<Direction, List<VRXOne>> map = new TreeMap<>();
        private final List<VRXOne> nulls = Lists.newArrayList();

        public void put(@Nullable Direction face, List<VRXOne> ones) {
            if (!ones.isEmpty()) {
                if (face != null) {
                    List<VRXOne> list = this.map.computeIfAbsent(face, d -> Lists.newArrayList());
                    list.addAll(ones);
                    list.sort(Comparator.comparingInt(one -> one.type.priority()));
                } else {
                    this.nulls.addAll(ones);
                    this.nulls.sort(Comparator.comparingInt(one -> one.type.priority()));
                }
            }
        }

        public List<VRXOne> remove(@Nullable Direction face) {
            if (face != null) {
                List<VRXOne> ones = this.map.remove(face);
                return ones != null ? ones : Collections.emptyList();
            } else {
                List<VRXOne> nulls = ImmutableList.copyOf(this.nulls);
                this.nulls.clear();
                return nulls;
            }
        }

        public void forEach(BiConsumer<Direction, List<VRXOne>> consumer) {
            this.map.forEach(consumer);
            consumer.accept(null, this.nulls);
        }

        public boolean noEmpty() {
            return !(this.map.isEmpty() && this.nulls.isEmpty());
        }

        public boolean matches(Palette palette) {
            return this.map.equals(palette.map) && this.nulls.equals(palette.nulls);
        }
    }
}
