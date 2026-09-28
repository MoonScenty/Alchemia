package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.AlembicBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A vessel that catches essentia rising out of a smelter, and stacks with others of its kind.
 * <p>
 * Its legs are not part of the vessel but of the stand it sits on, so a vessel with another underneath has none:
 * it is standing on that one instead. The state says which, and the blockstate picks the model to match.
 */
public class AlembicBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** True when there is nothing underneath to stand on, so it has to stand on its own legs. */
    public static final BooleanProperty LEGS = BooleanProperty.create("legs");

    public static final MapCodec<AlembicBlock> CODEC = simpleCodec(AlembicBlock::new);

    // the body, which is a shade wider than a block; the legs underneath are not worth catching a click on
    private static final VoxelShape SHAPE = Block.box(0, 0, 1, 16, 16, 15);

    public AlembicBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LEGS, true));
    }

    @Override
    protected MapCodec<AlembicBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LEGS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlembicBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LEGS, needsLegs(context.getLevel(), context.getClickedPos()));
    }

    /** A vessel sat on another does not put its own legs down through it. */
    private static boolean needsLegs(BlockGetter level, BlockPos pos) {
        return !(level.getBlockState(pos.below()).getBlock() instanceof AlembicBlock);
    }

    /**
     * Sets the legs right for a vessel that arrived without being placed by hand.
     * <p>
     * Only a player's placement goes through {@link #getStateForPlacement}; a structure, a command or another mod
     * sets the block as it stands, and a vessel that turns up already stacked would otherwise have legs through
     * the one below it.
     */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState before, boolean moving) {
        boolean legs = needsLegs(level, pos);
        if (state.getValue(LEGS) != legs) {
            level.setBlock(pos, state.setValue(LEGS, legs), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction towards, BlockState neighbour, LevelAccessor level,
            BlockPos pos, BlockPos neighbourPos) {
        return towards == Direction.DOWN
                ? state.setValue(LEGS, !(neighbour.getBlock() instanceof AlembicBlock))
                : state;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /** Says what has been caught, since there is nowhere else to read it. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof AlembicBlockEntity alembic)) {
            return InteractionResult.PASS;
        }
        // a block that only reports what is in it must not swallow the click: Minecraft asks the block with
        // the item first, then the block on its own, and only then the item itself. A jar that answers a phial
        // with a sentence about its contents is a jar the phial can never be used on.
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        player.displayClientMessage(alembic.holding()
                .map(aspect -> Component.translatable("block.alchemia.alembic.holding",
                        aspect.value().displayName(), alembic.amount(), AlembicBlockEntity.CAPACITY))
                .orElse(Component.translatable("block.alchemia.alembic.empty"))
                .copy().withStyle(ChatFormatting.GRAY), true);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof AlembicBlockEntity alembic) {
            alembic.spill();
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
