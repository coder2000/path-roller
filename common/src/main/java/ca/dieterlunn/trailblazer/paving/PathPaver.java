package ca.dieterlunn.trailblazer.paving;

import ca.dieterlunn.trailblazer.Trailblazer;
import ca.dieterlunn.trailblazer.TrailblazerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Works out which columns a roller of a given width covers, and turns rollable blocks in them into dirt paths.
 *
 * <p>Cardinal paths are exactly {@code width} blocks wide. A diagonal path is built from diagonal "bands" of
 * blocks; neighbouring bands are only 1/sqrt(2) apart, so a diagonal path uses about {@code width * sqrt(2)}
 * bands to look as wide as a straight one, and never fewer than two so its blocks share edges and can be walked on.
 */
public final class PathPaver {
    /** Blocks a roller can flatten. Defaults mirror what a shovel flattens; extend it with a datapack. */
    public static final TagKey<Block> ROLLABLE =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Trailblazer.MOD_ID, "rollable"));
    /**
     * Plants the roller clears (dropping them as items) even though they aren't replaceable like grass is.
     * Defaults to small and tall flowers; extend it with a datapack.
     */
    public static final TagKey<Block> CLEARABLE =
            TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Trailblazer.MOD_ID, "clearable"));

    private PathPaver() {
    }

    /** Horizontal offsets (x, z) from the player's footing that one roller stamp covers. */
    public static List<int[]> footprint(PaveDirection dir, int width) {
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

    /**
     * Paves one stamp around {@code footing}.
     *
     * @return the number of blocks turned into dirt path
     */
    public static int pave(ServerLevel level, Player player, ItemStack roller, BlockPos footing, PaveDirection dir, int width) {
        TrailblazerConfig config = TrailblazerConfig.get();
        int converted = 0;
        for (int[] offset : footprint(dir, width)) {
            BlockPos column = footing.offset(offset[0], 0, offset[1]);
            if (paveColumn(level, player, roller, column, config)) {
                converted++;
            }
        }
        return converted;
    }

    private static boolean paveColumn(ServerLevel level, Player player, ItemStack roller, BlockPos column, TrailblazerConfig config) {
        // Scan from the top of the reach down to the first solid surface, so paths follow gentle slopes.
        for (int dy = config.verticalReach; dy >= -config.verticalReach; dy--) {
            BlockPos pos = column.above(dy);
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || isClearablePlant(state)) {
                continue;
            }
            return state.is(ROLLABLE) && flatten(level, player, roller, pos, config);
        }
        return false;
    }

    private static boolean flatten(ServerLevel level, Player player, ItemStack roller, BlockPos pos, TrailblazerConfig config) {
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, Direction.UP, roller)) {
            return false;
        }
        BlockPos abovePos = pos.above();
        BlockState above = level.getBlockState(abovePos);
        if (!above.isAir()) {
            if (!config.clearPlants || !isClearablePlant(above) || !level.mayInteract(player, abovePos)) {
                return false;
            }
            // Grass and the like vanish quietly; tagged plants such as flowers are dropped so they aren't lost.
            level.destroyBlock(abovePos, above.is(CLEARABLE), player);
        }

        BlockState path = Blocks.DIRT_PATH.defaultBlockState();
        level.setBlock(pos, path, Block.UPDATE_ALL_IMMEDIATE);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, path));
        return true;
    }

    private static boolean isClearablePlant(BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty() && (state.canBeReplaced() || state.is(CLEARABLE));
    }
}
