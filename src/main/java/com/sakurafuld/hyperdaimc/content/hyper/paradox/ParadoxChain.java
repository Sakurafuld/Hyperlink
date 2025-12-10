package com.sakurafuld.hyperdaimc.content.hyper.paradox;

import com.google.common.collect.Queues;
import com.sakurafuld.hyperdaimc.content.hyper.paradox.system.ParadoxBomber;
import com.sakurafuld.hyperdaimc.infrastructure.Boxes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class ParadoxChain {
    private static final Direction[] DIRECTIONS = Direction.values();
    public final BlockPos from;
    public final BlockPos to;
    public final AABB aabb;
    private final Vec3 center;

    public ParadoxChain(BlockPos from, BlockPos to) {
        this.from = new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        this.to = new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        this.aabb = Boxes.of(this.from, this.to);
        this.center = this.aabb.getCenter();
    }

    public static Set<ParadoxChain> find(Collection<ParadoxChain> chains, ParadoxChain start) {
        ObjectOpenHashSet<ParadoxChain> found = new ObjectOpenHashSet<>();
        ArrayDeque<ParadoxChain> frontier = Queues.newArrayDeque();
        ObjectOpenHashSet<ParadoxChain> visited = new ObjectOpenHashSet<>();

        frontier.add(start);
        visited.add(start);
        while (!frontier.isEmpty()) {
            ParadoxChain current = frontier.poll();
            found.add(current);

            for (ParadoxChain other : chains) {
                if (!current.aabb.intersects(other.aabb))
                    continue;
                if (visited.add(other))
                    frontier.add(other);
            }
        }

        return found;
    }

    public static List<BlockPos> connect(Set<ParadoxChain> found, BlockPos cursor, ServerPlayer player, boolean skipPaused) {
        ServerLevel level = player.serverLevel();
        ArrayDeque<BlockPos> next = new ArrayDeque<>();
        ObjectOpenHashSet<BlockPos> checked = new ObjectOpenHashSet<>();
        ObjectArrayList<BlockPos> connected = new ObjectArrayList<>();

        next.add(cursor);
        checked.add(cursor);
        connected.add(cursor);
        while (!next.isEmpty()) {
            ArrayDeque<BlockPos> currents = next;
            next = new ArrayDeque<>();

            for (BlockPos pos : currents) {
                for (Direction face : DIRECTIONS) {
                    BlockPos relative = pos.relative(face);
                    if (checked.add(relative)) {
                        Vec3 center = Vec3.atCenterOf(relative);
                        boolean contain = ParadoxBomber.canPerfectKnockout(player, level, relative, skipPaused);
                        for (ParadoxChain chain : found) {
                            if (!chain.aabb.contains(center))
                                continue;
                            next.add(relative);
                            if (contain) connected.add(relative);
                        }
                    }
                }
            }
        }

        if (!ParadoxBomber.canPerfectKnockout(player, level, cursor, skipPaused))
            connected.remove(cursor);
        return connected;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ParadoxChain) obj;
        return Objects.equals(this.from, that.from) &&
                Objects.equals(this.to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return "[From=" + this.from + "|To=" + this.to + ']';
    }

    public LongArrayTag serialize() {
        return new LongArrayTag(new long[]{this.from.asLong(), this.to.asLong()});
    }

    public ParadoxChain(LongArrayTag tag) {
        this(BlockPos.of(tag.get(0).getAsLong()), BlockPos.of(tag.get(1).getAsLong()));
    }
}
