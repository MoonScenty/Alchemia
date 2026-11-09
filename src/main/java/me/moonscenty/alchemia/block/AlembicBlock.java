package me.moonscenty.alchemia.block;

import me.moonscenty.alchemia.block.entity.Spills;
import com.mojang.serialization.MapCodec;

import me.moonscenty.alchemia.block.entity.AlembicBlockEntity;
import me.moonscenty.alchemia.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import java.util.Optional;
import me.moonscenty.alchemia.aspect.Aspect;
import me.moonscenty.alchemia.item.PhialItem;
import me.moonscenty.alchemia.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AlembicBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LEGS = BooleanProperty.create("legs");

    public static final MapCodec<AlembicBlock> CODEC = simpleCodec(AlembicBlock::new);

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

    private static boolean needsLegs(BlockGetter level, BlockPos pos) {
        return !(level.getBlockState(pos.below()).getBlock() instanceof AlembicBlock);
    }

    // structures and commands skip getStateForPlacement
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

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(ModItems.JAR_LABEL.get()) || !hit.getDirection().getAxis().isHorizontal()
                || !(level.getBlockEntity(pos) instanceof AlembicBlockEntity alembic) || alembic.filter().isPresent()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Optional<Holder<Aspect>> word = alembic.amount() > 0 ? alembic.holding() : PhialItem.inside(stack);
        if (word.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            alembic.label(word.get(), hit.getDirection());
            stack.consume(1, player);
            level.playSound(null, pos, ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof AlembicBlockEntity alembic
                ? EssentiaSignal.of(alembic.amount(), AlembicBlockEntity.CAPACITY) : 0;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof AlembicBlockEntity alembic)) {
            return InteractionResult.PASS;
        }
        // empty hand only: answering a held item would block that item's own use
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (alembic.filter().isPresent() && hit.getDirection() == alembic.labelFace()) {
                popResourceFromFace(level, pos, hit.getDirection(), new ItemStack(ModItems.JAR_LABEL.get()));
                alembic.unlabel();
                level.playSound(null, pos, ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                return InteractionResult.CONSUME;
            }
            level.playSound(null, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
            level.playSound(null, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.5F,
                    1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.3F);
            alembic.pourOut();
            return InteractionResult.CONSUME;
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
        Spills.onRemove(state, replacement, level, pos);
        super.onRemove(state, level, pos, replacement, moving);
    }
}
