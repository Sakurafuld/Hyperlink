package com.sakurafuld.hyperdaimc.content.hyper.chronicle;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.helper.Boxes;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.chronicle.ClientboundChronicleSyncSave;
import com.sakurafuld.hyperdaimc.network.chronicle.ServerboundChronicleSyncSave;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

public class ChronicleSavedData extends SavedData {
    private static final Object2ObjectOpenHashMap<ResourceKey<Level>, ChronicleSavedData> client = new Object2ObjectOpenHashMap<>();

    private final Level level;
    private final List<Entry> entries = Lists.newArrayList();
    private final Long2ObjectOpenHashMap<List<Entry>> posMap = new Long2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<LevelChunkSection, Int2LongOpenHashMap> chunkMap = new Object2ObjectOpenHashMap<>();

    private ChronicleSavedData(Level level) {
        this.level = level;
    }

    private ChronicleSavedData(Level level, CompoundTag tag) {
        this.level = level;
        this.load(tag);
    }

    public static ChronicleSavedData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(tag -> new ChronicleSavedData(level, tag), () -> new ChronicleSavedData(level), HYPERDAIMC + "_chronicle");
        } else {
            return client.computeIfAbsent(level.dimension(), dimension -> new ChronicleSavedData(level));
        }
    }

    public void addEntry(Entry entry) {
        this.entries.add(entry);
        BlockPos.betweenClosedStream(entry.from, entry.to)
                .forEach(pos -> {
                    this.posMap.computeIfAbsent(pos.asLong(), at -> Lists.newArrayList()).add(0, entry);

                    LevelChunkSection section = this.level.getChunkAt(pos).getSection(this.level.getSectionIndex(pos.getY()));
                    this.chunkMap.computeIfAbsent(section, at -> Util.make(new Int2LongOpenHashMap(), map -> map.defaultReturnValue(Long.MIN_VALUE)))
                            .put((pos.getX() & 0xF) << 8 | (pos.getY() & 0xF) << 4 | pos.getZ() & 0xF, pos.asLong());
                });
    }

    public void removeEntry(Entry entry) {
        this.entries.remove(entry);
        this.posMap.values().forEach(list -> list.remove(entry));

        BlockPos.betweenClosedStream(entry.from, entry.to)
                .forEach(pos -> {
                    LevelChunkSection section = this.level.getChunkAt(pos).getSection(this.level.getSectionIndex(pos.getY()));
                    this.chunkMap.getOrDefault(section, new Int2LongOpenHashMap())
                            .remove((pos.getX() & 0xF) << 8 | (pos.getY() & 0xF) << 4 | pos.getZ() & 0xF);
                });
    }

    public List<Entry> getEntries() {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get()) {
            return Collections.emptyList();
        }
        return this.entries;
    }

    public Optional<List<Entry>> getPaused(BlockPos pos) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.posMap.get(pos.asLong()));
    }

    public Optional<Int2LongOpenHashMap> getPaused(LevelChunkSection section) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.chunkMap.get(section));
    }

    public boolean pause(UUID uuid, BlockPos from, BlockPos to, Consumer<Component> sender) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get()) {
            return false;
        }

        Entry entry = new Entry(uuid, from, to);
        if (!this.entries.contains(entry)) {

            List<BlockPos> area = BlockPos.betweenClosedStream(from, to)
                    .map(BlockPos::immutable)
                    .toList();
            if (area.size() <= HyperCommonConfig.CHRONICLE_SIZE.get()) {

                this.addEntry(entry);
                this.setDirty();
            } else {
                sender.accept(new TranslatableComponent("chat.hyperdaimc.chronicle.too_large").withStyle(ChatFormatting.DARK_RED));
                return false;
            }
        }
        return true;
    }

    public void restart(UUID uuid, BlockPos pos) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get()) {
            return;
        }
        this.getPaused(pos)
                .flatMap(list -> list.stream().filter(entry -> entry.uuid.equals(uuid)).findFirst())
                .ifPresent(entry -> {
                    this.removeEntry(entry);
                    this.setDirty();
                });
    }

    public void sync2Client(ServerPlayer player) {
        HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundChronicleSyncSave(this.save(new CompoundTag())));
    }

    public void sync2Client(Supplier<ResourceKey<Level>> dimension) {
        HyperConnection.INSTANCE.send(PacketDistributor.DIMENSION.with(dimension), new ClientboundChronicleSyncSave(this.save(new CompoundTag())));
    }

    public void sync2Server() {
        HyperConnection.INSTANCE.sendToServer(new ServerboundChronicleSyncSave(this.save(new CompoundTag())));
    }

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        ListTag entries = new ListTag();
        this.entries.stream()
                .map(Entry::save)
                .forEach(entries::add);
        pCompoundTag.put("Entries", entries);

        return pCompoundTag;
    }

    public void load(CompoundTag tag) {
        this.entries.clear();
        this.posMap.clear();
        this.chunkMap.clear();
        tag.getList("Entries", Tag.TAG_COMPOUND).stream()
                .map(CompoundTag.class::cast)
                .map(Entry::load)
                .forEach(this::addEntry);
    }

    public static class Entry {
        public final UUID uuid;
        public final BlockPos from;
        public final BlockPos to;
        public final AABB aabb;

        public Entry(UUID uuid, BlockPos from, BlockPos to) {
            this.uuid = uuid;
            this.from = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
            this.to = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
            this.aabb = Boxes.of(this.from, this.to);
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("UUID", this.uuid);
            tag.putLong("From", this.from.asLong());
            tag.putLong("To", this.to.asLong());
            return tag;
        }

        public static Entry load(CompoundTag tag) {
            return new Entry(tag.getUUID("UUID"), BlockPos.of(tag.getLong("From")), BlockPos.of(tag.getLong("To")));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return Objects.equals(uuid, entry.uuid) && Objects.equals(from, entry.from) && Objects.equals(to, entry.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, from, to);
        }
    }
}
