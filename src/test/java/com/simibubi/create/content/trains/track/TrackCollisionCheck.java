package com.simibubi.create.content.trains.track;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Run with ./gradlew trackCollisionCheck. */
public final class TrackCollisionCheck {
    public static void main(String[] args) {
        for (double width : new double[] {1.25, 1.75}) {
            Map<BlockPos, List<AABB>> boxes = new HashMap<>();
            TrackCollision.bed(boxes, new Vec3(-2.5, 10 + 3.0 / 16, 0.5), new Vec3(1, 0, 0), width);
            check(hit(boxes, new AABB(-2.8, 10, 0.2, -2.2, 10.6, 0.8)), "feet must hit track");
            check(
                    !hit(boxes, new AABB(-2.8, 10.4, 0.2, -2.2, 11, 0.8)),
                    "crawl space above rails stays open");
            check(
                    !hit(boxes, new AABB(-2.8, 8, 0.2, -2.2, 9.8, 0.8)),
                    "space below elevated track stays open");
            check(
                    hit(boxes, new AABB(-2.5 + width - 0.2, 10, 0.2, -2.5 + width, 10.3, 0.8)),
                    "outer rail has collision");
            for (List<AABB> local : boxes.values())
                for (AABB b : local)
                    check(
                            b.minX >= 0
                                    && b.minY >= 0
                                    && b.minZ >= 0
                                    && b.maxX <= 1
                                    && b.maxY <= 1
                                    && b.maxZ <= 1,
                            "collision must be local to its block");
        }
        System.out.println(
                "Track collision: solid rails, crawl clearance, bridge clearance, wide gauge and"
                        + " negative coordinates OK");
    }

    private static boolean hit(Map<BlockPos, List<AABB>> boxes, AABB entity) {
        return boxes.entrySet().stream()
                .anyMatch(
                        e ->
                                e.getValue().stream()
                                        .anyMatch(b -> b.move(e.getKey()).intersects(entity)));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
