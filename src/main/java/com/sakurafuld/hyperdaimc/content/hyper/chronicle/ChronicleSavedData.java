package com.sakurafuld.hyperdaimc.content.hyper.chronicle;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.infrastructure.Boxes;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.chronicle.ClientboundChronicleSyncSave;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

public class ChronicleSavedData extends SavedData {
    private static final Object2ObjectOpenHashMap<ResourceKey<Level>, ChronicleSavedData> client = new Object2ObjectOpenHashMap<>();

    private final Level level;
    private final List<Entry> entries = Lists.newArrayList();
    private final Long2ObjectOpenHashMap<List<Entry>> posMap = new Long2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<LevelChunkSection, Int2LongOpenHashMap> sectionMap = new Object2ObjectOpenHashMap<>();

    private ChronicleSavedData(Level level) {
        this.level = level;
    }

    private ChronicleSavedData(Level level, CompoundTag tag) {
        this.level = level;
        this.load(tag);
    }

    public static ChronicleSavedData get(Level level) {
        if (level instanceof ServerLevel serverLevel)
            return serverLevel.getDataStorage().computeIfAbsent(tag -> new ChronicleSavedData(level, tag), () -> new ChronicleSavedData(level), HYPERDAIMC + "_chronicle");
        else
            return client.computeIfAbsent(level.dimension(), dimension -> new ChronicleSavedData(level));
    }

    private void addEntry(Entry entry) {
        this.entries.add(entry);
        BlockPos.betweenClosedStream(entry.from, entry.to)
                .forEach(pos -> {
                    this.posMap.computeIfAbsent(pos.asLong(), at -> Lists.newArrayList()).add(0, entry);

                    LevelChunkSection section = this.level.getChunkAt(pos).getSection(this.level.getSectionIndex(pos.getY()));
                    this.sectionMap.computeIfAbsent(section, at -> Util.make(new Int2LongOpenHashMap(), map -> map.defaultReturnValue(Long.MIN_VALUE)))
                            .put((pos.getX() & 0xF) << 8 | (pos.getY() & 0xF) << 4 | pos.getZ() & 0xF, pos.asLong());
                });
    }

    private void removeEntry(Entry entry) {
        this.entries.remove(entry);
        this.posMap.values().removeIf(list -> {
            list.remove(entry);
            return list.isEmpty();
        });

        BlockPos.betweenClosedStream(entry.from, entry.to).forEach(pos -> {
            LevelChunkSection section = this.level.getChunkAt(pos).getSection(this.level.getSectionIndex(pos.getY()));
            Int2LongOpenHashMap map = this.sectionMap.get(section);
            if (map == null) return;

            map.remove((pos.getX() & 0xF) << 8 | (pos.getY() & 0xF) << 4 | pos.getZ() & 0xF);
            if (map.isEmpty()) this.sectionMap.remove(section);
        });
    }

    public List<Entry> getEntries() {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get())
            return Collections.emptyList();
        return this.entries;
    }

    @Nullable
    public List<Entry> getPaused(BlockPos pos) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get())
            return null;
        return this.posMap.get(pos.asLong());
    }

    @Nullable
    public Int2LongOpenHashMap getPaused(LevelChunkSection section) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get())
            return null;
        return this.sectionMap.get(section);
    }

    public boolean check(UUID uuid, BlockPos from, BlockPos to, Consumer<Component> sender) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get())
            return false;


        from = Boxes.clamp(this.level, from);
        to = Boxes.clamp(this.level, to);

        if (this.entries.contains(new Entry(uuid, from, to))) {
            sender.accept(Component.translatable("chat.hyperdaimc.chronicle.conflict").withStyle(ChatFormatting.YELLOW));
            return false;
        }

        if (BlockPos.betweenClosedStream(from, to).count() > HyperCommonConfig.CHRONICLE_SIZE.get()) {
            sender.accept(Component.translatable("chat.hyperdaimc.chronicle.too_large").withStyle(ChatFormatting.DARK_RED));
            return false;
        }

        return true;
    }

    public void pause(UUID uuid, BlockPos from, BlockPos to) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get())
            return;

        from = Boxes.clamp(this.level, from);
        to = Boxes.clamp(this.level, to);

        Entry entry = new Entry(uuid, from, to);
        if (!this.entries.contains(entry) && BlockPos.betweenClosedStream(from, to).count() <= HyperCommonConfig.CHRONICLE_SIZE.get()) {
            this.addEntry(entry);
            this.setDirty();
        }
    }

    public void restart(UUID uuid, BlockPos pos) {
        if (!HyperCommonConfig.ENABLE_CHRONICLE.get())
            return;

        List<Entry> paused = this.getPaused(pos);
        if (paused == null) return;

        for (Entry entry : paused) {
            if (entry.uuid.equals(uuid)) {
                this.removeEntry(entry);
                this.setDirty();
                break;
            }
        }
    }

    public void sync2Client(PacketDistributor.PacketTarget target) {
        HyperConnection.INSTANCE.send(target, new ClientboundChronicleSyncSave(this.save(new CompoundTag())));
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
        this.sectionMap.clear();
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
        public final long time;
        private final Vec3 center;

        private Entry(UUID uuid, BlockPos from, BlockPos to, long time) {
            this.uuid = uuid;
            this.from = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
            this.to = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
            this.aabb = Boxes.of(this.from, this.to);
            this.time = time;
            this.center = this.aabb.getCenter();
        }

        @SuppressWarnings("RedundantIfStatement")
        public boolean visible(Vec3 camera, float distance) {
            if (Math.abs(this.center.x() - camera.x()) > this.aabb.getXsize() / 2 + distance)
                return false;
            if (Math.abs(this.center.y() - camera.y()) > this.aabb.getYsize() / 2 + distance)
                return false;
            if (Math.abs(this.center.z() - camera.z()) > this.aabb.getZsize() / 2 + distance)
                return false;
            return true;
        }

        private Entry(UUID uuid, BlockPos from, BlockPos to) {
            this(uuid, from, to, System.currentTimeMillis());
        }

        public static Entry load(CompoundTag tag) {
            return new Entry(tag.getUUID("UUID"), BlockPos.of(tag.getLong("From")), BlockPos.of(tag.getLong("To")), tag.getLong("Time"));
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("UUID", this.uuid);
            tag.putLong("From", this.from.asLong());
            tag.putLong("To", this.to.asLong());
            tag.putLong("Time", this.time);
            return tag;
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
