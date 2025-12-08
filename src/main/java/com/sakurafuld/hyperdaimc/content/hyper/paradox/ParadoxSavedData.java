package com.sakurafuld.hyperdaimc.content.hyper.paradox;

import com.sakurafuld.hyperdaimc.infrastructure.Calculates;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.paradox.ClientboundParadoxSyncEntry;
import com.sakurafuld.hyperdaimc.network.paradox.ClientboundParadoxSyncSave;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

public class ParadoxSavedData extends SavedData {
    private static final ParadoxSavedData CLIENT = new ParadoxSavedData();

    private final Object2ObjectOpenHashMap<UUID, Entry> entries = new Object2ObjectOpenHashMap<>();

    private ParadoxSavedData() {
    }

    private ParadoxSavedData(CompoundTag tag) {
        this.load(tag);
    }

    public static ParadoxSavedData getServer() {
        ServerLevel level = ServerLifecycleHooks.getCurrentServer().overworld();
        return level.getDataStorage().computeIfAbsent(ParadoxSavedData::new, ParadoxSavedData::new, HYPERDAIMC + "_paradox");
    }

    public static ParadoxSavedData getClient() {
        return CLIENT;
    }

    public void add(UUID uuid, List<ItemStack> items) {
        this.entries.computeIfAbsent(uuid, u -> new Entry()).add(items);
    }

    public void set(UUID uuid, CompoundTag entry) {
        this.entries.put(uuid, new Entry(entry));
        this.setDirty();
    }

    public void remove(UUID uuid) {
        this.entries.remove(uuid);
        this.setDirty();
    }

    public Entry get(UUID uuid) {
        return this.entries.computeIfAbsent(uuid, u -> new Entry());
    }

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        ListTag entries = new ListTag();
        for (Object2ObjectMap.Entry<UUID, Entry> entry : this.entries.object2ObjectEntrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("UUID", entry.getKey());
            entry.getValue().save(tag);

            entries.add(tag);
        }

        pCompoundTag.put("Entries", entries);
        return pCompoundTag;
    }

    public void load(CompoundTag tag) {
        this.entries.clear();
        for (Tag nbt : tag.getList("Entries", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) nbt;
            UUID uuid = entry.getUUID("UUID");
            this.entries.put(uuid, new Entry(entry));
        }
    }

    public void sync2Client(PacketDistributor.PacketTarget target) {
        HyperConnection.INSTANCE.send(target, new ClientboundParadoxSyncSave(this.save(new CompoundTag())));
    }

    public void sync2Client(UUID uuid) {
        CompoundTag entry = this.entries.containsKey(uuid) ? Util.make(new CompoundTag(), this.entries.get(uuid)::save) : null;
        HyperConnection.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientboundParadoxSyncEntry(uuid, entry));
    }

    public class Entry {
        private final ObjectArrayList<ItemStack> stacks;
        @Nullable
        private ParadoxTooltip tooltip;

        private Entry() {
            this.stacks = new ObjectArrayList<>();
        }

        private Entry(CompoundTag tag) {
            this();
            for (Tag nbt : tag.getList("Stacks", Tag.TAG_COMPOUND)) {
                CompoundTag extended = (CompoundTag) nbt;
                ItemStack stack = ItemStack.of(extended.getCompound("Stack"));
                stack.setCount(extended.getInt("Count"));
                this.stacks.add(stack);
            }

            this.update();
        }

        public void add(List<ItemStack> items) {
            // Integer.MAX_VALUE以上は破棄.
            first:
            for (ItemStack item : items) {
                for (ItemStack existing : this.stacks) {
                    if (ItemHandlerHelper.canItemStacksStack(item, existing)) {
                        int space = Integer.MAX_VALUE - existing.getCount();
                        if (space > 0) existing.grow(Math.min(item.getCount(), space));
                        continue first;
                    }
                }

                this.stacks.add(item.copy());
            }

            this.update();
        }

        public boolean peek(Consumer<ItemStack> consumer) {
            this.stacks.removeIf(stack -> {
                consumer.accept(stack);
                return stack.isEmpty();
            });

            this.update();
            return this.stacks.isEmpty();
        }

        private void update() {
            if (this.stacks.isEmpty())
                this.tooltip = null;
            else {
                this.tooltip = new ParadoxTooltip(this.stacks);
                this.stacks.sort(Calculates.LOWEST_TO_HIGHEST);
            }

            ParadoxSavedData.this.setDirty();
        }

        public Optional<TooltipComponent> tooltip() {
            return Optional.ofNullable(this.tooltip);
        }

        public void save(CompoundTag tag) {
            ListTag stacks = new ListTag();
            for (ItemStack stack : this.stacks) {
                CompoundTag extended = new CompoundTag();
                extended.put("Stack", stack.serializeNBT());
                extended.putInt("Count", stack.getCount());
                stacks.add(extended);
            }

            tag.put("Stacks", stacks);
        }
    }
}
