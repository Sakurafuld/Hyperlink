package com.sakurafuld.hyperdaimc.content.hyper.vrx;

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
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

public class VRXSavedData extends SavedData {
    private static final Object2ObjectOpenHashMap<ResourceKey<Level>, VRXSavedData> client = new Object2ObjectOpenHashMap<>();

    private final List<Entry> entries = Lists.newArrayList();
    private final Long2ObjectOpenHashMap<List<Entry>> map = new Long2ObjectOpenHashMap<>();

    private VRXSavedData() {
    }

    private VRXSavedData(CompoundTag tag) {
        this.load(tag);
    }

    public static VRXSavedData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(VRXSavedData::new, VRXSavedData::new, HYPERDAIMC + "_vrx");
        } else {
            return client.computeIfAbsent(level.dimension(), dimension -> new VRXSavedData());
        }
    }

    public List<Entry> getEntries() {
        if (!HyperCommonConfig.ENABLE_VRX.get()) {
            return Collections.emptyList();
        }
        return this.entries;
    }

    public List<Entry> getEntries(BlockPos pos) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) {
            return Collections.emptyList();
        }

        return this.map.getOrDefault(pos.asLong(), Collections.emptyList());
    }

    public boolean create(UUID uuid, BlockPos pos, EnumMap<Direction, List<VRXOne>> map, List<VRXOne> nulls) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) {
            return false;
        }

        this.entries.removeIf(entry -> entry.uuid.equals(uuid) && entry.pos.equals(pos));
        this.map.computeIfPresent(pos.asLong(), (p, l) -> {
            l.removeIf(e -> e.uuid.equals(uuid));
            return l;
        });

        MutableBoolean success = new MutableBoolean(false);
        map.forEach((face, ones) -> {
            if (this.addEntry(uuid, pos, face, ones)) {
                success.setTrue();
            }
        });

        if (this.addEntry(uuid, pos, null, nulls)) {
            success.setTrue();
        }

        this.setDirty();
        return success.booleanValue();
    }

    private boolean addEntry(UUID uuid, BlockPos pos, @Nullable Direction face, List<VRXOne> ones) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) {
            return false;
        }

        if (!ones.isEmpty()) {
            Entry entry = new Entry(uuid, pos, face, ones);
            if (this.entries.remove(entry)) {
                this.map.computeIfPresent(pos.asLong(), (p, e) -> {
                    e.remove(entry);
                    return e;
                });
            }

            this.entries.add(entry);
            this.map.computeIfAbsent(pos.asLong(), p -> Lists.newArrayList()).add(entry);

            return true;
        } else {
            return false;
        }
    }

    public boolean check(UUID uuid, BlockPos pos, Direction face) {
        if (this.entries.contains(new Entry(uuid, pos, face, Collections.emptyList()))) {
            return true;
        } else {
            return this.entries.stream().anyMatch(entry -> entry.uuid.equals(uuid) && entry.pos.equals(pos));
        }
    }

    public void erase(UUID uuid, BlockPos pos, Direction face) {
        Entry entry = new Entry(uuid, pos, face, Collections.emptyList());
        MutableBoolean erased = new MutableBoolean(this.entries.remove(entry));
        if (erased.booleanValue()) {
            this.map.computeIfPresent(pos.asLong(), (p, e) -> {
                e.remove(entry);
                return e;
            });
        } else {
            MutableBoolean same = new MutableBoolean(false);
            this.entries.removeIf(next -> {
                if (next.uuid.equals(uuid) && next.pos.equals(pos)) {
                    same.setTrue();
                    if (next.face == null) {
                        this.map.computeIfPresent(next.pos.asLong(), (p, e) -> {
                            e.remove(next);
                            return e;
                        });
                        erased.setTrue();
                        return true;
                    }
                }

                return false;
            });

            if (!erased.booleanValue() && same.booleanValue()) {
                this.entries.removeIf(next -> {
                    if (next.uuid.equals(uuid) && next.pos.equals(pos)) {
                        this.map.computeIfPresent(next.pos.asLong(), (p, e) -> {
                            e.remove(next);
                            return e;
                        });
                        erased.setTrue();
                        return true;
                    } else {
                        return false;
                    }
                });
            }
        }

        if (erased.booleanValue()) {
            this.setDirty();
        }
    }

    public void erase(Entry entry) {
        this.entries.remove(entry);
        this.map.computeIfPresent(entry.pos.asLong(), (p, e) -> {
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
        for (Entry entry : this.entries) {
            entries.add(entry.save());
        }
        pCompoundTag.put("Entries", entries);
        return pCompoundTag;
    }

    public void load(CompoundTag tag) {
        this.entries.clear();
        this.map.clear();
        tag.getList("Entries", Tag.TAG_COMPOUND).stream()
                .map(CompoundTag.class::cast)
                .map(Entry::load)
                .forEach(entry -> {
                    this.entries.add(entry);
                    this.map.computeIfAbsent(entry.pos.asLong(), p -> Lists.newArrayList()).add(entry);
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
            this.contents = contents;

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

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("UUID", this.uuid);
            tag.putLong("Pos", this.pos.asLong());

            if (this.face != null) {
                tag.putString("Face", this.face.name());
            }

            ListTag list = new ListTag();
            for (VRXOne one : this.contents) {
                list.add(one.serialize());
            }
            tag.put("Contents", list);
            return tag;
        }

        public static Entry load(CompoundTag tag) {
            UUID uuid = tag.getUUID("UUID");
            BlockPos pos = BlockPos.of(tag.getLong("Pos"));

            Direction face;
            if (tag.contains("Face")) {
                face = Direction.valueOf(tag.getString("Face"));
            } else {
                face = null;
            }


            List<VRXOne> contents = tag.getList("Contents", Tag.TAG_COMPOUND).stream()
                    .map(CompoundTag.class::cast)
                    .map(nbt -> {
                        VRXOne.Type type = VRXOne.Type.of(nbt.getString("Type"));
                        return type.load(nbt.getCompound("Data"));
                    })
                    .toList();

            return new Entry(uuid, pos, face, contents);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return Objects.equals(uuid, entry.uuid) && Objects.equals(pos, entry.pos) && face == entry.face;
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, pos, face);
        }
    }
}
