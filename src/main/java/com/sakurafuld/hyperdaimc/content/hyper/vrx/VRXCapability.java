package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ClientboundVRXSyncCapability;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.sakurafuld.hyperdaimc.helper.Deets.HYPERDAIMC;

@Mod.EventBusSubscriber(modid = HYPERDAIMC, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VRXCapability implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<VRXCapability> TOKEN = CapabilityManager.get(new CapabilityToken<>() {
    });
    private final LazyOptional<VRXCapability> optional = LazyOptional.of(() -> this);


    private final List<Entry> entries = Lists.newArrayList();

    public List<Entry> getEntries() {
        if (!HyperCommonConfig.ENABLE_VRX.get()) {
            return Collections.emptyList();
        }
        return this.entries;
    }

    public void create(UUID uuid, Direction face, List<VRXOne> list) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) {
            return;
        }
        Entry entry = new Entry(uuid, face, list);
        int index = this.entries.indexOf(entry);
        if (index >= 0) {
            Entry old = this.entries.get(index);
            this.entries.remove(index);
            entry = new Entry(uuid, face, Util.make(Lists.newArrayList(old.contents), contents -> contents.addAll(list)));
        }
        this.entries.add(entry);
    }

    public void erase(UUID uuid) {
        this.entries.removeIf(entry -> entry.uuid.equals(uuid));
    }

    public boolean check(UUID uuid) {
        return this.entries.stream().anyMatch(entry -> entry.uuid.equals(uuid));
    }

    public void sync2Client(int entity, PacketDistributor.PacketTarget target) {
        HyperConnection.INSTANCE.send(target, new ClientboundVRXSyncCapability(entity, this.serializeNBT()));
    }

    public static class Entry {
        public final UUID uuid;
        @Nullable
        public final Direction face;
        public final List<VRXOne> contents;

        private Entry(UUID uuid, @Nullable Direction face, List<VRXOne> contents) {
            this.uuid = uuid;
            this.face = face;
            this.contents = contents;
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("UUID", this.uuid);

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

            return new Entry(uuid, face, contents);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return Objects.equals(uuid, entry.uuid) && face == entry.face;
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, face);
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "UUID=" + uuid +
                    ", Face=" + face +
                    ", Contents=" + contents +
                    '}';
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == TOKEN ? this.optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag entries = new ListTag();
        for (Entry entry : this.entries) {
            entries.add(entry.save());
        }
        tag.put("Entries", entries);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.entries.clear();
        nbt.getList("Entries", Tag.TAG_COMPOUND).stream()
                .map(CompoundTag.class::cast)
                .map(Entry::load)
                .forEach(this.entries::add);
    }

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(VRXCapability.class);
    }
}
