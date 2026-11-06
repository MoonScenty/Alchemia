package me.moonscenty.alchemia.block.taint;

import java.util.EnumMap;
import java.util.Map;

import me.moonscenty.alchemia.AlchemiaConfig;
import me.moonscenty.alchemia.aura.AuraHandler;
import me.moonscenty.alchemia.entity.TaintCrawler;
import me.moonscenty.alchemia.registry.ModAspects;
import me.moonscenty.alchemia.registry.ModBlocks;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// the original's swollen taint: a pod that leaves turn into beside a tainted log, pointing away from what holds it
public class TaintFeatureBlock extends Block implements Tainted {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Map.of(
            Direction.DOWN, Block.box(2, 10, 2, 14, 16, 14),
            Direction.UP, Block.box(2, 0, 2, 14, 6, 14),
            Direction.NORTH, Block.box(2, 2, 10, 14, 14, 16),
            Direction.SOUTH, Block.box(2, 2, 0, 14, 14, 6),
            Direction.WEST, Block.box(10, 2, 2, 16, 14, 14),
            Direction.EAST, Block.box(0, 2, 2, 6, 14, 14)));

    // original's values
    private static final float CRAWLER_CHANCE = 0.333F;
    private static final int DIE_CHANCE = 10;
    private static final int BREATHES_ONE_IN = 200;
    private static final int GEYSER_ONE_IN = 100;

    public TaintFeatureBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public static BlockState facing(Direction facing) {
        return ModBlocks.TAINT_FEATURE.get().defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void die(ServerLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, TaintDustBlock.of(ModBlocks.TAINT_DUST.get(), level.random.nextInt(2)), Block.UPDATE_ALL);
        TaintSpread.heal(level, pos, level.random, 25);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (TaintSpread.starved(level, pos) && random.nextInt(DIE_CHANCE) == 0) {
            die(level, pos, state);
            return;
        }
        if (AuraHandler.get(level, pos, ModAspects.FLUX) <= TaintSpread.SPREADS_ABOVE
                && random.nextInt(BREATHES_ONE_IN) == 0) {
            AuraHandler.add(level, pos, ModAspects.FLUX, 1);
            return;
        }
        TaintSpread.spread(level, pos, random);
        if (!level.getBlockState(pos).is(this)) {
            return;
        }
        BlockState under = level.getBlockState(pos.below());
        if (under.is(ModBlocks.TAINT_LOG.get())
                && under.getOptionalValue(RotatedPillarBlock.AXIS).orElse(Direction.Axis.Y) == Direction.Axis.Y
                && random.nextInt(GEYSER_ONE_IN) == 0) {
            if (random.nextFloat() < AlchemiaConfig.TAINT_SPREAD_COST.get() * 5.0F) {
                AuraHandler.drainAvailable(level, pos, ModAspects.FLUX, 1);
            }
            level.setBlock(pos, ModBlocks.TAINT_GEYSER.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    // bursting lets out a crawler a third of the time, otherwise a point of flux
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (level instanceof ServerLevel served && !replacement.is(this)) {
            if (served.random.nextFloat() < CRAWLER_CHANCE) {
                TaintCrawler.spawnAt(served, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            } else {
                AuraHandler.add(served, pos, ModAspects.FLUX, 1);
            }
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    // torn off what it grew on, it drops as goo
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        BlockPos holder = pos.relative(facing.getOpposite());
        if (direction == facing.getOpposite() && !level.getBlockState(holder).isFaceSturdy(level, holder, facing)) {
            if (level instanceof ServerLevel served) {
                Block.popResource(served, pos, new ItemStack(ModItems.TAINTED_GOO.get()));
            }
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
