package ca.dieterlunn.pathroller.paving;

import net.minecraft.util.Mth;

/** The eight compass directions a path can run in, as unit steps on the X/Z grid. */
public enum PaveDirection {
    SOUTH(0, 1),
    SOUTH_WEST(-1, 1),
    WEST(-1, 0),
    NORTH_WEST(-1, -1),
    NORTH(0, -1),
    NORTH_EAST(1, -1),
    EAST(1, 0),
    SOUTH_EAST(1, 1);

    public final int dx;
    public final int dz;

    PaveDirection(int dx, int dz) {
        this.dx = dx;
        this.dz = dz;
    }

    public boolean isDiagonal() {
        return dx != 0 && dz != 0;
    }

    /**
     * Snaps a Minecraft yaw (0 = south, 90 = west) to the nearest direction.
     * With diagonals disabled only the four cardinal directions are returned.
     */
    public static PaveDirection fromYaw(float yaw, boolean allowDiagonals) {
        float wrapped = Mth.wrapDegrees(yaw);
        if (wrapped < 0) {
            wrapped += 360F;
        }
        if (allowDiagonals) {
            return values()[Math.round(wrapped / 45F) % 8];
        }
        return values()[(Math.round(wrapped / 90F) % 4) * 2];
    }
}
