package ca.dieterlunn.pathroller.paving;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathPaverTest {
    private static Set<Long> cells(List<int[]> offsets) {
        Set<Long> set = new HashSet<>();
        for (int[] o : offsets) {
            set.add(key(o[0], o[1]));
        }
        return set;
    }

    private static long key(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    /** Edge-connected (4-neighbour), i.e. walkable without stepping across a corner. */
    private static boolean isEdgeConnected(Set<Long> set) {
        long start = set.iterator().next();
        Set<Long> seen = new HashSet<>(List.of(start));
        ArrayDeque<Long> queue = new ArrayDeque<>(List.of(start));
        while (!queue.isEmpty()) {
            long k = queue.poll();
            int x = (int) (k >> 32);
            int z = (int) k;
            for (long n : new long[]{key(x + 1, z), key(x - 1, z), key(x, z + 1), key(x, z - 1)}) {
                if (set.contains(n) && seen.add(n)) {
                    queue.add(n);
                }
            }
        }
        return seen.size() == set.size();
    }

    @ParameterizedTest
    @EnumSource(PaveDirection.class)
    void footprintIsWalkableAndContainsFooting(PaveDirection dir) {
        for (int width = 1; width <= 15; width++) {
            Set<Long> set = cells(PathPaver.footprint(dir, width));
            assertTrue(set.contains(key(0, 0)), dir + " width " + width + " must cover the footing");
            assertTrue(isEdgeConnected(set), dir + " width " + width + " must be edge-connected");
        }
    }

    @Test
    void cardinalFootprintIsExactlyWidthWide() {
        for (int width = 1; width <= 15; width++) {
            Set<Long> set = cells(PathPaver.footprint(PaveDirection.NORTH, width));
            assertEquals(width * 2, set.size());
            long across = set.stream().filter(k -> (int) (long) k == 0).count();
            assertEquals(width, across);
        }
    }

    @Test
    void consecutiveDiagonalStampsLeaveNoGaps() {
        // Walk one block diagonally at a time and make sure every band stays continuous.
        for (int width = 1; width <= 7; width++) {
            Set<Long> path = new HashSet<>();
            for (int step = 0; step < 10; step++) {
                for (int[] o : PathPaver.footprint(PaveDirection.NORTH_EAST, width)) {
                    path.add(key(o[0] + step, o[1] - step));
                }
            }
            assertTrue(isEdgeConnected(path));
            // Every band crossing the middle of the walk must have a block at each diagonal step.
            int bands = PathPaver.diagonalBands(width);
            int low = -(bands - 1) / 2;
            for (int side = low; side < low + bands; side++) {
                for (int along = 4; along <= 14; along++) {
                    if (Math.floorMod(along + side, 2) != 0) {
                        continue;
                    }
                    // Solve x*dx + z*dz = along, x*dz - z*dx = side for NE (dx=1, dz=-1).
                    int x = (along - side) / 2;
                    int z = -(along + side) / 2;
                    assertTrue(path.contains(key(x, z)), "gap at band " + side + ", along " + along + ", width " + width);
                }
            }
        }
    }

    @Test
    void yawSnapsToEightDirections() {
        assertEquals(PaveDirection.SOUTH, PaveDirection.fromYaw(10, true));
        assertEquals(PaveDirection.SOUTH_WEST, PaveDirection.fromYaw(40, true));
        assertEquals(PaveDirection.WEST, PaveDirection.fromYaw(90, true));
        assertEquals(PaveDirection.NORTH, PaveDirection.fromYaw(-180, true));
        assertEquals(PaveDirection.NORTH_EAST, PaveDirection.fromYaw(-135, true));
        assertEquals(PaveDirection.EAST, PaveDirection.fromYaw(-90, true));
        assertEquals(PaveDirection.SOUTH_EAST, PaveDirection.fromYaw(-50, true));
        assertEquals(PaveDirection.SOUTH, PaveDirection.fromYaw(350, true));
    }

    @Test
    void yawSnapsToCardinalsWhenDiagonalsDisabled() {
        assertEquals(PaveDirection.SOUTH, PaveDirection.fromYaw(40, false));
        assertEquals(PaveDirection.WEST, PaveDirection.fromYaw(50, false));
        assertEquals(PaveDirection.EAST, PaveDirection.fromYaw(-60, false));
        assertEquals(PaveDirection.SOUTH, PaveDirection.fromYaw(-20, false));
    }
}
