package com.sakurafuld.hyperdaimc.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class Boxes {
    private Boxes() {
    }

    public static final BlockPos INVALID = new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    public static AABB identity(AABB aabb) {
        return new AABB(0, 0, 0, aabb.getXsize(), aabb.getYsize(), aabb.getZsize());
    }

    public static AABB of(BlockPos start, BlockPos end) {
        return new AABB(start, end).expandTowards(1, 1, 1);
    }

    public static AABB lerp(float delta, AABB aa, AABB bb) {
        double minX = Mth.lerp(delta, aa.minX, bb.minX);
        double minY = Mth.lerp(delta, aa.minY, bb.minY);
        double minZ = Mth.lerp(delta, aa.minZ, bb.minZ);
        double maxX = Mth.lerp(delta, aa.maxX, bb.maxX);
        double maxY = Mth.lerp(delta, aa.maxY, bb.maxY);
        double maxZ = Mth.lerp(delta, aa.maxZ, bb.maxZ);
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static BlockPos clamp(Level level, BlockPos pos) {
        if (pos.getY() >= level.getMaxBuildHeight()) {
            return new BlockPos(pos.getX(), level.getMaxBuildHeight() - 1, pos.getZ());
        } else if (level.getMinBuildHeight() > pos.getY()) {
            return new BlockPos(pos.getX(), level.getMinBuildHeight(), pos.getZ());
        } else {
            return pos;
        }
    }
}
