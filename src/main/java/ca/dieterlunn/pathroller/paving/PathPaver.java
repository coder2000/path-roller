package ca.dieterlunn.pathroller.paving;

import ca.dieterlunn.pathroller.PathRoller;
import ca.dieterlunn.pathroller.PathRollerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/** Turns rollable blocks inside a {@link PaveFootprint} into dirt paths. */
public final class PathPaver {
    /** Blocks a roller can flatten. Defaults mirror what a shovel flattens; extend it with a datapack. */
    public static final TagKey<Block> ROLLABLE =
            TagKey.create(Registries.BLOCK, new ResourceLocation(PathRoller.MOD_ID, "rollable"));
    /**
     * Plants the roller clears (dropping them as items) even though they aren't replaceable like grass is.
     * Defaults to small and tall flowers; extend it with a datapack.
     */
    public static final TagKey<Block> CLEARABLE =
            TagKey.create(Registries.BLOCK, new ResourceLocation(PathRoller.MOD_ID, "clearable"));

    private PathPaver() {
    }

    /**
     * Paves one stamp around {@code footing}.
     *
     * @return the number of blocks turned into dirt path
     */
    public static int pave(ServerLevel level, Player player, ItemStack roller, BlockPos footing, PaveDirection dir, int width) {
        PathRollerConfig config = PathRollerConfig.get();
        int converted = 0;
        List<BlockPos> surfaces = new ArrayList<>();
        for (int[] offset : PaveFootprint.of(dir, width)) {
            BlockPos surface = findSurface(level, footing.offset(offset[0], 0, offset[1]), config);
            if (surface == null) {
                continue;
            }
            if (level.getBlockState(surface).is(ROLLABLE) && flatten(level, player, roller, surface, config)) {
                converted++;
            }
            surfaces.add(surface);
        }

        BlockState slab = config.smoothSlopes ? pathSlabState(config) : null;
        if (config.debugLogging) {
            PathRoller.LOGGER.info("[pave] footing={} dir={} width={} converted={} surfaces={} slab={}",
                    footing.toShortString(), dir, width, converted, describe(level, surfaces),
                    config.smoothSlopes ? (slab != null ? slab : "'" + config.pathSlab + "' not installed") : "disabled");
        }
        if (slab != null) {
            for (BlockPos surface : surfaces) {
                smoothAround(level, player, surface, slab);
            }
        }
        return converted;
    }

    /** Scans from the top of the reach down to the first solid surface, so paths follow gentle slopes. */
    private static BlockPos findSurface(ServerLevel level, BlockPos column, PathRollerConfig config) {
        for (int dy = config.verticalReach; dy >= -config.verticalReach; dy--) {
            BlockPos pos = column.above(dy);
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || isClearablePlant(state)) {
                continue;
            }
            return pos;
        }
        return null;
    }

    /**
     * Where this path block and a neighbouring path block differ in height by exactly one, turns the higher one
     * into a path slab so the path climbs in half steps.
     */
    private static void smoothAround(ServerLevel level, Player player, BlockPos pos, BlockState slab) {
        if (!level.getBlockState(pos).is(Blocks.DIRT_PATH)) {
            trace("[smooth] {} skipped: surface is {}, not a full dirt path", pos, level.getBlockState(pos));
            return;
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = pos.relative(side);
            if (level.getBlockState(neighbour.above()).is(Blocks.DIRT_PATH)) {
                trace("[smooth] {} has a path one block higher to the {} at {}", pos, side, neighbour.above());
                halve(level, player, neighbour.above(), slab);
            } else if (level.getBlockState(neighbour.below()).is(Blocks.DIRT_PATH)) {
                trace("[smooth] {} has a path one block lower to the {} at {}", pos, side, neighbour.below());
                halve(level, player, pos, slab);
                return;
            }
        }
    }

    private static void halve(ServerLevel level, Player player, BlockPos pos, BlockState slab) {
        if (!level.getBlockState(pos).is(Blocks.DIRT_PATH)) {
            trace("[halve] {} skipped: already {}", pos, level.getBlockState(pos));
            return;
        }
        BlockState above = level.getBlockState(pos.above());
        if (!above.isAir()) {
            trace("[halve] {} skipped: {} above it", pos, above);
            return;
        }
        if (!level.mayInteract(player, pos)) {
            trace("[halve] {} skipped: player may not build here", pos);
            return;
        }
        // Mid-staircase blocks stay whole: halving one would leave a step and a half up to the next block.
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(side).above()).is(Blocks.DIRT_PATH)) {
                trace("[halve] {} skipped: mid-staircase (path one higher to the {})", pos, side);
                return;
            }
        }
        level.setBlock(pos, slab, Block.UPDATE_ALL);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, slab));
        trace("[halve] {} turned into {}", pos, slab);
    }

    private static void trace(String message, Object... args) {
        if (PathRollerConfig.get().debugLogging) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof BlockPos p) {
                    args[i] = p.toShortString();
                }
            }
            PathRoller.LOGGER.info(message, args);
        }
    }

    private static String describe(ServerLevel level, List<BlockPos> surfaces) {
        StringBuilder out = new StringBuilder("[");
        for (BlockPos pos : surfaces) {
            if (out.length() > 1) {
                out.append(", ");
            }
            out.append(pos.toShortString()).append('=')
                    .append(BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getPath());
        }
        return out.append(']').toString();
    }

    /** The bottom-half path slab named in the config, or null if that block isn't installed. */
    private static BlockState pathSlabState(PathRollerConfig config) {
        ResourceLocation id = ResourceLocation.tryParse(config.pathSlab);
        if (id == null || !ForgeRegistries.BLOCKS.containsKey(id)) {
            return null;
        }
        BlockState state = ForgeRegistries.BLOCKS.getValue(id).defaultBlockState();
        if (state.hasProperty(SlabBlock.TYPE)) {
            state = state.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }

    private static boolean flatten(ServerLevel level, Player player, ItemStack roller, BlockPos pos, PathRollerConfig config) {
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
