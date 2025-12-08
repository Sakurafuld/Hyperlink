package com.sakurafuld.hyperdaimc.infrastructure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Boxes {
    public static final BlockPos INVALID = new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    private Boxes() {
    }

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

    public static BlockPos getCursor(Player player, boolean fluid, boolean clamp) {
        double reach = Math.max(player.getBlockReach(), player.getEntityReach());
        Vec3 eye = player.getEyePosition(1);
        Vec3 view = player.getViewVector(1);
        Vec3 point = eye.add(view.scale(reach));
        AABB aabb = player.getBoundingBox().expandTowards(view.scale(reach)).inflate(1);

        HitResult picked = player.pick(reach, 1, fluid);

        double max = Mth.square(reach);
        if (picked.getType() != HitResult.Type.MISS) max = picked.getLocation().distanceToSqr(eye);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player, eye, point, aabb, e -> !e.isSpectator() && e.isPickable(), max);

        if (entityHit == null && picked.getType() == HitResult.Type.MISS) {
            BlockPos cursor = BlockPos.containing(eye.add(view.scale(4)));
            return clamp ? clamp(player.level(), cursor) : cursor;
        }

        if (entityHit != null && entityHit.getType() != HitResult.Type.MISS)
            return BlockPos.containing(entityHit.getLocation());

        return ((BlockHitResult) picked).getBlockPos().immutable();
    }

    public static BlockPos rotate(BlockPos center, BlockPos target, Vector4f rotation, boolean normalize) {
        int dx = target.getX() - center.getX();
        int dy = target.getY() - center.getY();
        int dz = target.getZ() - center.getZ();
        Vector3f delta = new Vector3f(dx, dy, dz);

        Vector3f axis = new Vector3f(rotation.x(), rotation.y(), rotation.z()).normalize();
        float cos = Mth.cos(rotation.w());
        float sin = Mth.sin(rotation.w());

        Vector3f rotated =
                new Vector3f(delta).mul(cos)
                        .add(new Vector3f(axis).cross(delta).mul(sin))
                        .add(new Vector3f(axis).mul(axis.dot(delta) * (1 - cos)));

        if (normalize)
            return new BlockPos(Math.round(rotated.x()), Math.round(rotated.y()), Math.round(rotated.z()));
        else
            return new BlockPos(Math.round(rotated.x()) + center.getX(), Math.round(rotated.y()) + center.getY(), Math.round(rotated.z()) + center.getZ());
    }

    public static Vector4f getRotation(Direction before, Direction after, Direction view) {
        if (before == after)
            return new Vector4f(before.getStepX(), before.getStepY(), before.getStepZ(), 0);

        if (before.getAxis() == after.getAxis()) {
            if (before.getAxis().isHorizontal())
                return new Vector4f(0, 1, 0, Mth.PI);
            else
                return new Vector4f(before.step().cross(view.step()), Mth.PI);
        }

        Vector3f a = before.step();
        Vector3f b = after.step();
        Vector3f axis = a.cross(b);
        return new Vector4f(axis, (float) Math.acos(a.dot(b)));
    }
}
