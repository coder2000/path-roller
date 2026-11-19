package ca.dieterlunn.trailblazer.paving;

import ca.dieterlunn.trailblazer.Trailblazer;
import ca.dieterlunn.trailblazer.TrailblazerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/** Turns rollable blocks inside a {@link PaveFootprint} into dirt paths. */
public final class PathPaver {
    /** Blocks a roller can flatten. Defaults mirror what a shovel flattens; extend it with a datapack. */
    public static final TagKey<Block> ROLLABLE =
            TagKey.create(Registries.BLOCK, new ResourceLocation(Trailblazer.MOD_ID, "rollable"));

    private PathPaver() {
    }

    /**
     * Paves one stamp around {@code footing}.
     *
     * @return the number of blocks turned into dirt path
     */
    public static int pave(ServerLevel level, Player player, ItemStack roller, BlockPos footing, PaveDirection dir, int width) {
        TrailblazerConfig config = TrailblazerConfig.get();
        int converted = 0;
        for (int[] offset : PaveFootprint.of(dir, width)) {
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
            level.destroyBlock(abovePos, false, player);
        }

        BlockState path = Blocks.DIRT_PATH.defaultBlockState();
        level.setBlock(pos, path, Block.UPDATE_ALL_IMMEDIATE);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, path));
        return true;
    }

    private static boolean isClearablePlant(BlockState state) {
        return !state.isAir() && state.canBeReplaced() && state.getFluidState().isEmpty();
    }
}
