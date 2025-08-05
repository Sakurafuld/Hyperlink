package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ClientboundVRXSyncSave;
import com.sakurafuld.hyperdaimc.network.vrx.ServerboundVRXSyncSave;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

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

    public void create(UUID uuid, BlockPos pos, Direction face, List<VRXOne> list) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) {
            return;
        }
        Entry entry = new Entry(uuid, pos, face, list);
        int index = this.entries.indexOf(entry);
        if (index >= 0) {
            Entry old = this.entries.get(index);
            this.entries.remove(index);
            Entry finalEntry = entry;
            this.map.computeIfPresent(pos.asLong(), (p, e) -> {
                e.remove(finalEntry);
                return e;
            });
            entry = new Entry(uuid, pos, face, Util.make(Lists.newArrayList(old.contents), contents -> contents.addAll(list)));
        }

        this.entries.add(entry);

        Entry finalEntry = entry;
        this.map.compute(pos.asLong(), (p, e) -> {
            if (e == null) {
                e = Lists.newArrayList();
            }
            e.add(finalEntry);
            return e;
        });
        this.setDirty();
    }

    public boolean erase(UUID uuid, BlockPos pos, Direction face) {
        Entry entry = new Entry(uuid, pos, face, Collections.emptyList());
        boolean erased = this.entries.remove(entry);
        if (erased) {
            this.map.computeIfPresent(pos.asLong(), (p, e) -> {
                e.remove(entry);
                return e;
            });
        } else {
            for (Entry entry1 : Lists.newArrayList(this.entries)) {
                if (entry1.uuid.equals(uuid) && entry1.pos.equals(pos)) {
                    this.entries.remove(entry1);
                    this.map.computeIfPresent(entry1.pos.asLong(), (p, e) -> {
                        e.remove(entry1);
                        return e;
                    });
                    erased = true;
                }
            }
        }

        if (erased) {
            this.setDirty();
        }
        return erased;
    }

    public void erase(Entry entry) {
        this.entries.remove(entry);
        this.map.computeIfPresent(entry.pos.asLong(), (p, e) -> {
            e.remove(entry);
            return e;
        });
    }

    public void sync2Client(ServerPlayer player) {
        HyperConnection.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundVRXSyncSave(this.save(new CompoundTag())));
    }

    public void sync2Client(Supplier<ResourceKey<Level>> dimension) {
        HyperConnection.INSTANCE.send(PacketDistributor.DIMENSION.with(dimension), new ClientboundVRXSyncSave(this.save(new CompoundTag())));
    }

    public void sync2Server() {
        HyperConnection.INSTANCE.sendToServer(new ServerboundVRXSyncSave(this.save(new CompoundTag())));
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
                    this.map.compute(entry.pos.asLong(), (p, e) -> {
                        if (e == null) {
                            e = Lists.newArrayList();
                        }
                        e.add(entry);
                        return e;
                    });
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

        public Entry(UUID uuid, BlockPos pos, @Nullable Direction face, List<VRXOne> contents) {
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
