package me.moonscenty.alchemia.block;

import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.ResearchTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A writing desk with the tools of research laid out on it.
 * <p>
 * The inkwell and the half-unrolled note are part of the block rather than things sitting on it, so the state says
 * whether each is there and the model is put together from those pieces.
 */
public class ResearchTableBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** True once a set of scribing tools has been left on the desk. */
    public static final BooleanProperty HAS_TOOLS = BooleanProperty.create("has_tools");
    /** True while a research note is spread out waiting to be worked through. */
    public static final BooleanProperty HAS_NOTES = BooleanProperty.create("has_notes");

    // legs to the tabletop, matching the model
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 10, 0, 16, 16, 16),
            Block.box(1, 0, 1, 5, 10, 5),
            Block.box(11, 0, 1, 15, 10, 5),
            Block.box(1, 0, 11, 5, 10, 15),
            Block.box(11, 0, 11, 15, 10, 15));

    public static final MapCodec<ResearchTableBlock> CODEC = simpleCodec(ResearchTableBlock::new);

    public ResearchTableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HAS_TOOLS, false)
                .setValue(HAS_NOTES, false));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // the drawer handle is on the north face of the model, so the desk faces the player who set it down
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_TOOLS, HAS_NOTES);
    }

    @Override
    protected MapCodec<ResearchTableBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResearchTableBlockEntity(pos, state);
    }

    /** The desk is a piece of furniture, so it is drawn from its model rather than as a plain cube. */
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Lays down or takes back whichever of the two things the player is holding. An empty hand takes the note first,
     * since that is what a player is usually reaching for.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ResearchTableBlockEntity table)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        for (int slot : new int[] {ResearchTableBlockEntity.SLOT_NOTES, ResearchTableBlockEntity.SLOT_TOOLS}) {
            if (table.accepts(slot, held) && table.get(slot).isEmpty()) {
                if (!level.isClientSide) {
                    table.put(slot, held.split(1));
                    level.playSound(null, pos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 0.7F, 1.0F);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        // anything else the player is holding means they want the desk itself, not to put that down on it
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * Sits the player down at the desk. Clearing it is done from there rather than by right-clicking the block, so
     * that taking the note back does not fight with opening the work in front of it.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ResearchTableBlockEntity table)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            player.openMenu(table);
            level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 0.7F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof ResearchTableBlockEntity table) {
            ResearchTableBlockEntity.drop(level, pos, table);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
