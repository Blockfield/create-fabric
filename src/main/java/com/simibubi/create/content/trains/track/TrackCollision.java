package com.simibubi.create.content.trains.track;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin track beds, split between occupied blocks; no columns down to the ground beneath a bridge.
 */
final class TrackCollision {
    private static final double STEP = 0.25;

    static Map<BlockPos, List<AABB>> curve(BezierConnection connection) {
        Map<BlockPos, List<AABB>> boxes = new HashMap<>();
        Vec3 origin = Vec3.atLowerCornerOf(connection.bePositions.getFirst());
        double halfWidth = connection.getMaterial().trackType.isWideGauge() ? 1.75 : 1.25;
        Vec3 previous = null;
        Vec3 previousNormal = null;
        for (BezierConnection.Segment segment : connection) {
            Vec3 position = segment.position.add(origin);
            if (previous != null) {
                int steps = Math.max(1, Mth.ceil(previous.distanceTo(position) / STEP));
                for (int i = 0; i < steps; i++) {
                    double t = (i + 0.5) / steps;
                    bed(
                            boxes,
                            previous.lerp(position, t),
                            previousNormal.lerp(segment.normal, t).normalize(),
                            halfWidth);
                }
            }
            previous = position;
            previousNormal = segment.normal;
        }
        return boxes;
    }

    static void bed(Map<BlockPos, List<AABB>> boxes, Vec3 center, Vec3 normal, double halfWidth) {
        for (double across = -halfWidth + STEP / 2; across < halfWidth; across += STEP) {
            Vec3 point = center.add(normal.scale(across));
            // Quantize outwards to the model's 1/16 grid so adjacent samples join without cracks.
            add(
                    boxes,
                    new AABB(
                            Math.floor((point.x - STEP / 2) * 16) / 16,
                            Math.floor((point.y - 3.0 / 16) * 16) / 16,
                            Math.floor((point.z - STEP / 2) * 16) / 16,
                            Math.ceil((point.x + STEP / 2) * 16) / 16,
                            Math.ceil((point.y + 2.0 / 16) * 16) / 16,
                            Math.ceil((point.z + STEP / 2) * 16) / 16));
        }
    }

    private static void add(Map<BlockPos, List<AABB>> boxes, AABB box) {
        for (int x = Mth.floor(box.minX); x < Math.ceil(box.maxX); x++)
            for (int y = Mth.floor(box.minY); y < Math.ceil(box.maxY); y++)
                for (int z = Mth.floor(box.minZ); z < Math.ceil(box.maxZ); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    AABB local = box.intersect(new AABB(pos)).move(-x, -y, -z);
                    boxes.computeIfAbsent(pos, ignored -> new ArrayList<>()).add(local);
                }
    }
}
