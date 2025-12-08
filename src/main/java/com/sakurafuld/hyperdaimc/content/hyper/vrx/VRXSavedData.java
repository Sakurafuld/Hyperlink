package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ClientboundVRXSyncSave;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;

public class VRXSavedData extends SavedData {
    private static final Object2ObjectOpenHashMap<ResourceKey<Level>, VRXSavedData> client = new Object2ObjectOpenHashMap<>();

    private final List<Entry> entries = Lists.newArrayList();
    private final Long2ObjectOpenHashMap<List<Entry>> posMap = new Long2ObjectOpenHashMap<>();

    private VRXSavedData() {
    }

    private VRXSavedData(CompoundTag tag) {
        this.load(tag);
    }

    public static VRXSavedData get(Level level) {
        if (level instanceof ServerLevel serverLevel)
            return serverLevel.getDataStorage().computeIfAbsent(VRXSavedData::new, VRXSavedData::new, HYPERDAIMC + "_vrx");
        else
            return client.computeIfAbsent(level.dimension(), dimension -> new VRXSavedData());
    }

    public void removeIf(Predicate<Entry> predicate) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) return;
        for (Iterator<Entry> iterator = this.entries.iterator(); iterator.hasNext(); ) {
            Entry entry = iterator.next();
            if (predicate.test(entry)) {
                iterator.remove();
                this.posMap.computeIfPresent(entry.pos.asLong(), (p, e) -> {
                    e.remove(entry);
                    return e;
                });
            }
        }
    }

    public List<Entry> getEntries() {
        if (!HyperCommonConfig.ENABLE_VRX.get()) return Collections.emptyList();
        return this.entries;
    }

    public List<Entry> getEntries(BlockPos pos) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) return Collections.emptyList();
        return this.posMap.getOrDefault(pos.asLong(), Collections.emptyList());
    }

    public boolean create(UUID uuid, BlockPos pos, VRXMenu.Palette palette) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) return false;

        this.entries.removeIf(entry -> entry.matches(uuid, pos));
        this.posMap.computeIfPresent(pos.asLong(), (p, l) -> {
            l.removeIf(e -> e.uuid.equals(uuid));
            return l;
        });

        MutableBoolean success = new MutableBoolean();
        palette.forEach((face, ones) -> {
            if (!ones.isEmpty()) {
                Entry entry = new Entry(uuid, pos, face, ones);
                this.entries.add(entry);
                this.posMap.computeIfAbsent(pos.asLong(), p -> Lists.newArrayList()).add(entry);
                success.setTrue();
            }
        });

        this.setDirty();
        return success.booleanValue();
    }

    public boolean check(UUID uuid, BlockPos pos) {
        return this.entries.stream().anyMatch(entry -> entry.matches(uuid, pos));
    }

    public void erase(UUID uuid, BlockPos pos, Direction face) {
        MutableObject<Entry> fullMatched = new MutableObject<>();
        MutableObject<Entry> nullMatched = new MutableObject<>();
        MutableBoolean containsMatched = new MutableBoolean();

        for (Entry e : this.entries) {
            if (e.matches(uuid, pos)) {
                containsMatched.setTrue();
                if (e.face == null && nullMatched.getValue() == null)
                    nullMatched.setValue(e);

                if (e.face == face) {
                    fullMatched.setValue(e);
                    break;
                }
            }
        }

        if (fullMatched.getValue() != null) {
            this.entries.remove(fullMatched.getValue());
            this.posMap.computeIfPresent(pos.asLong(), (p, l) -> {
                l.remove(fullMatched.getValue());
                return l;
            });
            this.setDirty();
            return;
        }

        if (nullMatched.getValue() != null) {
            this.entries.remove(nullMatched.getValue());
            this.posMap.computeIfPresent(pos.asLong(), (p, l) -> {
                l.remove(nullMatched.getValue());
                return l;
            });
            this.setDirty();
            return;
        }

        if (containsMatched.booleanValue()) {
            this.entries.removeIf(entry -> entry.matches(uuid, pos));
            this.posMap.computeIfPresent(pos.asLong(), (p, l) -> {
                l.removeIf(entry -> entry.matches(uuid, pos));
                return l;
            });
            this.setDirty();
        }
    }

    public void erase(Entry entry) {
        this.entries.remove(entry);
        this.posMap.computeIfPresent(entry.pos.asLong(), (p, e) -> {
            e.remove(entry);
            return e;
        });
    }

    public void sync2Client(PacketDistributor.PacketTarget target) {
        HyperConnection.INSTANCE.send(target, new ClientboundVRXSyncSave(this.save(new CompoundTag())));
    }

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        ListTag entries = new ListTag();
        for (Entry entry : this.entries)
            entries.add(entry.save());

        pCompoundTag.put("Entries", entries);
        return pCompoundTag;
    }

    public void load(CompoundTag tag) {
        this.entries.clear();
        this.posMap.clear();
        tag.getList("Entries", Tag.TAG_COMPOUND).stream()
                .map(CompoundTag.class::cast)
                .map(Entry::load)
                .filter(entry -> !entry.contents.isEmpty())
                .forEach(entry -> {
                    this.entries.add(entry);
                    this.posMap.computeIfAbsent(entry.pos.asLong(), p -> Lists.newArrayList()).add(entry);
                });
    }

    public static class Entry {
        public final UUID uuid;
        public final BlockPos pos;
        @Nullable
        public final Direction face;
        public final List<VRXOne> contents;

        // Render.
        public final float xRot;
        public final float yRot;
        //.

        private Entry(UUID uuid, BlockPos pos, @Nullable Direction face, List<VRXOne> contents) {
            this.uuid = uuid;
            this.pos = pos;
            this.face = face;
            this.contents = ImmutableList.copyOf(contents);

            if (face == null) {
                this.xRot = 0;
                this.yRot = 0;
            } else {
                if (face.getAxis().isHorizontal()) {
                    this.xRot = 0;
                    this.yRot = (float) (face.get2DDataValue() * 90);
                } else {
                    this.xRot = (float) (-90 * face.getAxisDirection().getStep());
                    this.yRot = 0;
                }
            }
        }

        public static Entry load(CompoundTag tag) {
            UUID uuid = tag.getUUID("UUID");
            BlockPos pos = BlockPos.of(tag.getLong("Pos"));

            Direction face;
            if (tag.contains("Face"))
                face = Direction.valueOf(tag.getString("Face"));
            else
                face = null;


            List<VRXOne> contents = tag.getList("Contents", Tag.TAG_COMPOUND).stream()
                    .map(CompoundTag.class::cast)
                    .map(VRXType::deserializeStatic)
                    .filter(one -> !one.isEmpty())
                    .toList();

            return new Entry(uuid, pos, face, contents);
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("UUID", this.uuid);
            tag.putLong("Pos", this.pos.asLong());

            if (this.face != null)
                tag.putString("Face", this.face.name());


            ListTag list = new ListTag();
            for (VRXOne one : this.contents)
                list.add(one.serialize());

            tag.put("Contents", list);
            return tag;
        }

        public boolean matches(UUID uuid, BlockPos pos) {
            return this.uuid.equals(uuid) && this.pos.equals(pos);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return this.matches(entry.uuid, entry.pos) && face == entry.face;
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, pos, face);
        }
    }
}
