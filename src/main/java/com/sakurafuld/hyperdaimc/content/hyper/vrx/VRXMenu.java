package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.content.HyperMenus;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ClientboundVRXSetTooltip;
import com.sakurafuld.hyperdaimc.network.vrx.ClientboundVRXSyncCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.sakurafuld.hyperdaimc.helper.Deets.LOG;

public class VRXMenu extends AbstractContainerMenu {
    public final Pair<BlockPos, Integer> provider;
    @Nullable
    public Direction face;

    private boolean first = true;
    private final List<Direction> available;

    @Nullable
    private VRXTooltip tooltip = null;

    public VRXMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, parse(buf));
    }

    public VRXMenu(int id, Inventory inventory, Pair<Pair<BlockPos, Integer>, Direction> pair) {
        super(HyperMenus.VRX.get(), id);
        LOG.debug("Open:{}", pair);
        this.provider = pair.getFirst();
        this.face = pair.getSecond();

        List<Direction> available = Lists.newArrayList(Direction.values());
        available.add(null);
        CapabilityProvider<?> provider = this.execute(inventory.player.getLevel(), block -> block, entity -> entity);
        available.removeIf(face -> !VRXOne.Type.check(provider, face));
        this.available = available;

        if (!this.available.isEmpty() && !this.available.contains(this.face)) {
            this.face = this.available.get(0);
        }

        ItemStackHandler handler = new ItemStackHandler(27);

        for (int row = 0; row < 3; ++row) {
            for (int colunm = 0; colunm < 9; ++colunm) {
                this.addSlot(new VRXSlot(handler, colunm + row * 9, 8 + colunm * 18, 18 + row * 18));
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

    public static void parse(FriendlyByteBuf buf, Pair<Pair<BlockPos, Integer>, Direction> pair) {

        if (pair.getFirst().getFirst() != null) {
            buf.writeBoolean(true);
            buf.writeBlockPos(pair.getFirst().getFirst());
        } else {
            buf.writeBoolean(false);
            buf.writeVarInt(pair.getFirst().getSecond());
        }

        if (pair.getSecond() != null) {
            buf.writeBoolean(true);
            buf.writeUtf(pair.getSecond().name());
        } else {
            buf.writeBoolean(false);
        }
    }

    public static Pair<Pair<BlockPos, Integer>, Direction> parse(FriendlyByteBuf buf) {

        Pair<BlockPos, Integer> pair;
        if (buf.readBoolean()) {
            pair = Pair.of(buf.readBlockPos(), null);
        } else {
            pair = Pair.of(null, buf.readVarInt());
        }

        Direction face = buf.readBoolean() ? Direction.valueOf(buf.readUtf()) : null;

        return Pair.of(pair, face);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) {
            return false;
        }
        if (this.first) {
            this.updateTooltip(pPlayer);
            this.first = false;
        }
        return !this.available.isEmpty() && this.execute(pPlayer.getLevel(), Objects::nonNull, Objects::nonNull);
    }

    // Prevent Jei Error.
    public void closedByKey(ServerPlayer player) {
        List<VRXOne> list = Lists.newArrayList();
        for (int index = 0; index < 27; index++) {
            if (this.getSlot(index) instanceof VRXSlot slot && !slot.isEmpty()) {
                list.add(slot.getOne());
            }
        }
        if (!list.isEmpty()) {

            this.execute(player.getLevel(), block -> {
                if (block != null) {
                    VRXSavedData data = VRXSavedData.get(player.getLevel());
                    data.create(player.getUUID(), block.getBlockPos(), this.face, list);
                    data.sync2Client(player.getLevel()::dimension);
                    VRXHandler.playSound(player.getLevel(), Vec3.atCenterOf(block.getBlockPos()), true);
                }
            }, entity -> {
                if (entity != null) {
                    entity.getCapability(VRXCapability.CAPABILITY).ifPresent(vrx -> {
                        vrx.create(player.getUUID(), this.face, list);
                        HyperConnection.INSTANCE.send(PacketDistributor.DIMENSION.with(player.getLevel()::dimension), new ClientboundVRXSyncCapability(entity.getId(), vrx.serializeNBT()));
                        VRXHandler.playSound(player.getLevel(), entity.position(), true);
                    });
                }
            });
        }
        player.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public void clicked(int pSlotId, int pButton, ClickType pClickType, Player pPlayer) {
        if (pSlotId >= 0 && this.getSlot(pSlotId) instanceof VRXSlot slot) {
            slot.clicked(this, pButton, pClickType);
        } else {
            super.clicked(pSlotId, pButton, pClickType, pPlayer);
        }
    }

    @Override
    public boolean clickMenuButton(Player pPlayer, int pId) {
        if (!this.available.isEmpty()) {
            int ordinal = this.available.indexOf(this.face);
            ordinal = (ordinal + (pId == 0 ? 1 : -1)) % this.available.size();
            this.face = this.available.get(ordinal < 0 ? this.available.size() + ordinal : ordinal);

            this.updateTooltip(pPlayer);

            return true;
        } else {
            return false;
        }
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

    public void updateTooltip(Player player) {
        if (!this.available.isEmpty()) {

            if (player instanceof ServerPlayer serverPlayer) {
                List<VRXOne> list = Lists.newArrayList();
                this.execute(player.getLevel(), block -> {
                    if (block != null) {
                        list.addAll(VRXOne.Type.collect(block, this.face));
                    }
                }, entity -> {
                    if (entity != null) {
                        list.addAll(VRXOne.Type.collect(entity, this.face));
                    }
                });

                HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientboundVRXSetTooltip(this.containerId, list));
            }
        }

    }

    public Optional<TooltipComponent> getTooltip() {
        return Optional.ofNullable(this.tooltip);
    }

    public void setTooltip(List<VRXOne> list) {

        this.tooltip = new VRXTooltip(list);
    }

    public <T> T execute(Level level, Function<BlockEntity, T> blockFunction, Function<Entity, T> entityFunction) {
        if (this.provider.getFirst() != null) {
            return blockFunction.apply(level.getBlockEntity(this.provider.getFirst()));
        } else {
            return entityFunction.apply(level.getEntity(this.provider.getSecond()));
        }
    }

    public void execute(Level level, Consumer<BlockEntity> blockConsumer, Consumer<Entity> entityConsumer) {
        this.execute(level, block -> {
            blockConsumer.accept(block);
            return null;
        }, entity -> {
            entityConsumer.accept(entity);
            return null;
        });
    }
}
