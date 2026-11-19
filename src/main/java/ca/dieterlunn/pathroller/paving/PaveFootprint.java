package ca.dieterlunn.pathroller.paving;

import java.util.ArrayList;
import java.util.List;

/**
 * Which columns a roller of a given width covers, relative to the player's footing.
 *
 * <p>Cardinal paths are exactly {@code width} blocks wide. A diagonal path is built from diagonal "bands" of
 * blocks; neighbouring bands are only 1/sqrt(2) apart, so a diagonal path uses about {@code width * sqrt(2)}
 * bands to look as wide as a straight one, and never fewer than two so its blocks share edges and can be walked on.
 */
public final class PaveFootprint {
    private PaveFootprint() {
    }

    /** Horizontal offsets (x, z) from the player's footing that one roller stamp covers. */
    public static List<int[]> of(PaveDirection dir, int width) {
        List<int[]> offsets = new ArrayList<>();
        if (!dir.isDiagonal()) {
            int perpX = -dir.dz;
            int perpZ = dir.dx;
            int low = -(width - 1) / 2;
            for (int side = low; side < low + width; side++) {
                // The block under the player and the one just ahead, so fast movement leaves no gaps.
                for (int along = 0; along <= 1; along++) {
                    offsets.add(new int[]{dir.dx * along + perpX * side, dir.dz * along + perpZ * side});
                }
            }
            return offsets;
        }

        int bands = diagonalBands(width);
        int low = -(bands - 1) / 2;
        int high = low + bands - 1;
        int radius = bands + 2;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int along = x * dir.dx + z * dir.dz;
                int side = x * dir.dz - z * dir.dx;
                if (side >= low && side <= high && along >= -1 && along <= 2) {
                    offsets.add(new int[]{x, z});
                }
            }
        }
        return offsets;
    }

    static int diagonalBands(int width) {
        return Math.max(2, Math.round(width * (float) Math.sqrt(2)));
    }
}
