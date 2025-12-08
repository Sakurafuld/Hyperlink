package com.sakurafuld.hyperdaimc.content.hyper.paradox;

import com.google.common.collect.Maps;
import com.sakurafuld.hyperdaimc.infrastructure.Boxes;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import org.joml.Vector4f;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class ParadoxChainCluster {
    private final EnumMap<Direction, Set<ParadoxChain>> horizontalMap;
    private final EnumMap<Direction, EnumMap<Direction, Set<ParadoxChain>>> verticalHorizontalMap;
    private final EnumMap<Direction, Long2ObjectOpenHashMap<Set<ParadoxChain>>> horizontalOffsetMap = Maps.newEnumMap(Direction.class);
    private final EnumMap<Direction, EnumMap<Direction, Long2ObjectOpenHashMap<Set<ParadoxChain>>>> verticalHorizontalOffsetMap = Maps.newEnumMap(Direction.class);

    public ParadoxChainCluster(Set<ParadoxChain> chains, BlockPos basePos, Direction baseDirection, Direction viewDirection) {
        this.horizontalMap = Maps.newEnumMap(Direction.class);
        this.verticalHorizontalMap = Maps.newEnumMap(Direction.class);

        EnumMap<Direction, Set<ParadoxChain>> verticalMap = Maps.newEnumMap(Direction.class);
        if (baseDirection.getAxis().isHorizontal()) {
            spin(Direction.Plane.HORIZONTAL, this.horizontalMap, chains, basePos, baseDirection, viewDirection);
            spin(Direction.Plane.VERTICAL, verticalMap, chains, basePos, baseDirection, viewDirection);
            for (Map.Entry<Direction, Set<ParadoxChain>> entry : verticalMap.entrySet()) {
                EnumMap<Direction, Set<ParadoxChain>> map = Maps.newEnumMap(Direction.class);
                spin(Direction.Plane.HORIZONTAL, map, entry.getValue(), BlockPos.ZERO, viewDirection, viewDirection);
                this.verticalHorizontalMap.put(entry.getKey(), map);
            }
        } else {
            Set<ParadoxChain> viewChains = spin(chains, basePos, baseDirection, viewDirection, viewDirection);
            spin(Direction.Plane.HORIZONTAL, this.horizontalMap, viewChains, BlockPos.ZERO, viewDirection, viewDirection);
            verticalMap.put(baseDirection, normalize(chains, basePos));
            verticalMap.put(baseDirection.getOpposite(), spin(chains, basePos, baseDirection, baseDirection.getOpposite(), viewDirection));
            for (Map.Entry<Direction, Set<ParadoxChain>> entry : verticalMap.entrySet()) {
                EnumMap<Direction, Set<ParadoxChain>> map = Maps.newEnumMap(Direction.class);
                spin(Direction.Plane.HORIZONTAL, map, entry.getValue(), BlockPos.ZERO, viewDirection, viewDirection);
                this.verticalHorizontalMap.put(entry.getKey(), map);
            }
        }
    }

    private static Set<ParadoxChain> normalize(Set<ParadoxChain> chains, BlockPos center) {
        ObjectOpenHashSet<ParadoxChain> normalized = new ObjectOpenHashSet<>(chains.size());
        for (ParadoxChain chain : chains) {
            BlockPos from = chain.from.subtract(center);
            BlockPos to = chain.to.subtract(center);
            normalized.add(new ParadoxChain(from, to));
        }
        return normalized;
    }

    public Set<ParadoxChain> get(BlockPos at, Direction direction, Direction view) {
        Set<ParadoxChain> base;
        Long2ObjectOpenHashMap<Set<ParadoxChain>> posMap;

        if (direction.getAxis().isHorizontal()) {
            base = this.horizontalMap.get(direction);
            posMap = this.horizontalOffsetMap.computeIfAbsent(direction, d -> new Long2ObjectOpenHashMap<>());
        } else {
            base = this.verticalHorizontalMap.get(direction).get(view);
            EnumMap<Direction, Long2ObjectOpenHashMap<Set<ParadoxChain>>> horizontalOffsetMap = this.verticalHorizontalOffsetMap.computeIfAbsent(direction, d -> Maps.newEnumMap(Direction.class));
            posMap = horizontalOffsetMap.computeIfAbsent(view, d -> new Long2ObjectOpenHashMap<>());
        }

        if (base == null)
            return Collections.emptySet();

        Set<ParadoxChain> chains = posMap.get(at.asLong());
        if (chains != null)
            return chains;

        chains = new ObjectOpenHashSet<>();
        for (ParadoxChain chain : base)
            chains.add(new ParadoxChain(chain.from.offset(at), chain.to.offset(at)));
        posMap.put(at.asLong(), chains);
        return chains;
    }

    private static Set<ParadoxChain> spin(Set<ParadoxChain> chains, BlockPos basePos, Direction before, Direction after, Direction view) {
        Set<ParadoxChain> set = new ObjectOpenHashSet<>();
        Vector4f rotation = Boxes.getRotation(before, after, view);
        for (ParadoxChain chain : chains) {
            BlockPos min = Boxes.rotate(basePos, chain.from, rotation, true);
            BlockPos max = Boxes.rotate(basePos, chain.to, rotation, true);
            set.add(new ParadoxChain(min, max));
        }
        return set;
    }

    private static void spin(Iterable<Direction> directions, EnumMap<Direction, Set<ParadoxChain>> map, Set<ParadoxChain> chains, BlockPos basePos, Direction baseDirection, Direction viewDirection) {
        for (Direction dir : directions)
            map.put(dir, spin(chains, basePos, baseDirection, dir, viewDirection));
    }

//    public void write(FriendlyByteBuf buf) {
//        buf.writeMap(this.horizontalMap,
//                FriendlyByteBuf::writeEnum,
//                ParadoxChainCluster::writeChains);
//        buf.writeMap(this.verticalHorizontalMap,
//                FriendlyByteBuf::writeEnum,
//                (buf1, map) ->
//                        buf.writeMap(map,
//                                FriendlyByteBuf::writeEnum,
//                                ParadoxChainCluster::writeChains));
//    }
//
//    private static void writeChains(FriendlyByteBuf buf, Set<ParadoxChain> chains) {
//        buf.writeCollection(chains, (buf1, chain) -> chain.write(buf1));
//    }
//
//    public ParadoxChainCluster(FriendlyByteBuf buf) {
//        this.horizontalMap = buf.readMap(i -> Maps.newEnumMap(Direction.class),
//                buf1 -> buf1.readEnum(Direction.class),
//                ParadoxChainCluster::readChains);
//        this.verticalHorizontalMap = buf.readMap(i -> Maps.newEnumMap(Direction.class),
//                buf1 -> buf1.readEnum(Direction.class),
//                buf1 -> buf1.readMap(i -> Maps.newEnumMap(Direction.class),
//                        buf2 -> buf2.readEnum(Direction.class),
//                        ParadoxChainCluster::readChains));
//    }
//
//    private static Set<ParadoxChain> readChains(FriendlyByteBuf buf) {
//        return buf.readCollection(ObjectOpenHashSet::new, ParadoxChain::new);
//    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        ListTag horizontalMap = new ListTag();
        for (Map.Entry<Direction, Set<ParadoxChain>> entry : this.horizontalMap.entrySet()) {
            CompoundTag horizontal = new CompoundTag();
            horizontal.putInt("Direction", entry.getKey().get3DDataValue());
            horizontal.put("Chains", serializeChains(entry.getValue()));
            horizontalMap.add(horizontal);
        }
        tag.put("HorizontalMap", horizontalMap);

        ListTag verticalHorizontalMap = new ListTag();
        for (Map.Entry<Direction, EnumMap<Direction, Set<ParadoxChain>>> entry : this.verticalHorizontalMap.entrySet()) {
            CompoundTag vertical = new CompoundTag();
            vertical.putInt("Direction", entry.getKey().get3DDataValue());
            horizontalMap = new ListTag();
            for (Map.Entry<Direction, Set<ParadoxChain>> entry1 : entry.getValue().entrySet()) {
                CompoundTag horizontal = new CompoundTag();
                horizontal.putInt("Direction", entry1.getKey().get3DDataValue());
                horizontal.put("Chains", serializeChains(entry1.getValue()));
                horizontalMap.add(horizontal);
            }
            vertical.put("HorizontalMap", horizontalMap);
            verticalHorizontalMap.add(vertical);
        }
        tag.put("VerticalHorizontalMap", verticalHorizontalMap);
        return tag;
    }

    private static ListTag serializeChains(Set<ParadoxChain> chains) {
        ListTag tag = new ListTag();
        for (ParadoxChain chain : chains)
            tag.add(chain.serialize());
        return tag;
    }

    public ParadoxChainCluster(CompoundTag nbt) {
        this.horizontalMap = Maps.newEnumMap(Direction.class);
        this.verticalHorizontalMap = Maps.newEnumMap(Direction.class);

        for (Tag tag : nbt.getList("HorizontalMap", Tag.TAG_COMPOUND)) {
            CompoundTag compound = (CompoundTag) tag;
            Direction direction = Direction.from3DDataValue(compound.getInt("Direction"));
            Set<ParadoxChain> chains = deserializeChains(compound.getList("Chains", Tag.TAG_LONG_ARRAY));
            this.horizontalMap.put(direction, chains);
        }

        for (Tag tag : nbt.getList("VerticalHorizontalMap", Tag.TAG_COMPOUND)) {
            CompoundTag compound = (CompoundTag) tag;
            Direction vertical = Direction.from3DDataValue(compound.getInt("Direction"));
            EnumMap<Direction, Set<ParadoxChain>> horizontalMap = Maps.newEnumMap(Direction.class);
            for (Tag tag1 : compound.getList("HorizontalMap", Tag.TAG_COMPOUND)) {
                CompoundTag compound1 = (CompoundTag) tag1;
                Direction horizontal = Direction.from3DDataValue(compound1.getInt("Direction"));
                Set<ParadoxChain> chains = deserializeChains(compound1.getList("Chains", Tag.TAG_LONG_ARRAY));
                horizontalMap.put(horizontal, chains);
            }
            this.verticalHorizontalMap.put(vertical, horizontalMap);
        }
    }

    private static Set<ParadoxChain> deserializeChains(ListTag tags) {
        Set<ParadoxChain> chains = new ObjectOpenHashSet<>();
        for (Tag tag : tags)
            chains.add(new ParadoxChain((LongArrayTag) tag));
        return chains;
    }
}
