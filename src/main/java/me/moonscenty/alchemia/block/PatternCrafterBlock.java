package me.moonscenty.alchemia.block;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.PatternCrafterBlockEntity;
import me.moonscenty.alchemia.registry.ModBlockEntities;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The arcane pattern crafter, the original's {@code BlockPatternCrafter}: a hopper-topped box with a brass dial on
 * its face. It takes from the inventory above, lays what it took in the pattern the dial is set to, and puts what
 * that makes into the inventory below. A redstone signal stops it.
 * <p>
 * It is set down facing the way its placer looks, so its face, with the dial and the window showing the pattern,
 * is turned back to them. Turning the dial is a click on the dial itself; a click anywhere else does nothing.
 */
public class PatternCrafterBlock extends BaseEntityBlock {
    public static final MapCodec<PatternCrafterBlock> CODEC = simpleCodec(PatternCrafterBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** Working, which is to say not held off by redstone. */
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    /** The dial, in the block's own measures, for each way it can face. The original's. */
    private static final Map<Direction, AABB> DIALS = new EnumMap<>(Map.of(
            Direction.EAST, new AABB(0.125, 0.125, 0.375, 0.25, 0.375, 0.625),
            Direction.NORTH, new AABB(0.375, 0.125, 0.75, 0.625, 0.375, 0.875),
            Direction.SOUTH, new AABB(0.375, 0.125, 0.125, 0.625, 0.375, 0.25),
            Direction.WEST, new AABB(0.75, 0.125, 0.375, 0.875, 0.375, 0.625)));
    /** A little give round the dial, so a click on its edge still finds it. */
    private static final double GIVE = 1.0 / 32.0;

    public PatternCrafterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ENABLED, true));
    }

    @Override
    protected MapCodec<PatternCrafterBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection())
                .setValue(ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from,
            boolean moving) {
        boolean enabled = !level.hasNeighborSignal(pos);
        if (enabled != state.getValue(ENABLED)) {
            level.setBlock(pos, state.setValue(ENABLED, enabled), Block.UPDATE_ALL);
        }
    }

    /** Whether a point on the block, in its own measures, is on the dial. */
    public static boolean onDial(BlockState state, Vec3 local) {
        return DIALS.get(state.getValue(FACING)).inflate(GIVE).contains(local);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!onDial(state, hit.getLocation().subtract(Vec3.atLowerCornerWithOffset(pos, 0, 0, 0)))) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PatternCrafterBlockEntity crafter) {
            crafter.cycle();
            level.playSound(null, pos, ModSounds.KEY.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PatternCrafterBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.PATTERN_CRAFTER.get(), level.isClientSide
                ? PatternCrafterBlockEntity::clientTick
                : PatternCrafterBlockEntity::serverTick);
    }
}
