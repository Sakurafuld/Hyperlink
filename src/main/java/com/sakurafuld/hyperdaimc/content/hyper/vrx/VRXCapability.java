package com.sakurafuld.hyperdaimc.content.hyper.vrx;

import com.google.common.collect.Lists;
import com.sakurafuld.hyperdaimc.HyperCommonConfig;
import com.sakurafuld.hyperdaimc.network.HyperConnection;
import com.sakurafuld.hyperdaimc.network.vrx.ClientboundVRXSyncCapability;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.sakurafuld.hyperdaimc.infrastructure.Deets.HYPERDAIMC;
import static com.sakurafuld.hyperdaimc.infrastructure.Deets.identifier;

@AutoRegisterCapability
@Mod.EventBusSubscriber(modid = HYPERDAIMC)
public class VRXCapability implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<VRXCapability> TOKEN = CapabilityManager.get(new CapabilityToken<>() {
    });
    private final LazyOptional<VRXCapability> optional = LazyOptional.of(() -> this);


    private final LivingEntity entity;
    private final List<Entry> entries = Lists.newArrayList();

    public VRXCapability(LivingEntity entity) {
        this.entity = entity;
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity entity)
            event.addCapability(identifier("vrx"), new VRXCapability(entity));
    }

    public List<Entry> getEntries() {
        if (!HyperCommonConfig.ENABLE_VRX.get())
            return Collections.emptyList();
        return this.entries;
    }

    public boolean create(UUID uuid, VRXMenu.Palette palette) {
        if (!HyperCommonConfig.ENABLE_VRX.get()) return false;
        this.entries.removeIf(entry -> entry.uuid.equals(uuid));

        MutableBoolean success = new MutableBoolean();
        palette.forEach((face, ones) -> {
            if (!ones.isEmpty()) {
                Entry entry = new Entry(uuid, face, ones);
                this.entries.add(entry);
                this.entity.getPersistentData().putBoolean(VRXHandler.TAG_HAS_VRX, true);
                success.setTrue();
            }
//            if (this.addEntry(uuid, face, ones))
        });

//        if (this.addEntry(uuid, null, nulls)) success.setTrue();

        return success.booleanValue();
    }

//    private boolean addEntry(UUID uuid, Direction face, List<VRXOne> ones) {
//        if (!HyperCommonConfig.ENABLE_VRX.get()) return false;
//        if (!ones.isEmpty()) {
//            Entry entry = new Entry(uuid, face, ones);
//
//            this.entries.add(entry);
//            this.entity.getPersistentData().putBoolean(VRXHandler.TAG_HAS_VRX, true);
//            return true;
//        } else return false;
//    }

    public void erase(UUID uuid) {
        this.entries.removeIf(entry -> entry.uuid.equals(uuid));
        this.entity.getPersistentData().putBoolean(VRXHandler.TAG_HAS_VRX, !this.entries.isEmpty());
    }

    public boolean check(UUID uuid) {
        return this.entries.stream().anyMatch(entry -> entry.uuid.equals(uuid));
    }

    public void sync2Client(int entity, PacketDistributor.PacketTarget target) {
        HyperConnection.INSTANCE.send(target, new ClientboundVRXSyncCapability(entity, this.serializeNBT()));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == TOKEN && HyperCommonConfig.ENABLE_VRX.get() ? this.optional.cast() : LazyOptional.empty();
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
        this.entity.getPersistentData().putBoolean(VRXHandler.TAG_HAS_VRX, !this.entries.isEmpty());
    }

    public record Entry(UUID uuid, @Nullable Direction face, List<VRXOne> contents) {
        public static Entry load(CompoundTag tag) {
            UUID uuid = tag.getUUID("UUID");
            Direction face;
            if (tag.contains("Face"))
                face = Direction.valueOf(tag.getString("Face"));
            else face = null;

            List<VRXOne> contents = tag.getList("Contents", Tag.TAG_COMPOUND).stream()
                    .map(CompoundTag.class::cast)
                    .map(VRXType::deserializeStatic)
                    .filter(one -> !one.isEmpty())
                    .toList();
            return new Entry(uuid, face, contents);
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("UUID", this.uuid);

            if (this.face != null)
                tag.putString("Face", this.face.name());

            ListTag list = new ListTag();
            for (VRXOne one : this.contents)
                list.add(one.serialize());

            tag.put("Contents", list);
            return tag;
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
    }
}
